package atlow.chemi.mymada.settings

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.view.GravityCompat
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.helpers.MyDrawerLayout
import com.google.android.material.navigation.NavigationView
import org.junit.Assert.*
import org.junit.Test

class SettingsNavigationTest {
    @Test fun eachDestinationRestoresAndDrawerBackDoesNotFinishTheScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).commit()
        for (language in listOf("en", "he")) {
            instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
            ActivityScenario.launch(SettingsPage::class.java).use { scenario ->
                for (destination in listOf(R.id.notification, R.id.soundAndVib, R.id.advanced, R.id.ampm, R.id.tools)) {
                    scenario.onActivity { activity ->
                        val navigation = activity.findViewById<NavigationView>(R.id.navigation)
                        activity.onNavigationItemSelected(navigation.menu.findItem(destination))
                    }
                    instrumentation.waitForIdleSync()
                    scenario.recreate()
                    scenario.onActivity { activity ->
                        assertEquals(destination, activity.findViewById<NavigationView>(R.id.navigation).checkedItem!!.itemId)
                        assertEquals("settings:$destination", activity.supportFragmentManager.findFragmentById(R.id.frame)!!.tag)
                        val navigation = activity.findViewById<NavigationView>(R.id.navigation)
                        val labels = arrayListOf<android.view.View>()
                        navigation.findViewsWithText(labels, navigation.checkedItem!!.title, android.view.View.FIND_VIEWS_WITH_TEXT)
                        assertTrue(labels.isNotEmpty())
                        labels.filterIsInstance<android.widget.TextView>().forEach { assertFalse(it.typeface.isBold) }
                        activity.findViewById<MyDrawerLayout>(R.id.activity_Utils).openDrawer(GravityCompat.START, false)
                    }
                    pressBack()
                    instrumentation.waitForIdleSync()
                    scenario.onActivity { assertFalse(it.isFinishing) }
                }
                // A returned screen must use the persisted state, not a stale MenuItem flag.
                scenario.onActivity { activity ->
                    context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", true).commit()
                    val item = activity.findViewById<NavigationView>(R.id.navigation).menu.findItem(R.id.detect)
                    item.isChecked = false
                    activity.onNavigationItemSelected(item)
                    assertFalse(context.getSharedPreferences("Settings", 0).getBoolean("Mapp", true))
                    assertFalse(item.isChecked)
                }
                pressBackUnconditionally()
                val deadline = android.os.SystemClock.uptimeMillis() + 3_000
                while (scenario.state != androidx.lifecycle.Lifecycle.State.DESTROYED && android.os.SystemClock.uptimeMillis() < deadline)
                    android.os.SystemClock.sleep(25)
                assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.state)
            }
        }
    }
}
