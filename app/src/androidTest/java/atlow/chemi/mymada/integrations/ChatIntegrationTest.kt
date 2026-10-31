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
import java.io.File

class ChatIntegrationTest {
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
        var identity: String? = null
        lateinit var changed: (String, Any?) -> Unit
        lateinit var failed: () -> Unit
        override fun identity() = token.task
        override fun listen(identity: String, changed: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit {
            this.identity = identity; this.changed = changed; this.failed = failed; listens++
            return { removals++ }
        }
        override fun send(identity: String, name: String, text: String) = write.task.also { assertEquals(this.identity, identity); sends++ }
        override fun name(identity: String, name: String) = rename.task.also { assertEquals(this.identity, identity) }
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
                capture("disclaimer")
                onView(withText(R.string.ok)).perform(click())
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
                    it.findViewById<EditText>(R.id.chatT).setText("Pending fixture")
                    it.findViewById<android.widget.Button>(R.id.sendChat).performClick()
                }
                scenario.recreate()
                scenario.onActivity {
                    assertSame(retained, ViewModelProvider(it)[ChatModel::class.java])
                    assertEquals("Pending fixture", it.findViewById<EditText>(R.id.chatT).text.toString())
                    assertEquals(1, backend.sends); assertEquals(1, backend.listens)
                }
                main { backend.write.setResult(null); backend.changed("SeenByAdmin", true) }
                scenario.onActivity {
                    assertEquals("", it.findViewById<EditText>(R.id.chatT).text.toString())
                    assertEquals(it.getString(R.string.msgSeen), it.findViewById<TextView>(R.id.msgSeen).text.toString())
                    it.askForName(true)
                }
                capture("name")
                scenario.onActivity {
                    val dialog = (it.supportFragmentManager.findFragmentByTag("chat-name") as androidx.fragment.app.DialogFragment).requireDialog() as androidx.appcompat.app.AlertDialog
                    val field = dialog.findViewById<EditText>(R.id.shabatT)!!
                    field.setText("123")
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick()
                    assertEquals(it.getString(R.string.chatNameNum), dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.shabatTil)!!.error.toString())
                    field.setText("Renamed Fixture")
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick()
                    assertEquals("Fixture Name", retained.name)
                }
                scenario.recreate()
                main { backend.rename.setResult(null) }
                scenario.onActivity { assertEquals("Renamed Fixture", retained.name) }
            }
            assertEquals(1, backend.removals)
        } finally { main { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) } }
    }
    private fun capture(name: String) {
        instrumentation.waitForIdleSync()
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val dir = File(context.getExternalFilesDir(null), "chat").apply { mkdirs() }
        val language = context.getSharedPreferences("Settings", 0).getString("lng", "en")
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            File(dir, "$name-$language.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
}
