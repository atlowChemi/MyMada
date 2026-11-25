package atlow.chemi.mymada.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Opt-in device check after selecting an external, >1s audio file through Settings. */
class ExternalAudioDispatchTest {
    @org.junit.Before fun seedCiSelection() {
        if (InstrumentationRegistry.getArguments().getString("externalAudioFixture") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${context.packageName} $permission")).use { it.readBytes() }
        val collection = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var selected: Uri? = null
        val deadline = SystemClock.uptimeMillis() + 10000
        while (selected == null && SystemClock.uptimeMillis() < deadline) {
            context.contentResolver.query(collection, arrayOf("_id"), "_display_name = ?",
                arrayOf("mymada-ci-external.mp3"), null)?.use { cursor ->
                if (cursor.moveToFirst()) selected = android.content.ContentUris.withAppendedId(collection, cursor.getLong(0))
            }
            if (selected == null) SystemClock.sleep(50)
        }
        assertNotNull("Host audio fixture must be indexed by MediaStore", selected)
        context.getSharedPreferences("Settings", 0).edit()
            .putBoolean("Napp", true).putString("NuRi", selected.toString()).commit()
    }

    @Test fun selectedExternalAudioDecodesAndDispatchesOnAlarmStream() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("externalAudio") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
        assertEquals(PackageManager.PERMISSION_GRANTED, context.checkSelfPermission(permission))
        val preferences = context.getSharedPreferences("Settings", 0)
        assertTrue(preferences.getBoolean("Napp", false))
        val uri = Uri.parse(requireNotNull(preferences.getString("NuRi", null)))
        assertTrue(uri.toString().startsWith("content://media/external/"))
        context.contentResolver.openInputStream(uri)!!.use { assertTrue(it.read() >= 0) }
        val player = MediaPlayer()
        try {
            // Same data-source/stream/prepare/start path as smsRes and FloatingWidgetService.
            player.setDataSource(context, uri)
            player.setAudioStreamType(AudioManager.STREAM_ALARM)
            player.prepare()
            assertTrue(player.duration > 1000)
            player.start()
            SystemClock.sleep(500) // Measure decoded playback progress, not UI readiness.
            assertTrue(player.isPlaying)
            assertTrue(player.currentPosition > 0)
            if (Build.VERSION.SDK_INT >= 26) {
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                assertTrue(audio.activePlaybackConfigurations.any {
                    it.audioAttributes.usage == android.media.AudioAttributes.USAGE_ALARM
                })
            }
            android.util.Log.i("ExternalAudioDispatch", "uri=$uri position=${player.currentPosition} duration=${player.duration}; dispatch verified, physical audibility not tested")
        } finally {
            player.release()
        }
    }
}
