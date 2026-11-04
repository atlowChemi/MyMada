package atlow.chemi.mymada.calls

import android.media.AudioManager
import android.widget.Button
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.smsRes
import org.junit.Assert.*
import org.junit.Test

class CallPopupComposeTest {
    @Test fun loopSurvivesRecreationAndCloseRestoresVolume() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val audio = context.getSystemService(AudioManager::class.java)
        val original = audio.getStreamVolume(AudioManager.STREAM_ALARM)
        val selected = if (original == 1) 2 else 1
        val settings = context.getSharedPreferences("Settings", 0)
        settings.edit().putBoolean("Mapp", false).putBoolean("cut", true).putInt("sleep", 0)
            .putBoolean("stat", false).putBoolean("NsouT1", true)
            .putBoolean("NvibT1", false).putBoolean("NappT1", false).putBoolean("loop", true)
            .putBoolean("killS", false).putBoolean("volumeApp", true).putInt("volumeAppNum", selected).commit()
        try {
            ActivityScenario.launch<smsRes>(CallEvent(1, "בדיקת לולאה", null).popupIntent(context, -1, true)).use { scenario ->
                scenario.onActivity {
                    assertEquals("בדיקת לולאה", it.findViewById<TextView>(R.id.Address).text.toString())
                    assertEquals(selected, audio.getStreamVolume(AudioManager.STREAM_ALARM))
                }
                scenario.recreate()
                scenario.onActivity {
                    assertEquals(selected, audio.getStreamVolume(AudioManager.STREAM_ALARM))
                    it.findViewById<Button>(R.id.button).performClick()
                }
            }
            assertEquals(original, audio.getStreamVolume(AudioManager.STREAM_ALARM))
        } finally {
            audio.setStreamVolume(AudioManager.STREAM_ALARM, original, 0)
            settings.edit().putBoolean("loop", false).putBoolean("NsouT1", false).commit()
        }
    }
}
