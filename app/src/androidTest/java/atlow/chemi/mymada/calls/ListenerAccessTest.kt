package atlow.chemi.mymada.calls

import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

/** Disposable emulator only: restores the original OS grant list even when an assertion fails. */
class ListenerAccessTest {
    /** Requires the offline source-fixture APK and an externally configured OS listener grant. */
    @Test fun osBoundListenerPreservesRepeatedSourceNotifications() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        val broadcasts = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(context)
        val count = java.util.concurrent.atomic.AtomicInteger()
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
                if (intent.getStringExtra("pack") == "atlow.mymadaadmin") count.incrementAndGet()
            }
        }
        fun await(predicate: () -> Boolean) {
            val deadline = android.os.SystemClock.uptimeMillis() + 10000
            while (!predicate() && android.os.SystemClock.uptimeMillis() < deadline) android.os.SystemClock.sleep(50)
            assertTrue(predicate())
        }
        val selected = requireNotNull(ListenerAccess.selected(context))
        if (android.os.Build.VERSION.SDK_INT == 24) {
            // Instrumentation force-stops the target; API24 does not rebind that stopped listener.
            // Reapply the OS grant in the harness, without cycling application components.
            val grants = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            fun shell(command: String) = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
                .use { it.readBytes() }
            shell("settings delete secure enabled_notification_listeners")
            android.os.SystemClock.sleep(200)
            shell("settings put secure enabled_notification_listeners $grants")
        }
        ListenerAccess.rebind(context)
        await {
            val dump = ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("dumpsys notification"))
                .bufferedReader().use { it.readText() }
            dump.substringAfter("Live notification listeners").substringBefore("Snoozed notification listeners")
                .contains(selected.className)
        }
        preferences.edit().putBoolean("Mapp", true).putBoolean("win", false).putBoolean("not", false).commit()
        broadcasts.registerReceiver(receiver, android.content.IntentFilter("MADA_APP"))
        try {
            repeat(2) { index ->
                context.sendBroadcast(android.content.Intent().setComponent(android.content.ComponentName("atlow.mymadaadmin", "atlow.mymadaadmin.Source"))
                    .putExtra("id", 412))
                await { count.get() >= index + 1 }
                instrumentation.uiAutomation.waitForIdle(300, 5000)
                assertEquals("One delivery per source callback, even with both grants", index + 1, count.get())
            }
        } finally {
            broadcasts.unregisterReceiver(receiver)
            preferences.edit().putBoolean("Mapp", false).commit()
        }
    }

    @Test fun nativeAndRnComponentGrantsSurviveRebindWithoutTogglingComponents() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val original = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        fun shell(command: String) {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
                .use { it.readBytes() }
        }
        fun set(value: String?) = shell(if (value.isNullOrEmpty()) "settings delete secure enabled_notification_listeners"
            else "settings put secure enabled_notification_listeners $value")
        val components = ListenerAccess.components(context)
        try {
            for (component in components) {
                val info = context.packageManager.getServiceInfo(component, 0)
                assertEquals("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE", info.permission)
                assertTrue(info.exported)
                assertTrue(info.enabled)
                val state = context.packageManager.getComponentEnabledSetting(component)
                set(component.flattenToString())
                assertEquals(component, ListenerAccess.selected(context))
                ListenerAccess.rebind(context)
                assertEquals(component, ListenerAccess.selected(context))
                assertEquals(state, context.packageManager.getComponentEnabledSetting(component))
            }
            set(components.reversed().joinToString(":") { it.flattenToString() })
            assertEquals("Stable selection when both identities are granted", components.first(), ListenerAccess.selected(context))
            set("${context.packageName}/.UnrelatedListener")
            assertNull("A package-level grant does not authorize our listener", ListenerAccess.selected(context))
            set(null)
            assertNull(ListenerAccess.selected(context))
        } finally {
            set(original)
        }
    }
}
