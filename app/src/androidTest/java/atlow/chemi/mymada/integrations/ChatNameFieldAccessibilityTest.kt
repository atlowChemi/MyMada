package atlow.chemi.mymada.integrations

import android.graphics.Bitmap
import android.widget.EditText
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.contactChatActivity
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class ChatNameFieldAccessibilityTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun fullWidthNativeNameAcceptsTouchTypingAndRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "he")
        val backend = object : ChatBackend {
            override fun identity() = Tasks.forResult("local-name-accessibility")
            override fun listen(identity: String, changed: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit = {}
            override fun send(identity: String, name: String, text: String) = error("No submission permitted")
            override fun name(identity: String, name: String) = error("No rename submission permitted")
        }
        val callback = ActivityLifecycleCallback { activity, stage ->
            if (activity is contactChatActivity && stage == Stage.PRE_ON_CREATE)
                ViewModelProvider(activity)[ChatModel::class.java].backend = backend
        }
        instrumentation.runOnMainSync {
            LocalHelper.setLocale(context, language)
            ActivityLifecycleMonitorRegistry.getInstance().addLifecycleCallback(callback)
        }
        context.getSharedPreferences("Settings", 0).edit().putString("UserName", "Existing Fixture")
            .putBoolean("Mapp", false).commit()
        try {
            ActivityScenario.launch(contactChatActivity::class.java).use { scenario ->
                compose.onNodeWithTag("chat-dialog-confirm").performClick()
                compose.onNodeWithText(LocalHelper.onAttach(context).getString(R.string.changeName)).performClick()
                val field = Espresso.onView(isAssignableFrom(EditText::class.java)).inRoot(isDialog())
                fun dimensions() {
                    val dialog = compose.onNodeWithTag("chat-name-dialog").fetchSemanticsNode().boundsInRoot
                    field.check { view, exception ->
                        if (exception != null) throw exception
                        val density = view.resources.displayMetrics.density
                        assertTrue("Name editor must span dialog, not wrap its short hint: ${view.width}/${dialog.width}",
                            view.width >= dialog.width - 8 * density)
                        assertTrue("Native touch height must be at least 48dp", view.height >= 48 * density - 1)
                    }
                }
                fun capture(phase: String) {
                    instrumentation.uiAutomation.waitForIdle(500, 5000)
                    val image = instrumentation.uiAutomation.takeScreenshot()
                    val scale = context.resources.configuration.fontScale
                    File(context.getExternalFilesDir(null), "chat-name-$language-$scale-$phase.png").outputStream().use {
                        image.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    image.recycle()
                }
                dimensions()
                field.perform(ViewActions.replaceText(""))
                dimensions()
                capture("empty")
                // Tap well away from the hint: the entire field must be usable when empty.
                compose.onNodeWithTag("chat-name-draft").performTouchInput { click(androidx.compose.ui.geometry.Offset(width * .75f, height * .4f)) }
                field.check(androidx.test.espresso.assertion.ViewAssertions.matches(hasFocus()))
                field.perform(ViewActions.typeText("Edited Fixture"))
                compose.onNodeWithTag("chat-name-draft").assertTextContains("Edited Fixture")
                capture("typing")
                Espresso.closeSoftKeyboard()
                scenario.recreate()
                compose.onNodeWithTag("chat-name-draft").assertTextContains("Edited Fixture")
                dimensions()
                field.perform(ViewActions.click())
                // A tap can select a word at large font sizes; append at an explicit native cursor.
                field.check { view, exception ->
                    if (exception != null) throw exception
                    (view as EditText).setSelection(view.text.length)
                }
                field.perform(ViewActions.typeTextIntoFocusedView(" X"))
                compose.onNodeWithTag("chat-name-draft").assertTextContains("Edited Fixture X")
                Espresso.closeSoftKeyboard()
                capture("restored")
            }
        } finally {
            instrumentation.runOnMainSync { ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(callback) }
        }
    }
}
