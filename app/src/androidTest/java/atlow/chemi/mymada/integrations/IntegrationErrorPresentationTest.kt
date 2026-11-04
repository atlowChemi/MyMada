package atlow.chemi.mymada.integrations

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.codeC
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class IntegrationErrorPresentationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun incompleteEntryAndIncorrectCompleteCodeHaveDistinctErrorsAndNeverGrantAccess() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putBoolean("realM", false).putString(ActivationContract.KEY, "fixture-key").commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        var calls = 0
        val backend = object : ActivationBackend {
            override fun token() = error("No token operation expected")
            override fun request(payload: Map<String, Any>) = error("No write expected")
            override fun activate(key: String, code: String) = Tasks.forResult(ActivationContract.Result.INVALID).also {
                calls++
                assertEquals("1234", code)
            }
        }
        ActivityScenario.launch(codeC::class.java).use { scenario ->
            scenario.onActivity { ViewModelProvider(it)[ActivationModel::class.java].backend = backend }
            compose.onNodeWithTag("activation-digit-0").performTextReplacement("1")
            compose.onNodeWithTag("activation-digit-1").performTextReplacement("2")
            compose.onNodeWithTag("activation-submit").performClick()
            onView(withText(R.string.err)).check(matches(isDisplayed()))
            onView(withId(android.R.id.message)).check(matches(withText(R.string.activationCodeIncomplete)))
            assertEquals(0, calls)
            scenario.recreate()
            onView(withId(android.R.id.message)).check(matches(withText(R.string.activationCodeIncomplete)))
            onView(withId(android.R.id.button2)).perform(click())
            compose.onNodeWithTag("activation-digit-2").performTextReplacement("3")
            compose.onNodeWithTag("activation-digit-3").performTextReplacement("4")
            compose.onNodeWithTag("activation-submit").performClick()
            onView(withText(R.string.err)).check(matches(isDisplayed()))
            onView(withId(android.R.id.message)).check(matches(withText(R.string.codeBad)))
            assertEquals(1, calls)
            assertFalse(preferences.getBoolean("realM", false))
            onView(withId(android.R.id.button2)).perform(click())
        }
    }
}
