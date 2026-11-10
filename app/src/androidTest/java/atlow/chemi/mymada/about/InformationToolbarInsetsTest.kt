package atlow.chemi.mymada.about

import android.app.Activity
import android.view.View
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.PrivacyActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Run on the owned offline emulator: both users of the shared information toolbar. */
class InformationToolbarInsetsTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun informationActionsStayBelowStatusBar() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val monitor = ActivityLifecycleMonitorRegistry.getInstance()
        for (activity in listOf<Class<out Activity>>(InfoActivityKt::class.java, PrivacyActivity::class.java)) {
            var owner: Activity? = null
            val callback = ActivityLifecycleCallback { instance, stage ->
                if (activity.isInstance(instance) && stage == Stage.PRE_ON_CREATE) owner = instance
            }
            instrumentation.runOnMainSync { monitor.addLifecycleCallback(callback) }
            try {
                // Privacy's animated offline WebView need not let startActivitySync become idle.
                instrumentation.runOnMainSync {
                    val context = instrumentation.targetContext
                    context.startActivity(android.content.Intent(context, activity).addFlags(
                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
                compose.waitUntil(10_000) {
                    var ready = false
                    instrumentation.runOnMainSync {
                        ready = owner?.let { monitor.getLifecycleStageOf(it) == Stage.RESUMED && it.hasWindowFocus() } == true
                    }
                    ready
                }
                val bounds = compose.onNodeWithTag("information-toolbar").fetchSemanticsNode().boundsInRoot
                instrumentation.runOnMainSync {
                    val it = owner!!
                    val content = it.findViewById<View>(android.R.id.content)
                    val origin = IntArray(2).also(content::getLocationOnScreen)
                    val statusBar = ViewCompat.getRootWindowInsets(content)!!
                        .getInsets(WindowInsetsCompat.Type.statusBars()).top
                    assertTrue("${activity.simpleName}: toolbar $bounds + origin ${origin[1]} overlaps status bar $statusBar",
                        bounds.top + origin[1] >= statusBar)
                    assertEquals(56 * it.resources.displayMetrics.density, bounds.height, 1f)
                }
            } finally {
                try {
                    instrumentation.runOnMainSync { owner?.let { if (!it.isDestroyed) it.finish() } }
                    compose.waitUntil(10_000) {
                        var destroyed = false
                        instrumentation.runOnMainSync { destroyed = owner?.isDestroyed != false }
                        destroyed
                    }
                } finally {
                    instrumentation.runOnMainSync { monitor.removeLifecycleCallback(callback) }
                }
            }
        }
    }
}
