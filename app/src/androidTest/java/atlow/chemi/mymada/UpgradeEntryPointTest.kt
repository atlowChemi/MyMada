package atlow.chemi.mymada

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.calls.CallEvent
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import org.junit.Assert.*
import org.junit.Test

class UpgradeEntryPointTest {
    @Test fun installedRnAliasesAndNotificationPayloadsRetainTheirDestinations() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext
        for ((alias, target) in mapOf("MainReactActivity" to HomePage::class.java.name, "MadaCallActivity" to smsRes::class.java.name)) {
            val info = app.packageManager.getActivityInfo(ComponentName(app.packageName, "atlow.chemi.mymada.$alias"), 0)
            assertEquals(target, info.targetActivity)
            assertTrue(info.exported)
        }
        var launched: Intent? = null
        val context = object : ContextWrapper(app) {
            override fun startActivity(intent: Intent) { launched = intent }
        }
        fun route(target: String, expected: Class<*>, extras: Intent.() -> Unit = {}): Intent {
            launched = null
            assertTrue(MyFirebaseMessagingService.routeLauncher(context, Intent().putExtra("target", target).apply(extras)))
            return requireNotNull(launched).also { assertEquals(expected.name, it.component?.className) }
        }
        assertTrue(route("Dispatch", MokedPage::class.java) { putExtra("page", "Form") }.getBooleanExtra("msg", false))
        assertTrue(route("MetronomeActivity", UtillitiesAndTools::class.java).getBooleanExtra("running", false))
        route("ContactUs", contactChatActivity::class.java)
        val activation = route("ActivateCode", codeC::class.java) { putExtra("code", "0012"); putExtra("firebaseKey", "offline-key") }
        assertEquals("0012", activation.getStringExtra("code"))
        assertEquals("offline-key", activation.getStringExtra("fireKey"))
        assertFalse(MyFirebaseMessagingService.routeLauncher(context, Intent().putExtra("target", "ActivateCode").putExtra("code", "bad")))
        for (type in 1..6) {
            val event = CallEvent.from(Intent().putExtra("callType", type.toDouble()).putExtra("text", "offline retained text"))
            assertEquals(type, event.type)
            assertEquals("offline retained text", event.text)
        }
        assertEquals(1, CallEvent.from(Intent().putExtra("callType", 2.5)).type)
        assertEquals(1, CallEvent.from(Intent().putExtra("callType", 7)).type)
        assertEquals(4, CallEvent.from(Intent().putExtra("callT", 4).putExtra("callType", 2.0)).type)
    }
}
