package atlow.chemi.mymada.integrations

import android.app.Activity
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import org.junit.Assert.*
import org.junit.Test

class SdkIntegrationTest {
    @Test fun routesDeduplicateRecreationAndRetainRnEventPayloads() {
        val events = mutableListOf<Pair<String, Bundle>>()
        val observer = SdkAnalytics { name, parameters -> events += name to parameters }
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            observer.onActivityResumed(HomePage())
            assertTrue(events.isEmpty())
            val routes = listOf(
                UtillitiesAndTools() to "Tools", lockPage() to "Lock", MokedPage() to "Dispatch",
                contactChatActivity() to "ContactUs", codeRequest() to "RequestCode", codeC() to "ActivateCode",
                InfoActivityKt() to "About", PrivacyActivity() to "Privacy", SettingsPage() to "Settings", HomePage() to "Home"
            )
            for ((activity, name) in routes) {
                val before = events.size
                observer.onActivityResumed(activity)
                observer.onActivityResumed(activity)
                observer.onActivityResumed(Activity())
                assertEquals(before + 1, events.size)
                assertEquals("screen_view", events.last().first)
                assertEquals(setOf("screen_name", "screen_class"), events.last().second.keySet())
                assertEquals(name, events.last().second.getString(FirebaseAnalytics.Param.SCREEN_NAME))
                assertEquals(name, events.last().second.getString(FirebaseAnalytics.Param.SCREEN_CLASS))
            }
        }
        val rate = SdkAnalytics.rateParameters(5L, 1700000000000L)
        assertEquals(setOf("launchCount", "firstLaunchDate"), rate.keySet())
        assertEquals(5L, rate.getLong("launchCount"))
        assertEquals(1700000000000L, rate.getLong("firstLaunchDate"))
        assertNotNull(FirebasePerformance.getInstance())
    }
}
