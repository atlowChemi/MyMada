package atlow.chemi.mymada.integrations

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.material.textfield.TextInputLayout
import atlow.chemi.mymada.R
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class IntegrationFieldErrorTest {
    @get:Rule val compose = createComposeRule()

    private fun requestErrors(language: String, scale: Float) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
            fontScale = scale
        }
        val context = androidx.appcompat.view.ContextThemeWrapper(base.createConfigurationContext(config), R.style.AppTheme)
        var confirmations = 0
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides config,
                LocalDensity provides Density(base.resources.displayMetrics.density, scale),
                LocalLayoutDirection provides if (language == "he") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                IntegrationTheme { ActivationRequestScreen(false) { _, _, _ -> confirmations++ } }
            }
        }
        compose.onNodeWithTag("activation-request-submit").performClick()
        listOf(R.string.from, R.string.name, R.string.note2).forEachIndexed { index, label ->
            compose.onNodeWithTag("activation-request-$index").performScrollTo()
            onView(withHint(context.getString(label))).check { view, exception ->
                if (exception != null) throw exception
                val layout = view.parent.parent as TextInputLayout
                val expanded = TextInputLayout::class.java.getDeclaredMethod("isHintExpanded").apply { isAccessible = true }
                assertFalse("Empty invalid input must float its label", expanded.invoke(layout) as Boolean)
                assertNotNull(layout.error)
            }
        }
        repeat(3) { compose.onRoot().performTouchInput { swipeUp() } }
        onView(withText(context.getString(R.string.emailMsgNone))).check(matches(isDisplayed()))
        onView(withHint(context.getString(R.string.note2))).check { view, _ ->
            val visible = android.graphics.Rect()
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            assertTrue(view.getGlobalVisibleRect(visible))
            assertEquals("Notes underline must be reachable", location[1] + view.height, visible.bottom)
        }
        compose.onNodeWithTag("activation-request-0").performScrollTo().performTextReplacement("bad")
        onView(withText(context.getString(R.string.emailInvalid))).check(matches(isDisplayed()))
        compose.onNodeWithTag("activation-request-0").performTextReplacement("fixture@example.org")
        onView(withText(context.getString(R.string.emailInvalid))).check(doesNotExist())
        val longNotes = (1..18).joinToString("\n") { "Fixture $it" }
        compose.onNodeWithTag("activation-request-2").performScrollTo().performTextReplacement(longNotes)
        onView(withHint(context.getString(R.string.note2))).check { view, exception ->
            if (exception != null) throw exception
            val edit = view as android.widget.EditText
            assertEquals(longNotes, edit.text.toString())
            assertTrue("Notes retain the expanding editor policy", edit.height >= edit.lineHeight * 18)
        }
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("activation-request-submit").assertIsDisplayed()
        assertEquals(0, confirmations)
    }

    @Test fun englishErrors() = requestErrors("en", 1f)
    @Test fun hebrewErrors() = requestErrors("he", 1f)
    @Test fun largeEnglishErrors() = requestErrors("en", 2f)
    @Test fun largeHebrewErrors() = requestErrors("he", 2f)

    @Test fun nativeEditsRespectControlledValuesAndExposeChatErrorSemantics() {
        var value by mutableStateOf("")
        compose.setContent {
            val context = androidx.appcompat.view.ContextThemeWrapper(LocalContext.current, R.style.AppTheme)
            CompositionLocalProvider(LocalContext provides context) { IntegrationTheme {
                IntegrationField(value, { if (it.length <= 4) value = it }, label = "Fixture",
                    error = if (value.isEmpty()) "Required" else null)
            } }
        }
        compose.onNodeWithText("Required").assertIsDisplayed()
        onView(withHint("Fixture")).perform(androidx.test.espresso.action.ViewActions.replaceText("12345"))
        compose.waitForIdle()
        onView(withHint("Fixture")).check(matches(withText("")))
        onView(withHint("Fixture")).perform(androidx.test.espresso.action.ViewActions.replaceText("1234"))
        compose.waitForIdle()
        assertEquals("1234", value)
        compose.onNodeWithText("Required").assertDoesNotExist()
    }

    @Test fun nativeTypingIsNotReplayedFromAnOlderComposition() {
        var value by mutableStateOf("")
        compose.setContent {
            val context = androidx.appcompat.view.ContextThemeWrapper(LocalContext.current, R.style.AppTheme)
            CompositionLocalProvider(LocalContext provides context) {
                IntegrationTheme { IntegrationField(value, { value = it }, label = "Fixture") }
            }
        }
        var editor: android.widget.EditText? = null
        onView(withHint("Fixture")).check { view, _ -> editor = view as android.widget.EditText }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        compose.mainClock.autoAdvance = false
        try {
            for (letter in "bad") {
                instrumentation.runOnMainSync { editor!!.append(letter.toString()) }
                instrumentation.waitForIdleSync()
                instrumentation.runOnMainSync { assertEquals(value, editor!!.text.toString()) }
                compose.mainClock.advanceTimeByFrame()
            }
        } finally {
            compose.mainClock.autoAdvance = true
        }
        compose.waitForIdle()
        assertEquals("bad", value)
        onView(withHint("Fixture")).check(matches(withText("bad")))
    }

    @Test fun formErrorsAndReadOnlyCodesKeepValidationAndClearBillingContract() {
        var fields by mutableStateOf(List(9) { "" })
        var previews = 0
        var picks = 0
        compose.setContent {
            val context = androidx.appcompat.view.ContextThemeWrapper(LocalContext.current, R.style.AppTheme)
            CompositionLocalProvider(LocalContext provides context) { IntegrationTheme {
                FormScreen(fields, { index, text -> fields = fields.toMutableList().also { it[index] = text } },
                    { picks++; fields = fields.toMutableList().also { it[8] = "2000" } }, { previews++ })
            } }
        }
        compose.onNodeWithTag("form-send").performClick()
        assertEquals(0, previews)
        for ((index, value) in mapOf(0 to "123", 3 to "456", 4 to "789", 6 to "Fixture", 7 to "Patient")) {
            compose.onNodeWithTag("form-field-$index").performScrollTo().performTextReplacement(value)
        }
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("form-codes").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, picks) }
        onView(withHint(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.pickC))).check { view, exception ->
            if (exception != null) throw exception
            assertNull("The picker field must not accept keyboard/accessibility text edits", (view as android.widget.EditText).keyListener)
        }
        compose.onNodeWithTag("form-send").performClick()
        assertEquals(0, previews)
        compose.onNodeWithTag("form-field-5").performScrollTo().assert(
            SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.Error,
                InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.err_sum)))
        compose.onNodeWithTag("form-field-4").performScrollTo().performTextReplacement("")
        compose.onNodeWithTag("form-send").performClick()
        assertEquals(1, previews)
    }
}
