package atlow.chemi.mymada.integrations

import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import atlow.chemi.mymada.R
import atlow.chemi.mymada.codeRequest
import atlow.chemi.mymada.contactChatActivity
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.gms.tasks.TaskCompletionSource
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Offline client fixtures only: no Firebase SDK writes, FCM delivery or WhatsApp launch. */
class FirebaseClientLocalAcceptanceTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val preferences = context.getSharedPreferences("Settings", 0)
    private fun main(block: () -> Unit) { instrumentation.runOnMainSync(block); instrumentation.waitForIdleSync() }
    private fun capture(name: String) {
        compose.waitForIdle()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val directory = File(context.getExternalFilesDir(null), "firebase-local").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { image ->
            File(directory, "$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
            image.recycle()
        }
    }

    @Test fun requestTokenFailureRetriesAndPublishesIdentityOnlyAfterAcknowledgement() {
        var token = TaskCompletionSource<String>()
        var write = TaskCompletionSource<String>()
        var requests = 0
        val backend = object : ActivationBackend {
            override fun token() = token.task
            override fun request(payload: Map<String, Any>) = write.task.also {
                requests++
                assertEquals("fixture-token", payload["token"])
                assertEquals("A&B#\nשלום", payload["reason"])
            }
            override fun activate(key: String, code: String) = error("Unexpected activation")
        }
        preferences.edit().putBoolean("Mapp", false).putBoolean("realM", false)
            .putString(ActivationContract.KEY, "prior-identity").commit()
        main { LocalHelper.setLocale(context, "en") }
        ActivityScenario.launch(codeRequest::class.java).use { scenario ->
            compose.onNodeWithTag("activation-request-confirm").performClick()
            lateinit var model: ActivationModel
            scenario.onActivity {
                model = ViewModelProvider(it)[ActivationModel::class.java]
                model.backend = backend
                model.request("fixture@example.org", "Fixture", "A&B#\nשלום")
                assertTrue(model.state.value!!.busy)
                assertEquals(0, requests)
            }
            compose.onNodeWithText(context.getString(R.string.load)).assertIsDisplayed()
            capture("request-token-pending")
            main { token.setException(IllegalStateException("local token failure")) }
            onView(withText(R.string.emailBad)).inRoot(isDialog()).check(matches(isDisplayed()))
            capture("request-token-error")
            assertEquals(0, requests)
            assertEquals("prior-identity", preferences.getString(ActivationContract.KEY, null))
            main {
                model.dismiss()
                token = TaskCompletionSource()
                model.request("fixture@example.org", "Fixture", "A&B#\nשלום")
                token.setResult("fixture-token")
            }
            main {
                assertEquals(1, requests)
                assertTrue(model.state.value!!.busy)
                assertEquals("prior-identity", preferences.getString(ActivationContract.KEY, null))
            }
            capture("request-write-pending")
            main { write.setException(IllegalStateException("local write rejection")) }
            onView(withText(R.string.emailBad)).inRoot(isDialog()).check(matches(isDisplayed()))
            capture("request-write-error")
            main {
                model.dismiss()
                write = TaskCompletionSource()
                model.request("fixture@example.org", "Fixture", "A&B#\nשלום")
            }
            main { assertEquals(2, requests); write.setResult("acknowledged-identity") }
            compose.onNodeWithText(context.getString(R.string.requestGood)).assertIsDisplayed()
            assertEquals("acknowledged-identity", preferences.getString(ActivationContract.KEY, null))
            assertFalse(preferences.getBoolean("realM", false))
            capture("request-acknowledged")
        }
    }

    @Test fun chatFailureRetainsDraftAcrossRecreationAndRendersTypedEventsBeforeRetryAck() {
        val token = TaskCompletionSource<String>()
        var write = TaskCompletionSource<Void>()
        var sends = 0
        var listens = 0
        var removals = 0
        lateinit var changed: (String, Any?) -> Unit
        val backend = object : ChatBackend {
            override fun identity() = token.task
            override fun listen(identity: String, update: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit {
                assertEquals("historical:identity", identity)
                listens++
                changed = update
                return { removals++ }
            }
            override fun send(identity: String, name: String, text: String) = write.task.also {
                assertEquals("historical:identity", identity)
                assertEquals("Fixture Name", name)
                assertEquals("Retry draft שלום", text)
                sends++
            }
            override fun name(identity: String, name: String) = error("Unexpected rename")
        }
        preferences.edit().putString("UserName", "Fixture Name").putBoolean("Mapp", false).commit()
        main { LocalHelper.setLocale(context, "en") }
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is contactChatActivity && stage == Stage.PRE_ON_CREATE)
                ViewModelProvider(activity)[ChatModel::class.java].backend = backend
        }
        main { ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback) }
        try {
            ActivityScenario.launch(contactChatActivity::class.java).use { scenario ->
                compose.onNodeWithTag("chat-dialog-confirm").performClick()
                lateinit var model: ChatModel
                scenario.onActivity { model = ViewModelProvider(it)[ChatModel::class.java] }
                main {
                    assertFalse(model.state.value!!.ready)
                    model.send("too soon")
                    assertEquals(0, sends)
                    token.setResult("historical:identity")
                }
                main {
                    changed("-a", mapOf("sender" to "admin", "msg" to "Admin fixture", "time" to 1700000000000L))
                    changed("-b", mapOf("sender" to "historical:identity", "msg" to "User fixture", "time" to 1700000001000L))
                    changed("SeenByAdmin", false)
                }
                compose.onNodeWithText("Admin fixture").assertIsDisplayed()
                capture("chat-admin")
                compose.onNodeWithTag("chat-messages").performScrollToNode(hasText("User fixture"))
                compose.onNodeWithText("User fixture").assertIsDisplayed()
                compose.onNodeWithText("Fixture Name").assertIsDisplayed()
                compose.onNodeWithTag("chat-seen").assertTextEquals(context.getString(R.string.msgNotSeen))
                capture("chat-user-admin-unseen")
                main {
                    changed("SeenByAdmin", "true")
                    assertNull(model.state.value!!.seen)
                    changed("-malformed", mapOf("sender" to 42, "msg" to "must not render"))
                    assertEquals(2, model.state.value!!.messages.size)
                }
                compose.onNodeWithTag("chat-seen").assertTextEquals("")
                main {
                    changed("SeenByAdmin", true)
                    changed("-c", mapOf("sender" to "admin", "msg" to "WhatsApp fixture", "WhatsApp" to true))
                }
                compose.onNodeWithTag("chat-seen").assertTextEquals(context.getString(R.string.msgSeen))
                compose.onNodeWithTag("chat-messages").performScrollToNode(hasContentDescription("WhatsApp"))
                compose.onNodeWithText("לחץ על כפתור הוואטסאפ בכדי לתקשר איתנו בוואטסאפ.").assertIsDisplayed()
                compose.onNodeWithContentDescription("WhatsApp").assertIsDisplayed()
                capture("chat-whatsapp-seen")
                compose.onNodeWithTag("chat-draft").performTextReplacement("Retry draft שלום")
                compose.onNodeWithTag("chat-send").performClick()
                main { model.send("duplicate"); assertEquals(1, sends); assertEquals(0L, model.state.value!!.sent) }
                scenario.recreate()
                scenario.onActivity { assertSame(model, ViewModelProvider(it)[ChatModel::class.java]) }
                compose.onNodeWithTag("chat-draft").assertTextContains("Retry draft שלום")
                capture("chat-send-pending-recreated")
                main { write.setException(IllegalStateException("local send rejected")) }
                compose.onNodeWithText(context.getString(R.string.emailBad)).assertIsDisplayed()
                capture("chat-send-error")
                compose.onNodeWithText(context.getString(R.string.ok)).performClick()
                scenario.recreate()
                compose.onNodeWithTag("chat-draft").assertTextContains("Retry draft שלום")
                main { assertEquals(0L, model.state.value!!.sent); write = TaskCompletionSource() }
                compose.onNodeWithTag("chat-send").performClick()
                main { assertEquals(2, sends); assertEquals(1, listens); write.setResult(null) }
                compose.onNodeWithTag("chat-draft").assert(SemanticsMatcher.expectValue(
                    androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
                main { assertEquals(1L, model.state.value!!.sent); assertFalse(model.state.value!!.error) }
                compose.onNodeWithText(context.getString(R.string.emailBad)).assertDoesNotExist()
                capture("chat-retry-acknowledged")
            }
            assertEquals(1, removals)
        } finally { main { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) } }
    }
}
