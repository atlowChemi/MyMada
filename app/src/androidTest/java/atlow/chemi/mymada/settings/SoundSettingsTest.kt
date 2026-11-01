package atlow.chemi.mymada.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsNotificationsSounds
import atlow.chemi.mymada.helpers.LocalHelper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import org.junit.Rule
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SoundSettingsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private fun clickRow(tag: String) = compose.onNodeWithTag(tag).performScrollTo().performClick()
    @Test fun permissionExplanationRestoresAndCancelChangesOnlyItsOwnGroup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_AUDIO
            else android.Manifest.permission.READ_EXTERNAL_STORAGE
        // Revoke before starting instrumentation: revoking a grant here kills the test process.
        assertEquals(android.content.pm.PackageManager.PERMISSION_DENIED, context.checkSelfPermission(permission))
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("Mapp", false).putBoolean("cut", false)
            .putBoolean("Nsou", true).putBoolean("Napp", false).putBoolean("NappT4", true)
            .putString("NuRi", "content://original/tone").commit()
        context.getSharedPreferences("permissionStatus", 0).edit().putBoolean(permission, true).commit()
        ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
            scenario.onActivity {
                it.displayFragment(SettingsNotificationsSounds(), R.id.soundAndVib)
            }
            clickRow("Napp")
            scenario.recreate()
            compose.onNodeWithTag("dialog-cancel").assertExists()
            clickRow("dialog-cancel")
            assertFalse(preferences.getBoolean("Napp", true))
            assertTrue(preferences.getBoolean("NappT4", false))
            assertEquals("content://original/tone", preferences.getString("NuRi", null))
        }
    }

    @Test fun bilingualGroupsPreserveTogglesAndRingtoneResultsAcrossRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val languages = InstrumentationRegistry.getArguments().getString("language")?.let { listOf(it) } ?: listOf("en", "he")
        for (language in languages) for (cut in listOf(false, true)) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            preferences.edit().apply {
                putBoolean("Mapp", false); putBoolean("cut", cut)
                putBoolean("callT4", true); putBoolean("callT6", true)
                for (type in 0..4) {
                    val suffix = if (type == 0) "" else "T$type"
                    putBoolean("Nvib$suffix", true); putBoolean("Nsou$suffix", true)
                    putBoolean("Napp$suffix", false); putString("NuRi$suffix", "content://previous/$type")
                    if (type in 1..3) putBoolean("callT$type", true)
                }
            }.commit()
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                scenario.onActivity {
                    it.displayFragment(SettingsNotificationsSounds(), R.id.soundAndVib)
                }
                compose.onNodeWithTag(if (cut) "callT1" else "Nvib").assertIsDisplayed()
                capture("sounds-$language-$cut")
                scenario.onActivity {
                    preferences.edit().apply {
                        for (type in 0..4) putBoolean("Napp" + if (type == 0) "" else "T$type", true)
                    }.commit()
                }
                scenario.recreate()
                for (type in if (cut) listOf(1, 2, 3, 4) else listOf(0, 4)) {
                    val suffix = if (type == 0) "" else "T$type"
                    clickRow("Nvib$suffix")
                    assertFalse(preferences.getBoolean("Nvib$suffix", true))
                    clickRow("Nsou$suffix")
                    compose.onNodeWithTag("Napp$suffix").assertIsNotEnabled()
                    compose.onNodeWithTag("NuRi$suffix").assertIsNotEnabled()
                    clickRow("Nsou$suffix")
                    scenario.recreate()
                    scenario.onActivity {
                        val fragment = it.supportFragmentManager.findFragmentById(R.id.frame) as SettingsNotificationsSounds
                        fragment.onActivityResult(type, Activity.RESULT_CANCELED, null)
                        fragment.onActivityResult(type, Activity.RESULT_OK, Intent())
                        assertEquals("content://previous/$type", preferences.getString("NuRi$suffix", null))
                        fragment.onActivityResult(type, Activity.RESULT_OK, Intent().putExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.parse("content://selected/$type")))
                        // Empty permission results occur when a system request is interrupted.
                        fragment.onRequestPermissionsResult(100 + type, emptyArray(), intArrayOf())
                    }
                    assertEquals("content://selected/$type", preferences.getString("NuRi$suffix", null))
                    assertFalse(preferences.getBoolean("Napp$suffix", true))
                    scenario.recreate()
                    compose.onNodeWithTag("NuRi$suffix").assertIsNotEnabled()
                    if (type in 1..3) {
                        clickRow("callT$type")
                        compose.onNodeWithTag("Nvib$suffix").assertDoesNotExist()
                        clickRow("callT$type")
                    }
                }
                scenario.onActivity {
                    preferences.edit().putBoolean("callT4", false).putBoolean("callT6", false).commit()
                }
                scenario.recreate()
                compose.onNodeWithTag("NvibT4").assertDoesNotExist()
            }
        }
    }

    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("capture") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(500, 5_000)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        File(instrumentation.targetContext.filesDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
