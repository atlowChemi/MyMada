package atlow.chemi.mymada.home

import android.app.Activity
import android.os.Debug
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.UtillitiesAndTools
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Same real navigation route on native master and Compose Home; stock 48MiB, offline emulator. */
class HomeMemoryTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun recreatedHomeToolsSettingsAboutAndBack() {
        prepare("en")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("oldU", false).commit()
        ActivityScenario.launch(HomePage::class.java).use { home ->
            var settings = ""
            home.onActivity { settings = it.getString(id(it, "title_activity_settings", "string")) }
            visit(SettingsPage::class.java) { onView(withText(settings)).perform(click()) }
            home.moveToState(Lifecycle.State.CREATED)
            context.getSharedPreferences("Settings", 0).edit().putString("lng", "he")
                .putBoolean("WasLangChanged", true).commit()
            home.moveToState(Lifecycle.State.RESUMED)
            compose.waitForIdle()
        }
        prepare("en")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("privacy", false).commit()
        ActivityScenario.launch(HomePage::class.java).use { home ->
            var privacy = ""
            home.onActivity { privacy = it.getString(id(it, "title_activity_privacy", "string")) }
            visit(atlow.chemi.mymada.PrivacyActivity::class.java) { onView(withText(privacy)).perform(click()) }
        }
        for (language in listOf("he", "en")) verifyRoute(language)
    }

    private fun prepare(language: String) {
        context.getSharedPreferences("Settings", 0).edit().clear().putString("lng", language)
            .putBoolean("privacy", true).putBoolean("oldU", true).putBoolean("Mapp", false)
            .putStringSet("Tools", setOf("metro", "oxy")).commit()
        context.getSharedPreferences("rate_app", 0).edit().putBoolean("dontshowagain", true).commit()
        instrumentation.uiAutomation.executeShellCommand("appops set ${context.packageName} SYSTEM_ALERT_WINDOW allow").use {
            android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
        }
    }

    private fun verifyRoute(language: String) {
        prepare(language)
        ActivityScenario.launch(HomePage::class.java).use { home ->
            compose.waitForIdle()
            memory("home")
            home.recreate()
            compose.waitForIdle()
            memory("recreated")
            visit(UtillitiesAndTools::class.java) { clickTile(home, 0, "title_activity_utills_and_tools") }
            memory("tools returned")
            visit(SettingsPage::class.java) {
                home.onActivity { activity ->
                    val menu = androidx.appcompat.widget.PopupMenu(activity, View(activity)).menu
                    activity.onCreateOptionsMenu(menu)
                    activity.onOptionsItemSelected(menu.findItem(id(activity, "action_settings", "id")))
                }
            }
            memory("settings returned")
            if (InstrumentationRegistry.getArguments().getString("dumpHeap") == "true") {
                Debug.dumpHprofData(File(context.filesDir, "home-before-about.hprof").path)
            }
            visit(InfoActivityKt::class.java) { clickTile(home, 4, "title_activity_info") }
            memory("about returned")
        }
    }

    private fun id(activity: Activity, name: String, type: String) =
        activity.resources.getIdentifier(name, type, activity.packageName)

    private fun clickTile(home: ActivityScenario<HomePage>, position: Int, label: String) {
        var native = false
        var text = ""
        home.onActivity { activity ->
            text = activity.getString(id(activity, label, "string"))
            val rvId = id(activity, "rv", "id")
            val rv = if (rvId == 0) null else activity.findViewById<RecyclerView>(rvId)
            native = rv != null
            rv?.scrollToPosition(position)
        }
        compose.waitForIdle()
        if (native) home.onActivity { activity ->
            val rv = activity.findViewById<RecyclerView>(id(activity, "rv", "id"))
            val holder = rv.findViewHolderForAdapterPosition(position)
            assertNotNull(holder)
            holder!!.itemView.findViewById<View>(id(activity, "buttonChange", "id")).performClick()
        } else compose.onNodeWithText(text).performScrollTo().performClick()
    }

    private fun visit(destination: Class<out Activity>, click: () -> Unit) {
        val monitor = instrumentation.addMonitor(destination.name, null, false)
        try {
            click()
            val activity = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
            assertNotNull(activity)
            compose.waitForIdle()
            memory(destination.simpleName)
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            compose.waitUntil(5_000) { activity!!.isDestroyed }
            compose.waitForIdle()
        } finally { instrumentation.removeMonitor(monitor) }
    }

    private fun memory(stage: String) {
        val runtime = Runtime.getRuntime()
        Log.i("HomeMemory", "$stage used=${runtime.totalMemory() - runtime.freeMemory()} max=${runtime.maxMemory()}")
    }
}
