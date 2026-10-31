package atlow.chemi.mymada.integrations

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.codeC
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.gms.tasks.Tasks
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ActivationComposeTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun fourDigitsRestoreAndPreserveLeadingZerosWithoutGrantingAccess() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val preferences = context.getSharedPreferences("Settings", 0)
        preferences.edit().putString(ActivationContract.KEY, "fixture-identity").putBoolean("realM", false).commit()
        instrumentation.runOnMainSync {
            LocalHelper.setLocale(context, InstrumentationRegistry.getArguments().getString("captureLanguage", "en"))
        }
        var submitted: String? = null
        val backend = object : ActivationBackend {
            override fun token() = error("No token operation expected")
            override fun request(payload: Map<String, Any>) = error("No request expected")
            override fun activate(key: String, code: String) = Tasks.forResult(ActivationContract.Result.USED).also {
                assertEquals("fixture-identity", key)
                submitted = code
            }
        }
        ActivityScenario.launch(codeC::class.java).use { scenario ->
            scenario.onActivity { ViewModelProvider(it)[ActivationModel::class.java].backend = backend }
            for ((index, digit) in "0012".withIndex()) compose.onNodeWithTag("activation-digit-$index").performTextReplacement(digit.toString())
            scenario.recreate()
            for ((index, digit) in "0012".withIndex()) compose.onNodeWithTag("activation-digit-$index").assert(
                SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.InputText, androidx.compose.ui.text.AnnotatedString(digit.toString())))
            compose.onNodeWithTag("activation-submit").performClick()
            scenario.onActivity {
                assertEquals("0012", submitted)
                assertFalse(preferences.getBoolean("realM", false))
                assertEquals(atlow.chemi.mymada.R.string.codeUsed, ViewModelProvider(it)[ActivationModel::class.java].state.value!!.message)
            }
        }
    }
}
