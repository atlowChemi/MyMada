package atlow.chemi.mymada.privacy

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.PrivacyActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference
import org.json.JSONArray

/** Disposable emulator with IPv4/IPv6 egress blocked; never run against a user's installation. */
class PrivacyScreenTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val preferences = instrumentation.targetContext.getSharedPreferences("Settings", 0)

    @Test fun englishPolicyNavigationAndState() = verifyScreen("en", "Privacy policy", "unable to connect")
    @Test fun hebrewPolicyNavigationAndState() = verifyScreen("he", "מדיניות פרטיות", "לא ניתן להתחבר")

    @Test fun retainedAuthenticPoliciesRenderAndScrollInBothLocales() {
        for ((language, hash) in mapOf(
            "en" to "9efca8dbd6eb632fc6f0027ca1da03f113b41130bad9f9dbbcdcb636b901e9ac",
            "he" to "c9d22a2aa8313cf46451c36f56680e5b79b9b05fd6c00037e85a868f1c286a46")) {
            val bytes = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "policy-$language.html").readBytes()
            assertEquals(hash, java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
                .joinToString("") { "%02x".format(it) })
            instrumentation.runOnMainSync {
                atlow.chemi.mymada.helpers.LocalHelper.setLocale(instrumentation.targetContext, language)
            }
            preferences.edit().putBoolean("Mapp", false).commit()
            ActivityScenario.launch(PrivacyActivity::class.java).use { scenario ->
                waitForBody(scenario, if (language == "en") "unable to connect" else "לא ניתן להתחבר")
                scenario.onActivity {
                    webView(it.window.decorView)!!.loadDataWithBaseURL("file:///android_asset/policy-$language.html",
                        bytes.toString(Charsets.UTF_8), "text/html", "UTF-8", null)
                }
                waitForBody(scenario, if (language == "en") "MADA reports" else "דיווחי מגן דוד אדום")
                val ready = AtomicReference("")
                compose.waitUntil(15_000) {
                    scenario.onActivity {
                        webView(it.window.decorView)!!.evaluateJavascript(
                            "document.readyState==='complete' && document.fonts.status==='loaded'", ready::set)
                    }
                    ready.get() == "true"
                }
                fun capture(phase: String) {
                    val painted = java.util.concurrent.atomic.AtomicBoolean(false)
                    scenario.onActivity {
                        webView(it.window.decorView)!!.postVisualStateCallback(0, object : WebView.VisualStateCallback() {
                            override fun onComplete(requestId: Long) { painted.set(true) }
                        })
                    }
                    compose.waitUntil(5_000) { painted.get() }
                    instrumentation.uiAutomation.waitForIdle(500, 5000)
                    val image = instrumentation.uiAutomation.takeScreenshot()
                    java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "authentic-policy-$language-$phase.png")
                        .outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                    image.recycle()
                }
                capture("top")
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(WebView::class.java))
                    .perform(androidx.test.espresso.action.ViewActions.swipeUp())
                compose.waitUntil(5_000) {
                    var scrolled = false
                    scenario.onActivity { scrolled = webView(it.window.decorView)!!.scrollY > 0 }
                    scrolled
                }
                val bottom = AtomicReference("")
                compose.waitUntil(5_000) {
                    scenario.onActivity {
                        webView(it.window.decorView)!!.evaluateJavascript(
                            "window.scrollTo(0,document.body.scrollHeight);" +
                                "(()=>{const e=document.querySelector('a[href=\"mailto:atlowapps@gmail.com\"]');" +
                                "const r=e.getBoundingClientRect();return e.innerText==='atlowapps@gmail.com'&&r.top>=0&&r.bottom<=innerHeight})()",
                            bottom::set)
                    }
                    bottom.get() == "true"
                }
                waitForBody(scenario, if (language == "en") "Contact Us" else "יצירת קשר")
                capture("bottom")
            }
        }
    }

    @Test fun localLoadedPolicyScrollsAndNavigatesWithinTheRealHost() {
        instrumentation.runOnMainSync {
            atlow.chemi.mymada.helpers.LocalHelper.setLocale(instrumentation.targetContext, "en")
        }
        preferences.edit().putBoolean("Mapp", false).commit()
        ActivityScenario.launch(PrivacyActivity::class.java).use { scenario ->
            waitForBody(scenario, "unable to connect")
            // Synthetic content proves client behavior, not the authenticity of the remote policy.
            val html = "<html><meta name='viewport' content='width=device-width,initial-scale=1'><body>" +
                "<h1>Synthetic local policy</h1>" +
                (1..60).joinToString("") { "<p>Local policy fixture paragraph $it. Scroll acceptance content.</p>" } +
                "<a id='end' href='err.html?lng=en'>End of local policy</a></body></html>"
            scenario.onActivity {
                webView(it.window.decorView)!!.loadDataWithBaseURL("file:///android_asset/local-policy.html",
                    html, "text/html", "UTF-8", null)
            }
            waitForBody(scenario, "Synthetic local policy")
            androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom(WebView::class.java))
                .perform(androidx.test.espresso.action.ViewActions.swipeUp())
            compose.waitUntil(5_000) {
                var scrolled = false
                scenario.onActivity { scrolled = webView(it.window.decorView)!!.scrollY > 0 }
                scrolled
            }
            val bottom = AtomicReference("")
            scenario.onActivity {
                webView(it.window.decorView)!!.evaluateJavascript("window.scrollTo(0,document.body.scrollHeight)", null)
            }
            compose.waitUntil(5_000) {
                scenario.onActivity {
                    webView(it.window.decorView)!!.evaluateJavascript(
                        "document.getElementById('end').getBoundingClientRect().bottom <= innerHeight", bottom::set)
                }
                bottom.get() == "true"
            }
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val image = instrumentation.uiAutomation.takeScreenshot()
            java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "privacy-local-loaded-bottom.png")
                .outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
            scenario.onActivity {
                webView(it.window.decorView)!!.evaluateJavascript("document.getElementById('end').click()", null)
            }
            waitForBody(scenario, "unable to connect")
            scenario.recreate()
            waitForBody(scenario, "unable to connect")
        }
    }

    private fun verifyScreen(language: String, title: String, error: String) {
        instrumentation.runOnMainSync {
            atlow.chemi.mymada.helpers.LocalHelper.setLocale(instrumentation.targetContext, language)
        }
        preferences.edit().putString("lng", language).putBoolean("privacy", false)
            .putBoolean("Mapp", true).commit()
        assertTrue(policyUrl(language).contains(if (language == "en") "/privacy-en.html?" else "/privacy.html?"))
        ActivityScenario.launch(PrivacyActivity::class.java).use { scenario ->
            var back = ""
            var settings = ""
            var detection = ""
            scenario.onActivity {
                assertEquals("atlow.chemi.mymada.PrivacyActivity", it.componentName.className)
                assertEquals(if (language == "en") View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL,
                    it.resources.configuration.layoutDirection)
                back = it.getString(androidx.appcompat.R.string.abc_action_bar_up_description)
                settings = it.getString(R.string.action_settings)
                detection = it.getString(R.string.pref_title_enable)
            }
            compose.onNodeWithText(title).assertIsDisplayed()
            val toolbar = compose.onNodeWithTag("information-toolbar").fetchSemanticsNode().boundsInRoot
            scenario.onActivity { assertEquals(56 * it.resources.displayMetrics.density, toolbar.height, 1f) }
            compose.onNodeWithContentDescription(detection).assertIsOn().performClick().assertIsOff()
            assertFalse(preferences.getBoolean("Mapp", true))
            compose.waitUntil(30_000) { preferences.getBoolean("privacy", false) }
            waitForBody(scenario, error)
            verifyViewport(scenario)
            val backX = compose.onNodeWithContentDescription(back).fetchSemanticsNode().boundsInRoot.center.x
            val settingsX = compose.onNodeWithContentDescription(settings).fetchSemanticsNode().boundsInRoot.center.x
            assertEquals(language == "en", backX < settingsX)

            // Exercise rendered HTML and an in-WebView link without contacting the policy backend.
            preferences.edit().putBoolean("privacy", false).commit()
            scenario.onActivity {
                webView(it.window.decorView)!!.loadDataWithBaseURL("file:///android_asset/fixture.html",
                    "<html><body><h1>Local policy fixture</h1><a id='link' href='err.html?lng=$language'>Policy link</a></body></html>",
                    "text/html", "UTF-8", null)
            }
            waitForBody(scenario, "Local policy fixture")
            compose.waitUntil(10_000) { preferences.getBoolean("privacy", false) }
            scenario.onActivity {
                webView(it.window.decorView)!!.evaluateJavascript("document.getElementById('link').click()", null)
            }
            waitForBody(scenario, error)
            scenario.recreate()
            compose.onNodeWithText(title).assertIsDisplayed()
            compose.onNodeWithContentDescription(detection).assertIsOff()
            waitForBody(scenario, error)
            assertTrue(preferences.getBoolean("privacy", false))

            val monitor = instrumentation.addMonitor(SettingsPage::class.java.name, null, false)
            try {
                compose.onNodeWithContentDescription(settings).performClick()
                val settingsActivity = instrumentation.waitForMonitorWithTimeout(monitor, 5_000)
                assertNotNull(settingsActivity)
                // Settings owns first-run permission dialogs; only its navigation is under test here.
                instrumentation.runOnMainSync { settingsActivity!!.finish() }
                compose.waitUntil(10_000) { scenario.state == Lifecycle.State.RESUMED }
                compose.onNodeWithText(title).assertIsDisplayed()
            } finally { instrumentation.removeMonitor(monitor) }
            compose.onNodeWithContentDescription(back).performClick()
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
        ActivityScenario.launch(PrivacyActivity::class.java).use { scenario ->
            compose.waitUntil(5_000) {
                var focused = false
                scenario.onActivity { focused = it.window.decorView.hasWindowFocus() }
                focused
            }
            instrumentation.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK)
            compose.waitUntil(10_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    private fun waitForBody(scenario: ActivityScenario<PrivacyActivity>, text: String) {
        val result = AtomicReference("")
        compose.waitUntil(15_000) {
            // A navigation can discard an in-flight JS callback; query the current document again.
            scenario.onActivity { activity ->
                webView(activity.window.decorView)!!.evaluateJavascript(
                    "document.body ? document.body.innerText : ''", result::set)
            }
            result.get().contains(text)
        }
    }

    private fun verifyViewport(scenario: ActivityScenario<PrivacyActivity>) {
        val geometry = AtomicReference("")
        scenario.onActivity { activity ->
            webView(activity.window.decorView)!!.evaluateJavascript(
                "[innerHeight, devicePixelRatio, document.getElementById('main').getBoundingClientRect().top]",
                geometry::set)
        }
        compose.waitUntil(5_000) { geometry.get().isNotEmpty() }
        val values = JSONArray(geometry.get())
        scenario.onActivity { activity ->
            val view = webView(activity.window.decorView)!!
            // WRAP_CONTENT silently shrinks WebView's HTML viewport even when Compose fills the screen.
            assertEquals(view.height.toDouble(), values.getDouble(0) * values.getDouble(1), 3.0)
            assertEquals(values.getDouble(0) * 0.25, values.getDouble(2), 1.0)
        }
    }

    private fun webView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (index in 0 until view.childCount) {
            webView(view.getChildAt(index))?.let { return it }
        }
        return null
    }
}
