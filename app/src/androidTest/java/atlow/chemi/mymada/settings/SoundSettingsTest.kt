package atlow.chemi.mymada.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.fragments.SettingsNotificationsSounds
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SoundSettingsTest {
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
                it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.soundAndVib))
            }
            onView(withId(R.id.NappT_pref)).perform(scrollTo(), click())
            scenario.recreate()
            onView(withText(R.string.permDesSD)).inRoot(isDialog()).check(matches(isDisplayed()))
            onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click())
            assertFalse(preferences.getBoolean("Napp", true))
            assertTrue(preferences.getBoolean("NappT4", false))
            assertEquals("content://original/tone", preferences.getString("NuRi", null))
        }
    }

    @Test fun bilingualGroupsPreserveTogglesAndRingtoneResultsAcrossRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val rows = listOf(
            intArrayOf(R.id.NvibT_pref, R.id.NsouT_pref, R.id.NappT_pref, R.id.NrinT_pref),
            intArrayOf(R.id.NvibT1_pref, R.id.NsouT1_pref, R.id.NappT1_pref, R.id.NrinT1_pref),
            intArrayOf(R.id.NvibT2_pref, R.id.NsouT2_pref, R.id.NappT2_pref, R.id.NrinT2_pref),
            intArrayOf(R.id.NvibT3_pref, R.id.NsouT3_pref, R.id.NappT3_pref, R.id.NrinT3_pref),
            intArrayOf(R.id.NvibT4_pref, R.id.NsouT4_pref, R.id.NappT4_pref, R.id.NrinT4_pref))
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
                    it.onNavigationItemSelected(it.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.soundAndVib))
                }
                onView(withId(if (cut) R.id.findT1_pref else R.id.NvibT_pref)).check(matches(isDisplayed()))
                capture("sounds-$language-$cut")
                scenario.onActivity {
                    preferences.edit().apply {
                        for (type in 0..4) putBoolean("Napp" + if (type == 0) "" else "T$type", true)
                    }.commit()
                }
                scenario.recreate()
                for (type in if (cut) listOf(1, 2, 3, 4) else listOf(0, 4)) {
                    val suffix = if (type == 0) "" else "T$type"
                    onView(withId(rows[type][0])).perform(scrollTo(), click())
                    assertFalse(preferences.getBoolean("Nvib$suffix", true))
                    onView(withId(rows[type][1])).perform(scrollTo(), click())
                    onView(withId(rows[type][2])).check(matches(org.hamcrest.Matchers.not(isEnabled())))
                    onView(withId(rows[type][3])).check(matches(org.hamcrest.Matchers.not(isEnabled())))
                    onView(withId(rows[type][1])).perform(click())
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
                    onView(withId(rows[type][3])).check(matches(org.hamcrest.Matchers.not(isEnabled())))
                    if (type in 1..3) {
                        val row = listOf(R.id.findT1_pref, R.id.findT2_pref, R.id.findT3_pref)[type - 1]
                        onView(withId(row)).perform(scrollTo(), click())
                        onView(withId(rows[type][0])).check(matches(withEffectiveVisibility(Visibility.GONE)))
                        onView(withId(row)).perform(click())
                    }
                }
                scenario.onActivity {
                    preferences.edit().putBoolean("callT4", false).putBoolean("callT6", false).commit()
                }
                scenario.recreate()
                onView(withId(R.id.non_critical)).check(matches(withEffectiveVisibility(Visibility.GONE)))
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
