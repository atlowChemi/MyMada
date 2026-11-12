package atlow.chemi.mymada.calls

import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals

/** Opt-in disposable-emulator grants, applied after instrumentation's force-stop. */
fun configureCallFixture() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val context = instrumentation.targetContext
    val arguments = InstrumentationRegistry.getArguments()
    fun shell(command: String) = ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    arguments.getString("listenerFixture")?.let { mode ->
        val components = ListenerAccess.components(context)
        val grants = when (mode) {
            "native" -> components.take(1)
            "rn" -> components.takeLast(1)
            "both" -> components
            else -> error("Unknown listener fixture: $mode")
        }
        if (Build.VERSION.SDK_INT >= 27) {
            grants.forEach { shell("cmd notification allow_listener ${it.flattenToString()}") }
        } else shell("settings put secure enabled_notification_listeners ${grants.joinToString(":") { it.flattenToString() }}")
        assertEquals(grants, ListenerAccess.granted(context))
    }
    arguments.getString("overlayFixture")?.let { mode ->
        require(mode == "allow" || mode == "deny") { "Unknown overlay fixture: $mode" }
        shell("appops set ${context.packageName} SYSTEM_ALERT_WINDOW $mode")
        assertEquals(mode == "allow", Settings.canDrawOverlays(context))
    }
}
