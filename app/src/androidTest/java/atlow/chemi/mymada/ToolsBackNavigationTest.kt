package atlow.chemi.mymada

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class ToolsBackNavigationTest {
    @Test fun backClosesUnlockedDrawerButLeavesToolsWhenClosedOrLocked() {
        InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("Settings", 0)
            .edit().putBoolean("Mapp", false).putStringSet("Tools", setOf("oxy")).commit()
        for (locked in listOf(false, true)) {
            ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
                scenario.recreate()
                scenario.onActivity { activity ->
                    val drawer = activity.findViewById<DrawerLayout>(R.id.activity_Utils)
                    drawer.openDrawer(GravityCompat.START, false)
                    if (locked) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
                    activity.onBackPressedDispatcher.onBackPressed()
                    if (!locked) {
                        assertFalse(activity.isFinishing)
                    }
                }
                if (!locked) {
                    var visible = true
                    val deadline = android.os.SystemClock.uptimeMillis() + 5000
                    while (visible && android.os.SystemClock.uptimeMillis() < deadline) {
                        scenario.onActivity { visible = it.findViewById<DrawerLayout>(R.id.activity_Utils)
                            .isDrawerVisible(GravityCompat.START) }
                        if (visible) Thread.sleep(25)
                    }
                    assertFalse("Back closed the drawer", visible)
                    scenario.onActivity {
                        it.onBackPressedDispatcher.onBackPressed()
                    }
                }
                // Modern Android sends a root task to the background instead of finishing it.
                val deadline = android.os.SystemClock.uptimeMillis() + 5000
                while (scenario.state != Lifecycle.State.CREATED && scenario.state != Lifecycle.State.DESTROYED &&
                    android.os.SystemClock.uptimeMillis() < deadline)
                    Thread.sleep(25)
                assertTrue("Back leaves Tools (locked=$locked, state=${scenario.state})", scenario.state == Lifecycle.State.CREATED ||
                    scenario.state == Lifecycle.State.DESTROYED)
            }
        }
    }
}
