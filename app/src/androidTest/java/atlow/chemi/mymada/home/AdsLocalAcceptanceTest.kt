package atlow.chemi.mymada.home

import android.app.Activity
import android.app.Instrumentation
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.InfoActivityKt
import atlow.chemi.mymada.UtillitiesAndTools
import atlow.chemi.mymada.contactChatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Local client contracts only: no HomePage, real SDK initialization/load, or ad click. */
class AdsLocalAcceptanceTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private fun prepare() {
        instrumentation.runOnMainSync { atlow.chemi.mymada.helpers.LocalHelper.setLocale(context, "en") }
        context.getSharedPreferences("Settings", 0).edit()
            .putStringSet("Tools", setOf("oxy")).putBoolean("Mapp", false).putBoolean("realM", true).commit()
    }

    @Test fun injectedCreativeReservesSpaceAndHomeRemainsReachable() {
        prepare()
        val height = mutableStateOf(0)
        val destinations = mutableListOf<String>()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContent { MaterialTheme {
                    HomeScreen(false, {}, {}, { destinations += it }) {
                        if (height.value > 0) Box(Modifier.fillMaxWidth().height(height.value.dp).testTag("local-creative"))
                    }
                } }
            }
            for (size in listOf(0, 50, 90, 0)) {
                compose.runOnIdle { height.value = size }
                compose.onNodeWithTag("homeBanner").assertIsDisplayed().assertHeightIsEqualTo(maxOf(50, size).dp)
                if (size > 0) {
                    val creative = compose.onNodeWithTag("local-creative").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                    val slot = compose.onNodeWithTag("homeBanner").fetchSemanticsNode().boundsInRoot
                    assertTrue(creative.top >= slot.top && creative.bottom <= slot.bottom)
                }
                for ((label, destination) in listOf(atlow.chemi.mymada.R.string.title_activity_info to "inf",
                    atlow.chemi.mymada.R.string.title_activity_contact to "con")) {
                    val before = destinations.size
                    compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(context.getString(label)))
                    compose.onNodeWithText(context.getString(label)).performClick()
                    compose.runOnIdle { assertEquals(before + 1, destinations.size); assertEquals(destination, destinations.last()) }
                }
            }
        }
    }

    @Test fun realCallbacksContinueOnceAndSuppressFinishedOwnerWithoutSdkRequests() {
        prepare()
        ActivityScenario.launch(UtillitiesAndTools::class.java).use { scenario ->
            // Class-matching blocking monitors work on API24 too; onStartActivity overrides do not.
            val monitors = listOf(InfoActivityKt::class.java.name, contactChatActivity::class.java.name).associateWith {
                instrumentation.addMonitor(it, Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null), true)
            }
            fun destinations() = monitors.values.sumOf { it.hits }
            val createdDestinations = mutableListOf<String>()
            val lifecycle = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
            val callback = androidx.test.runner.lifecycle.ActivityLifecycleCallback { activity, stage ->
                if (stage == androidx.test.runner.lifecycle.Stage.PRE_ON_CREATE &&
                    (activity is InfoActivityKt || activity is contactChatActivity)) {
                    createdDestinations += activity.javaClass.name
                }
            }
            instrumentation.runOnMainSync { lifecycle.addLifecycleCallback(callback) }
            try {
                scenario.onActivity { activity ->
                    for ((destination, target) in listOf("inf" to InfoActivityKt::class.java.name,
                        "con" to contactChatActivity::class.java.name)) {
                        for (outcome in listOf("unloaded", "load-failure", "dismiss", "failure")) {
                            var initialized = 0
                            val loads = mutableListOf<InterstitialAdLoadCallback>()
                            val actions = HomeActions(activity, { initialized++ }, { _, _, callback -> loads += callback })
                            assertEquals(1, initialized)
                            assertEquals(1, loads.size)
                            val fake = FakeInterstitial()
                            val loaded = outcome == "dismiss" || outcome == "failure"
                            if (loaded) loads.single().onAdLoaded(fake)
                            if (outcome == "load-failure") loads.single().onAdFailedToLoad(
                                LoadAdError(0, "local fixture", "test", null, null))
                            val before = destinations()
                            val targetBefore = monitors.getValue(target).hits
                            actions.open(destination)
                            if (!loaded) assertEquals(0, fake.shows) else {
                                assertEquals(1, fake.shows)
                                assertEquals(before, destinations())
                                if (outcome == "dismiss") fake.callback!!.onAdDismissedFullScreenContent()
                                else fake.callback!!.onAdFailedToShowFullScreenContent(AdError(0, "local fixture", "test"))
                            }
                            assertEquals(before + 1, destinations())
                            assertEquals(targetBefore + 1, monitors.getValue(target).hits)
                            assertEquals(if (loaded) 2 else 1, loads.size)
                            assertEquals(1, initialized)
                        }
                    }
                    // Contact's non-volunteer continuation is the real local chooser, not navigation.
                    context.getSharedPreferences("Settings", 0).edit().putBoolean("realM", false).commit()
                    assertNull(context.packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency"))
                    for (failure in listOf(false, true)) {
                        val fake = FakeInterstitial()
                        var loaded = false
                        val actions = HomeActions(activity, {}, { _, _, callback ->
                            if (!loaded) { loaded = true; callback.onAdLoaded(fake) }
                        })
                        val before = destinations()
                        actions.open("con")
                        if (failure) fake.callback!!.onAdFailedToShowFullScreenContent(AdError(0, "local fixture", "test"))
                        else fake.callback!!.onAdDismissedFullScreenContent()
                        activity.supportFragmentManager.executePendingTransactions()
                        val prompt = activity.supportFragmentManager.findFragmentByTag("home-action:contact") as HomeActions.ActionPrompt
                        assertTrue(prompt.requireDialog().isShowing)
                        assertEquals(1, activity.supportFragmentManager.fragments.filterIsInstance<HomeActions.ActionPrompt>().size)
                        assertEquals(before, destinations())
                        prompt.dismissNow()
                    }
                    val fake = FakeInterstitial()
                    var loads = 0
                    val actions = HomeActions(activity, {}, { _, _, callback -> loads++; callback.onAdLoaded(fake) })
                    actions.open("inf")
                    val before = destinations()
                    activity.finish()
                    assertTrue(activity.isFinishing)
                    fake.callback!!.onAdDismissedFullScreenContent()
                    fake.callback!!.onAdFailedToShowFullScreenContent(AdError(0, "local fixture", "test"))
                    assertEquals(before, destinations())
                    assertEquals(1, loads)
                    assertTrue("Destinations must be intercepted before activity creation: $createdDestinations",
                        createdDestinations.isEmpty())
                }
            } finally {
                instrumentation.runOnMainSync { lifecycle.removeLifecycleCallback(callback) }
                monitors.values.forEach(instrumentation::removeMonitor)
            }
        }
    }

    private class FakeInterstitial : InterstitialAd() {
        var callback: FullScreenContentCallback? = null
        var shows = 0
        override fun show(activity: Activity) { shows++ }
        override fun getAdUnitId() = "local-only"
        override fun setFullScreenContentCallback(value: FullScreenContentCallback?) { callback = value }
        override fun getFullScreenContentCallback() = callback
        override fun setImmersiveMode(value: Boolean) {}
        override fun getResponseInfo(): ResponseInfo = error("No SDK response in a local fixture")
        override fun setOnPaidEventListener(value: OnPaidEventListener?) {}
        override fun getOnPaidEventListener(): OnPaidEventListener? = null
        override fun getPlacementId() = 0L
        override fun setPlacementId(value: Long) {}
    }
}
