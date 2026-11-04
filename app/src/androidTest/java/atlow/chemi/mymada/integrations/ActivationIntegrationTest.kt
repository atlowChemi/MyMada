package atlow.chemi.mymada.integrations

import android.app.Application
import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.*
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import com.google.android.gms.tasks.TaskCompletionSource
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Run only on the owned offline emulator. All backend outcomes here are fixtures, not live delivery. */
class ActivationIntegrationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)
    private class Backend : ActivationBackend {
        var tokenResult = TaskCompletionSource<String>()
        var requestResult = TaskCompletionSource<String>()
        var activationResult = TaskCompletionSource<ActivationContract.Result>()
        var requests = 0
        var activations = 0
        var payload: Map<String, Any>? = null
        var identity: String? = null
        override fun token() = tokenResult.task
        override fun request(payload: Map<String, Any>) = requestResult.task.also { requests++; this.payload = payload }
        override fun activate(key: String, code: String) = activationResult.task.also { activations++; identity = key }
    }
    private fun main(action: () -> Unit) { instrumentation.runOnMainSync(action); instrumentation.waitForIdleSync() }
    private fun prepare() {
        preferences.edit().putBoolean("realM", false).putBoolean("Mapp", false)
            .remove(ActivationContract.KEY).remove("fcm").remove("chatIdentity").commit()
        main { LocalHelper.setLocale(context, "en") }
    }

    @Test fun requestAwaitsTokenAndAcknowledgementAndAllowsRetry() {
        prepare()
        val backend = Backend()
        lateinit var model: ActivationModel
        main {
            model = ActivationModel(context.applicationContext as Application).apply { this.backend = backend }
            preferences.edit().putString(ActivationContract.KEY, "old-identity").commit()
            model.request("a@example.org", "שם", "& #\nfixture")
            model.request("duplicate@example.org", "duplicate", "duplicate")
            assertTrue(model.state.value!!.busy)
            assertEquals(0, backend.requests)
            backend.tokenResult.setResult("fresh-token")
        }
        main {
            assertEquals(1, backend.requests)
            assertEquals("fresh-token", backend.payload!!["token"])
            assertEquals("old-identity", preferences.getString(ActivationContract.KEY, ""))
            backend.requestResult.setException(IllegalStateException("fixture write rejected"))
        }
        main {
            assertFalse(model.state.value!!.busy)
            assertEquals(R.string.emailBad, model.state.value!!.message)
            assertEquals("old-identity", preferences.getString(ActivationContract.KEY, ""))
            backend.requestResult = TaskCompletionSource()
            model.request("a@example.org", "שם", "retry")
        }
        main { backend.requestResult.setResult("new-identity") }
        main {
            assertEquals("new-identity", preferences.getString(ActivationContract.KEY, ""))
            assertEquals(R.string.requestGood, model.state.value!!.message)
        }
    }

    @Test fun activationDoesNotGrantBeforeAckAndRotationDoesNotRepeatWork() {
        prepare()
        preferences.edit().putString(ActivationContract.KEY, "existing-identity").commit()
        val backend = Backend()
        ActivityScenario.launch(codeC::class.java).use { scenario ->
            lateinit var original: Activity
            lateinit var retained: ActivationModel
            scenario.onActivity {
                original = it
                val model = ViewModelProvider(it)[ActivationModel::class.java]
                retained = model
                model.backend = backend
                model.activate("0012", null)
                assertFalse(preferences.getBoolean("realM", false))
                assertEquals("existing-identity", backend.identity)
            }
            // ActivityScenario.recreate waits for an idle queue; API24's indeterminate
            // progress animation never idles. Observe the actual lifecycle instead.
            val resumed = CountDownLatch(1)
            var recreated: codeC? = null
            val callback = ActivityLifecycleCallback { activity, stage ->
                if (activity is codeC && activity !== original && stage == Stage.RESUMED) {
                    recreated = activity
                    resumed.countDown()
                }
            }
            ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback)
            try {
                instrumentation.runOnMainSync { original.recreate() }
                assertTrue(resumed.await(5, TimeUnit.SECONDS))
                instrumentation.runOnMainSync {
                    assertSame(retained, ViewModelProvider(recreated!!)[ActivationModel::class.java])
                    retained.activate("0012", null)
                    assertEquals(1, backend.activations)
                    backend.activationResult.setException(IllegalStateException("fixture rejection"))
                }
                instrumentation.waitForIdleSync()
            } finally { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) }
            scenario.onActivity {
                val model = ViewModelProvider(it)[ActivationModel::class.java]
                assertFalse(preferences.getBoolean("realM", false))
                assertEquals(R.string.emailBad, model.state.value!!.message)
                backend.activationResult = TaskCompletionSource()
                model.activate("0012", null)
            }
            main { backend.activationResult.setResult(ActivationContract.Result.READY) }
            scenario.onActivity {
                assertTrue(preferences.getBoolean("realM", false))
                assertTrue(ViewModelProvider(it)[ActivationModel::class.java].state.value!!.activated)
            }
        }
    }

    @Test fun coldApprovalHasValidatedIntentAndCannotGrantFromPayloadAlone() {
        prepare()
        val route = PushRoute.parse(mapOf("type" to "codeApproval", "code" to "0012"))!!
        val intent = MyFirebaseMessagingService.destination(context, route)!!
        assertEquals("0012", intent.getStringExtra("code"))
        assertNotEquals(intent.action, MyFirebaseMessagingService.destination(context, route.copy(code = "0013"))!!.action)
        val first = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val second = PendingIntent.getActivity(context, 0, MyFirebaseMessagingService.destination(context, route.copy(code = "0013"))!!,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        assertNotEquals(first, second)
        ActivityScenario.launch<codeC>(intent).use { scenario ->
            onView(withId(android.R.id.message)).check(matches(withText(R.string.codeNotExist)))
            assertFalse(preferences.getBoolean("realM", false))
            scenario.recreate()
            onView(withId(android.R.id.message)).check(matches(withText(R.string.codeNotExist)))
        }
        MyFirebaseMessagingService.publish(context, route)
        if (Build.VERSION.SDK_INT >= 26) assertNotNull(context.getSystemService(NotificationManager::class.java).getNotificationChannel("Push"))
        val malformed = Intent(context, codeC::class.java).putExtra("toCode", true).putExtra("code", "abcd")
        ActivityScenario.launch<codeC>(malformed).use {
            onView(withId(android.R.id.message)).check(matches(withText(R.string.codeBad)))
            assertFalse(preferences.getBoolean("realM", false))
        }
        val monitor = instrumentation.addMonitor(codeC::class.java.name, null, false)
        try {
            context.startActivity(Intent(context, HomePage::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("type", "codeApproval").putExtra("code", "0012"))
            val activity = instrumentation.waitForMonitorWithTimeout(monitor, 5000)
            assertNotNull("FCM launcher cold entry must reach activation", activity)
            assertEquals("0012", activity.intent.getStringExtra("code"))
            assertFalse(preferences.getBoolean("realM", false))
            main { activity.finish() }
        } finally { instrumentation.removeMonitor(monitor); first.cancel(); second.cancel() }
    }

    @Test fun tokenRotationPreservesExistingConversationIdentity() {
        prepare()
        preferences.edit().putString("fcm", "historical-conversation").commit()
        MyFirebaseMessagingService.saveToken(context, "fresh-delivery-token")
        MyFirebaseMessagingService.saveToken(context, "second-delivery-token")
        assertEquals("historical-conversation", MyFirebaseMessagingService.chatIdentity(context))
        assertEquals("second-delivery-token", preferences.getString("fcm", ""))
    }

    @Test fun requestValidationAndConfirmationSurviveRecreationWithoutSending() {
        prepare()
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        main { LocalHelper.setLocale(context, language) }
        ActivityScenario.launch(codeRequest::class.java).use { scenario ->
            fun assertLocalizedTitle() = scenario.onActivity { activity ->
                val expected = activity.getString(R.string.title_activity_code)
                assertEquals(expected, activity.title.toString())
                assertEquals(expected, activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).title.toString())
            }
            assertLocalizedTitle()
            compose.onNodeWithTag("activation-request-confirm").performClick()
            closeSoftKeyboard()
            compose.onNodeWithTag("activation-request-submit").performClick()
            compose.onNodeWithText(LocalHelper.onAttach(context).getString(R.string.emailNone)).assertIsDisplayed()
            compose.onNodeWithTag("activation-request-0").performScrollTo().performTextReplacement("fixture@example.org")
            compose.onNodeWithTag("activation-request-1").performScrollTo().performTextReplacement("Fixture Name")
            compose.onNodeWithTag("activation-request-2").performScrollTo().performTextReplacement("A&B#\nשלום")
            closeSoftKeyboard()
            compose.onNodeWithTag("activation-request-submit").performClick()
            scenario.recreate()
            assertLocalizedTitle()
            val text = compose.onNodeWithTag("activation-request-summary").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].single()
            val label = text.text.indexOf(LocalHelper.onAttach(context).getString(R.string.msg))
            assertTrue(text.spanStyles.any { it.start == label && it.item.color == androidx.compose.ui.graphics.Color(0xff238bad) })
            assertTrue(text.text.contains("A&B#\nשלום"))
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val image = instrumentation.uiAutomation.takeScreenshot()
            val directory = File(context.getExternalFilesDir(null), "integrations").apply { mkdirs() }
            File(directory, "confirmation-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
            compose.onNodeWithTag("activation-request-cancel").assertIsDisplayed().performClick()
            compose.onNodeWithTag("activation-request-2").assertTextContains("A&B#\nשלום")
            assertFalse(preferences.contains(ActivationContract.KEY))
        }
    }

    @Test fun capturePublishedLayoutStates() {
        prepare()
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        main { LocalHelper.setLocale(context, language) }
        fun capture(name: String) {
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            val image = instrumentation.uiAutomation.takeScreenshot()
            val directory = File(context.getExternalFilesDir(null), "integrations").apply { mkdirs() }
            File(directory, "$name-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
        ActivityScenario.launch(lockPage::class.java).use { capture("gate") }
        ActivityScenario.launch(codeC::class.java).use { capture("activation") }
        ActivityScenario.launch(codeRequest::class.java).use {
            capture("disclaimer")
            compose.onNodeWithTag("activation-request-confirm").performClick()
            capture("request")
            closeSoftKeyboard()
            capture("request-full")
        }
    }
}
