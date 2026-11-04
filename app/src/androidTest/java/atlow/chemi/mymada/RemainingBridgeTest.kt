package atlow.chemi.mymada

import android.content.Context
import android.content.ComponentName
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.recieversAndServices.ActionReceiver
import atlow.chemi.mymada.recieversAndServices.MetroService
import atlow.chemi.mymada.recieversAndServices.MetroWidget
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
import org.junit.Assert.*
import org.junit.Test

class RemainingBridgeTest {
    @Test fun receiverRoutesInstalledExtrasAndLegacyWidgetWithoutChangingStopSemantics() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val target = instrumentation.targetContext
        val registered = target.packageManager.getReceiverInfo(ComponentName(target, ActionReceiver::class.java), 0)
        assertEquals("atlow.chemi.mymada.recieversAndServices.ActionReceiver", registered.name)
        assertTrue(registered.enabled)
        assertFalse(registered.exported)
        val calls = mutableListOf<Pair<String, Intent>>()
        var reject = false
        val context = object : ContextWrapper(target) {
            override fun startService(service: Intent) = service.component.also {
                if (reject) throw IllegalStateException("Rejected start")
                calls += "start" to service
            }
            override fun startForegroundService(service: Intent) = service.component.also {
                if (reject) throw IllegalStateException("Rejected foreground start")
                calls += "foreground" to service
            }
            override fun stopService(service: Intent): Boolean {
                if (reject) throw IllegalStateException("Rejected stop")
                calls += "stop" to service
                return true
            }
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("remaining-bridge-test.$name", mode)
        }
        val settings = context.getSharedPreferences("Settings", 0)
        val recovery = context.getSharedPreferences("CallRecovery", 0)
        instrumentation.runOnMainSync {
            val before = MetroService.running
            val receiver = ActionReceiver::class.java.getConstructor().newInstance()
            fun receive(action: String?) = receiver.onReceive(context, Intent().putExtra("action", action))
            fun expect(operation: String, type: Class<*>, action: String? = null) {
                assertEquals(1, calls.size)
                val (actual, intent) = calls.single()
                assertEquals(operation, actual)
                assertEquals(type.name, intent.component!!.className)
                assertEquals(action, intent.action)
                calls.clear()
            }
            try {
                MetroService.running = false
                receiver.onReceive(context, Intent("widget")) // Intent.action is not the installed extra.
                receive(null)
                receive("unknown")
                assertTrue(calls.isEmpty())
                receive("widget")
                expect(if (Build.VERSION.SDK_INT >= 26) "foreground" else "start", MetroService::class.java)
                MetroWidget().onReceive(context, Intent("ON_CLICK_TAG"))
                expect(if (Build.VERSION.SDK_INT >= 26) "foreground" else "start", MetroService::class.java)
                reject = true
                receive("widget") // Only widget start failures are caught.
                assertTrue(calls.isEmpty())
                reject = false
                receive("stopM")
                expect("stop", MetroService::class.java)
                MetroService.running = true
                receive("widget")
                expect("start", MetroService::class.java, "metronome.stop")
                receive("stopM")
                expect("start", MetroService::class.java, "metronome.stop")
                reject = true
                try {
                    receive("stopM")
                    fail("Stop failures must still propagate")
                } catch (_: IllegalStateException) { }
                reject = false
                settings.edit().putBoolean("Mapp", true).commit()
                recovery.edit().putString("epoch", "fixture").putLong("floor", 123).commit()
                receive("runInBG")
                expect("stop", SMSMonitorService::class.java)
                assertFalse(settings.getBoolean("Mapp", true))
                assertTrue(recovery.all.isEmpty())
                assertTrue("Detection stop must not stop the metronome", MetroService.running)
            } finally {
                MetroService.running = before
                settings.edit().clear().commit()
                recovery.edit().clear().commit()
            }
        }
    }
}
