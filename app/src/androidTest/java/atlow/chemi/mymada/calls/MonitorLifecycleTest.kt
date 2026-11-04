package atlow.chemi.mymada.calls

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.recieversAndServices.ActionReceiver
import atlow.chemi.mymada.recieversAndServices.BootCompleteReceiver
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
import org.junit.Assert.*
import org.junit.Test

/** Service lifecycle checks, not POST_NOTIFICATIONS user-flow certification. */
class MonitorLifecycleTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)
    private fun allowNotifications() {
        if (Build.VERSION.SDK_INT >= 33) ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}")).use { it.readBytes() }
    }
    private fun await(predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5000
        while (!predicate() && SystemClock.uptimeMillis() < deadline) SystemClock.sleep(30)
        assertTrue(predicate())
    }

    @Test fun repeatedStartAndNotificationStopCleanUpOnlyTheMonitor() {
        allowNotifications()
        val manager = context.getSystemService(NotificationManager::class.java)
        ActivityScenario.launch(InfoActivityKt::class.java).use { scenario ->
            try {
                preferences.edit().putBoolean("Mapp", true).commit()
                scenario.onActivity { SMSMonitorService.start(it); SMSMonitorService.start(it) }
                await { manager.activeNotifications.count { it.id == 78 } == 1 }
                val stopped = mutableListOf<String?>()
                val forwarding = object : ContextWrapper(context) {
                    override fun stopService(service: Intent): Boolean {
                        stopped += service.component?.className
                        return super.stopService(service)
                    }
                }
                ActionReceiver().onReceive(forwarding, Intent().putExtra("action", "runInBG"))
                assertEquals(listOf(SMSMonitorService::class.java.name), stopped)
                assertFalse(preferences.getBoolean("Mapp", true))
                await { manager.activeNotifications.none { it.id == 78 } }
                scenario.onActivity { SMSMonitorService.start(it) }
                assertTrue(manager.activeNotifications.none { it.id == 78 })
            } finally {
                preferences.edit().putBoolean("Mapp", false).commit()
                context.stopService(Intent(context, SMSMonitorService::class.java))
            }
        }
    }

    @Test fun bootAndOwnUpdateRespectOptInAndIgnoreOtherPackages() {
        allowNotifications()
        val starts = mutableListOf<Intent>()
        val recording = object : ContextWrapper(context) {
            override fun startService(service: Intent) = service.component.also { starts += service }
            override fun startForegroundService(service: Intent) = startService(service)
        }
        val receiver = BootCompleteReceiver()
        try {
            preferences.edit().putBoolean("Mapp", false).commit()
            receiver.onReceive(recording, Intent(Intent.ACTION_BOOT_COMPLETED))
            assertTrue(starts.isEmpty())
            preferences.edit().putBoolean("Mapp", true).commit()
            receiver.onReceive(recording, Intent(Intent.ACTION_PACKAGE_ADDED))
            receiver.onReceive(recording, null)
            assertTrue(starts.isEmpty())
            receiver.onReceive(recording, Intent(Intent.ACTION_BOOT_COMPLETED))
            receiver.onReceive(recording, Intent(Intent.ACTION_MY_PACKAGE_REPLACED))
            assertEquals(2, starts.size)
            assertTrue(starts.all { it.component?.className == SMSMonitorService::class.java.name })
        } finally { preferences.edit().putBoolean("Mapp", false).commit() }
    }
}
