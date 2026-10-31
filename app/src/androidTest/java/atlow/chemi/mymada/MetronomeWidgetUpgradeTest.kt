package atlow.chemi.mymada

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.recieversAndServices.MetroService
import atlow.chemi.mymada.recieversAndServices.MetroWidget
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Run createFixture on the retained pre-controls APK, install -r, then verifyFixture. */
@RunWith(AndroidJUnit4::class)
class MetronomeWidgetUpgradeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val prefs = context.getSharedPreferences("widget-upgrade-test", 0)
    private val manager = AppWidgetManager.getInstance(context)
    private val host = AppWidgetHost(context, 2500)

    @Test fun createFixture() {
        val shell = instrumentation.uiAutomation.executeShellCommand("appwidget grantbind --package ${context.packageName}")
        shell.close()
        // Shell grant delivery is asynchronous; bind attempts are bounded below.
        val id = host.allocateAppWidgetId()
        val deadline = SystemClock.elapsedRealtime() + 5000
        var bound = false
        while (!bound && SystemClock.elapsedRealtime() < deadline) {
            bound = manager.bindAppWidgetIdIfAllowed(id, ComponentName(context, MetroWidget::class.java))
            if (!bound) Thread.sleep(50)
        }
        assertTrue("Widget host grant", bound)
        assertTrue(prefs.edit().putInt("id", id).commit())
        assertNotNull(manager.getAppWidgetInfo(id))
    }

    @Test fun verifyFixture() {
        val id = prefs.getInt("id", -1)
        assertTrue("Run createFixture before install -r", id > 0)
        val info = manager.getAppWidgetInfo(id)
        assertNotNull("Installed widget binding retained", info)
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { screen ->
            lateinit var view: AppWidgetHostView
            try {
                screen.onActivity {
                    host.startListening()
                    view = host.createView(it, id, info)
                    it.findViewById<FrameLayout>(R.id.frame).addView(view)
                    context.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setComponent(ComponentName(context, MetroWidget::class.java))
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(id)))
                }
                await {
                    var present = false
                    instrumentation.runOnMainSync { present = view.findViewById<View>(R.id.metro_widget_image) != null }
                    present
                }
                instrumentation.runOnMainSync { view.findViewById<View>(R.id.metro_widget_image).performClick() }
                await { MetroService.running }
                instrumentation.runOnMainSync { view.findViewById<View>(R.id.metro_widget_image).performClick() }
                await { !MetroService.running }
                // Also exercise an RN-installed PendingIntent's original action contract.
                context.sendBroadcast(Intent(context, MetroWidget::class.java).setAction("ON_CLICK_TAG"))
                await { MetroService.running }
                context.sendBroadcast(Intent(context, MetroWidget::class.java).setAction("ON_CLICK_TAG"))
                await { !MetroService.running }
            } finally {
                instrumentation.runOnMainSync { MetroService.stop(context); host.stopListening() }
            }
        }
    }

    private fun await(condition: () -> Boolean) {
        val end = SystemClock.elapsedRealtime() + 8000
        while (!condition() && SystemClock.elapsedRealtime() < end) Thread.sleep(25)
        assertTrue(condition())
    }
}
