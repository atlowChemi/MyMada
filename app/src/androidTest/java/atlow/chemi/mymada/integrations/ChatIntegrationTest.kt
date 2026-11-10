package atlow.chemi.mymada.integrations

import android.app.Application
import android.graphics.Bitmap
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.contactChatActivity
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import com.google.android.gms.tasks.TaskCompletionSource
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import java.io.File

class ChatIntegrationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private fun main(block: () -> Unit) { instrumentation.runOnMainSync(block); instrumentation.waitForIdleSync() }
    private class Backend : ChatBackend {
        var token = TaskCompletionSource<String>()
        var write = TaskCompletionSource<Void>()
        var rename = TaskCompletionSource<Void>()
        var listens = 0
        var removals = 0
        var sends = 0
        var renames = 0
        var identity: String? = null
        lateinit var changed: (String, Any?) -> Unit
        lateinit var failed: () -> Unit
        override fun identity() = token.task
        override fun listen(identity: String, changed: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit {
            this.identity = identity; this.changed = changed; this.failed = failed; listens++
            return { removals++ }
        }
        override fun send(identity: String, name: String, text: String) = write.task.also { assertEquals(this.identity, identity); sends++ }
        override fun name(identity: String, name: String) = rename.task.also { assertEquals(this.identity, identity); renames++ }
    }
    @Test fun keyboardKeepsHeaderAndWholeSendVisibleWithoutWrites() {
        val backend = Backend()
        context.getSharedPreferences("Settings", 0).edit().putString("UserName", "Fixture Name").commit()
        main { LocalHelper.setLocale(context, InstrumentationRegistry.getArguments().getString("captureLanguage") ?: "en") }
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is contactChatActivity && stage == Stage.PRE_ON_CREATE)
                ViewModelProvider(activity)[ChatModel::class.java].backend = backend
        }
        main { ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback) }
        try {
            ActivityScenario.launch(contactChatActivity::class.java).use { scenario ->
                main { backend.token.setResult("offline-keyboard-fixture") }
                compose.onNodeWithTag("chat-dialog-confirm").performClick()
                compose.onNodeWithTag("chat-draft").performTextReplacement("Pending fixture")
                fun checkKeyboard() {
                    compose.waitUntil(5000) {
                        var focused = false
                        scenario.onActivity { focused = it.hasWindowFocus() }
                        focused
                    }
                    compose.onNodeWithTag("chat-draft").performClick()
                    compose.waitUntil(5000) {
                        var visible = false
                        scenario.onActivity { visible = androidx.core.view.ViewCompat.getRootWindowInsets(it.window.decorView)
                            ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == true }
                        visible
                    }
                    compose.waitForIdle()
                    val send = compose.onNodeWithTag("chat-send").fetchSemanticsNode().boundsInRoot
                    val name = compose.onNodeWithText(LocalHelper.onAttach(context).getString(R.string.changeName))
                        .fetchSemanticsNode().boundsInRoot
                    scenario.onActivity { activity ->
                        assertTrue("Send must retain its full minimum height, not a clipped semantics sliver",
                            send.height >= 60 * activity.resources.displayMetrics.density - 1)
                        val frame = android.graphics.Rect()
                        activity.window.decorView.getWindowVisibleDisplayFrame(frame)
                        val location = IntArray(2)
                        activity.findViewById<android.view.View>(R.id.chat_content).getLocationOnScreen(location)
                        for (bounds in listOf(send, name)) {
                            assertTrue("Whole control must remain above IME: $bounds / $frame", bounds.bottom + location[1] <= frame.bottom)
                            assertTrue("Header must not pan above visible frame", bounds.top + location[1] >= frame.top)
                            assertTrue(bounds.width > 0 && bounds.height > 0)
                        }
                        val toolbar = activity.findViewById<android.view.View>(R.id.toolbar)
                        toolbar.getLocationOnScreen(location)
                        assertTrue("Toolbar must remain visible", location[1] >= frame.top && location[1] + toolbar.height <= frame.bottom)
                    }
                    capture("keyboard-visible")
                }
                checkKeyboard()
                androidx.test.espresso.Espresso.closeSoftKeyboard()
                // Do not recreate while the old window's asynchronous IME hide is pending.
                compose.waitUntil(5000) {
                    var hidden = false
                    scenario.onActivity { hidden = androidx.core.view.ViewCompat.getRootWindowInsets(it.window.decorView)
                        ?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == false }
                    hidden
                }
                compose.onNodeWithTag("chat-draft").assertTextContains("Pending fixture")
                capture("keyboard-hidden")
                scenario.recreate()
                compose.onNodeWithTag("chat-draft").assertTextContains("Pending fixture")
                checkKeyboard()
                assertEquals(0, backend.sends)
                assertEquals(0, backend.renames)
            }
        } finally { main { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) } }
    }
    @Test fun tokenFailureRetriesAndCachedConversationSurvivesRefresh() {
        val backend = Backend()
        val store = ViewModelStore()
        lateinit var model: ChatModel
        main {
            model = ChatModel(context.applicationContext as Application).apply { this.backend = backend }
            store.put("chat", model)
            model.connect(); backend.token.setException(IllegalStateException("fixture token failure"))
        }
        main {
            assertTrue(model.state.value!!.error); assertEquals(0, backend.listens)
            backend.token = TaskCompletionSource()
            model.connect(); backend.token.setResult("empty")
        }
        main {
            assertTrue(model.state.value!!.error); assertEquals(0, backend.listens)
            backend.token = TaskCompletionSource()
            model.connect(); backend.token.setResult("valid-token")
        }
        main {
            assertEquals("valid-token", backend.identity); store.clear()
            context.getSharedPreferences("Settings", 0).edit().remove("chatIdentity").putString("fcm", "old-token").commit()
            MyFirebaseMessagingService.saveToken(context, "new-delivery-token")
            val cached = FirebaseChatBackend(context.applicationContext as Application).identity()
            assertTrue(cached.isComplete); assertEquals("old-token", cached.result)
        }
    }
    @Test fun awaitsIdentityHandlesLiveSeenUpdatesRetryAndListenerCleanup() {
        val backend = Backend()
        val store = ViewModelStore()
        lateinit var model: ChatModel
        context.getSharedPreferences("Settings", 0).edit().putString("UserName", "Original").commit()
        main {
            model = ChatModel(context.applicationContext as Application).apply { this.backend = backend }
            store.put("chat", model)
            model.connect(); model.connect(); model.send("too soon")
            assertEquals(0, backend.listens); assertEquals(0, backend.sends)
            backend.token.setResult("historical-token")
        }
        main {
            assertEquals(1, backend.listens)
            backend.changed("Name", "Original")
            backend.changed("-a", mapOf("sender" to "admin", "msg" to "hello", "time" to 123L))
            backend.changed("-a", mapOf("sender" to "admin", "msg" to "edited", "time" to 123L))
            assertEquals(1, model.state.value!!.messages.size)
            assertEquals("edited", model.state.value!!.messages.single().text)
            backend.changed("SeenByAdmin", false); assertEquals(false, model.state.value!!.seen)
            backend.changed("SeenByAdmin", true); assertEquals(true, model.state.value!!.seen)
            model.changeName("Replacement")
            assertEquals("Original", model.name)
            backend.rename.setException(IllegalStateException("fixture rejection"))
        }
        main {
            assertEquals("Original", model.name)
            backend.rename = TaskCompletionSource()
            model.changeName("Replacement"); backend.rename.setResult(null)
        }
        main {
            assertEquals("Replacement", model.name)
            model.send("fixture"); model.send("duplicate")
            assertEquals(1, backend.sends)
            backend.write.setException(IllegalStateException("fixture rejection"))
        }
        main {
            assertEquals(0L, model.state.value!!.sent)
            assertTrue(model.state.value!!.error)
            backend.write = TaskCompletionSource()
            model.send("retry"); backend.write.setResult(null)
        }
        main {
            assertEquals(1L, model.state.value!!.sent)
            backend.changed("-a", null); assertTrue(model.state.value!!.messages.isEmpty())
            backend.failed(); assertFalse(model.state.value!!.ready)
            model.connect()
        }
        main { assertEquals(2, backend.listens); store.clear(); assertEquals(2, backend.removals) }
    }
    @Test fun recreationRetainsPendingSendAndPublishedChatLayout() {
        val backend = Backend()
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putString("UserName", "Fixture Name").putBoolean("Mapp", false).commit()
        main { LocalHelper.setLocale(context, InstrumentationRegistry.getArguments().getString("captureLanguage") ?: "en") }
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is contactChatActivity && stage == Stage.PRE_ON_CREATE) {
                ViewModelProvider(activity)[ChatModel::class.java].backend = backend
            }
        }
        main { ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback) }
        try {
            val route = PushRoute.parse(mapOf("type" to "newChat", "msg" to "Fixture notification"))!!
            val intent = MyFirebaseMessagingService.destination(context, route)!!
            ActivityScenario.launch<contactChatActivity>(intent).use { scenario ->
                scenario.onActivity { assertEquals(it.getString(R.string.title_activity_contact), it.supportActionBar?.title.toString()) }
                capture("disclaimer")
                compose.onNodeWithTag("chat-dialog-confirm").assertIsDisplayed().performClick()
                main { backend.token.setResult("historical-token") }
                capture("empty")
                main {
                    backend.changed("SeenByAdmin", false)
                    backend.changed("-a", mapOf("sender" to "admin", "msg" to "Fixture reply / תשובת בדיקה", "time" to 1700000000000L))
                    backend.changed("-b", mapOf("sender" to "historical-token", "msg" to "Fixture question / שאלת בדיקה", "time" to 1700000001000L))
                }
                capture("messages")
                // ActivityScenario mutates its launch Intent with CLEAR_TASK; real FCM does not.
                android.app.PendingIntent.getActivity(context, 987, MyFirebaseMessagingService.destination(context, route),
                    android.app.PendingIntent.FLAG_CANCEL_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE).send()
                instrumentation.waitForIdleSync()
                assertEquals(1, backend.listens)
                lateinit var retained: ChatModel
                scenario.onActivity {
                    retained = ViewModelProvider(it)[ChatModel::class.java]
                }
                compose.onNodeWithTag("chat-draft").performTextReplacement("Pending fixture")
                compose.onNodeWithTag("chat-send").performClick()
                scenario.recreate()
                compose.onNodeWithTag("chat-draft").assertTextContains("Pending fixture")
                scenario.onActivity {
                    assertSame(retained, ViewModelProvider(it)[ChatModel::class.java])
                    assertEquals(1, backend.sends); assertEquals(1, backend.listens)
                }
                main { backend.write.setResult(null); backend.changed("SeenByAdmin", true) }
                compose.onNodeWithTag("chat-draft").assert(SemanticsMatcher.expectValue(
                    androidx.compose.ui.semantics.SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
                compose.onNodeWithTag("chat-seen").assertTextEquals(LocalHelper.onAttach(context).getString(R.string.msgSeen))
                scenario.onActivity {
                    it.askForName(true)
                }
                capture("name")
                compose.onNodeWithTag("chat-name-draft").performScrollTo().performTextReplacement("123")
                compose.onNodeWithTag("chat-dialog-confirm").assertIsDisplayed().performClick()
                compose.onNodeWithText(LocalHelper.onAttach(context).getString(R.string.chatNameNum)).assertIsDisplayed()
                compose.onNodeWithTag("chat-name-draft").performScrollTo().performTextReplacement("Renamed Fixture")
                compose.onNodeWithTag("chat-dialog-confirm").assertIsDisplayed().performClick()
                scenario.onActivity { assertEquals("Fixture Name", retained.name) }
                scenario.recreate()
                main { backend.rename.setResult(null) }
                scenario.onActivity { assertEquals("Renamed Fixture", retained.name) }
            }
            assertEquals(1, backend.removals)
        } finally { main { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) } }
    }
    @Test fun blankNameOpensAboveDisclaimerAndRestoresOrder() {
        val backend = Backend()
        context.getSharedPreferences("Settings", 0).edit().remove("UserName").putBoolean("Mapp", false).commit()
        main { LocalHelper.setLocale(context, InstrumentationRegistry.getArguments().getString("captureLanguage") ?: "en") }
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is contactChatActivity && stage == Stage.PRE_ON_CREATE) {
                ViewModelProvider(activity)[ChatModel::class.java].backend = backend
            }
        }
        main { ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback) }
        try {
            ActivityScenario.launch(contactChatActivity::class.java).use { scenario ->
                main { backend.token.setResult("offline-first-open-fixture") }
                fun assertOrder() {
                    // Compose layout can precede WindowManager's focus transfer,
                    // both on first show and after restoring the two dialogs.
                    compose.waitUntil(5_000) {
                        var focused = false
                        scenario.onActivity { activity ->
                            val dialog = activity.supportFragmentManager.findFragmentByTag("chat-name")
                                as? contactChatActivity.ChatDialog
                            focused = dialog?.dialog?.window?.decorView?.hasWindowFocus() == true
                        }
                        focused
                    }
                    compose.onNodeWithTag("chat-name-draft").assertIsDisplayed()
                    scenario.onActivity { activity ->
                        val dialogs = activity.supportFragmentManager.fragments.filterIsInstance<contactChatActivity.ChatDialog>()
                        assertEquals(listOf("chat-disclaimer", "chat-name"), dialogs.map { it.tag })
                        assertTrue(dialogs.last().requireDialog().window!!.decorView.hasWindowFocus())
                        assertFalse(dialogs.last().isCancelable)
                    }
                }
                assertOrder()
                capture("first-open-name")
                compose.onNodeWithTag("chat-name-draft").performTextReplacement("First Fixture")
                scenario.recreate()
                assertOrder()
                compose.onNodeWithTag("chat-name-draft").assertTextContains("First Fixture")
                compose.onNode(hasTestTag("chat-dialog-confirm") and hasAnyAncestor(hasTestTag("chat-name-dialog")))
                    .assertIsDisplayed().performClick()
                main { backend.rename.setResult(null) }
                compose.onNodeWithTag("chat-name-draft").assertDoesNotExist()
                compose.onNodeWithTag("chat-disclaimer-dialog").assertIsDisplayed()
                capture("first-open-disclaimer")
                compose.onNodeWithTag("chat-dialog-confirm").assertIsDisplayed().performClick()
                compose.onNodeWithTag("chat-disclaimer-dialog").assertDoesNotExist()
                scenario.recreate()
                compose.onNodeWithTag("chat-name-draft").assertDoesNotExist()
                compose.onNodeWithTag("chat-disclaimer-dialog").assertDoesNotExist()
                assertEquals(0, backend.sends)
            }
        } finally { main { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) } }
    }
    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val dir = File(context.getExternalFilesDir(null), "chat").apply { mkdirs() }
        val language = context.getSharedPreferences("Settings", 0).getString("lng", "en")
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            if (name == "empty" && android.os.Build.VERSION.SDK_INT >= 35)
                assertEquals(context.getColor(R.color.ColorPrimary), bitmap.getPixel(bitmap.width / 2, 2))
            File(dir, "$name-$language.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
