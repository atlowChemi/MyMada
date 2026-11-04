package atlow.chemi.mymada.integrations

import android.graphics.Color
import android.widget.EditText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import atlow.chemi.mymada.codeC
import atlow.chemi.mymada.dictionary.*
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.tools.vitals.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Local client contracts only: no Firebase reads/writes or server acknowledgements. */
class MedicalLocalAcceptanceTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Test fun parsedRemoteRangesRebindExistingInputAndAge() {
        val context = instrumentation.targetContext
        context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false)
            .putStringSet("Tools", emptySet()).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, "en") }
        val ranges = mutableStateOf(nativeVitals)
        // Empty activation host supplies the app theme without starting the live Vitals Fragment.
        ActivityScenario.launch(codeC::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setContentView(ComposeView(activity).apply { setContent { VitalsScreen(ranges.value) } })
            }
            onView(withId(R.id.pulsa)).perform(replaceText("60"), closeSoftKeyboard())
            fun assertPulse(color: Int) {
                compose.waitForIdle()
                scenario.onActivity {
                    val field = it.findViewById<EditText>(R.id.pulsa)
                    assertEquals("60", field.text.toString())
                    assertEquals(color, field.currentTextColor)
                }
            }
            assertPulse(Color.rgb(70, 178, 58))
            scenario.onActivity {
                ranges.value = nativeVitals.map { fallback ->
                    fallback.copy(pulse = remoteVitalRange(71L, 91.0, fallback.pulse),
                        breath = remoteVitalRange("bad", 100L, fallback.breath))
                }
            }
            assertPulse(Color.rgb(191, 21, 22))
            assertEquals(nativeVitals.map { it.breath }, ranges.value.map { it.breath })
            scenario.onActivity {
                ranges.value = ranges.value.mapIndexed { index, range ->
                    if (index == 1) range.copy(pulse = remoteVitalRange(59L, 61L, range.pulse)) else range
                }
            }
            onView(withId(R.id.kid)).perform(click())
            assertPulse(Color.rgb(70, 178, 58))
            onView(withId(R.id.old)).perform(click())
            assertPulse(Color.rgb(191, 21, 22))
        }
    }

    @Test fun dictionaryOnlyAcceptsSuccessfulLocalCompletion() {
        var completion: ((Exception?) -> Unit)? = null
        var writes = 0
        val source = object : DictionarySource {
            override fun newKey() = "local-fixture"
            override fun listen(result: (List<DictionaryEntry>?, Boolean, Exception?) -> Unit): () -> Unit = {}
            override fun submit(key: String, edit: Boolean, fields: Map<String, Any>, result: (Exception?) -> Unit) {
                writes++
                assertEquals("local-fixture", key)
                assertFalse(edit)
                assertEquals(false, fields["approved"])
                completion = result
            }
        }
        instrumentation.runOnMainSync {
            val model = DictionaryModel(source)
            model.submit("local-fixture", null, "Local term", "Local definition")
            model.submit("local-fixture", null, "Duplicate", "Duplicate")
            assertEquals(1, writes)
            assertTrue(model.sending)
            assertNull(model.success)
            completion!!(IllegalStateException("Local rejection"))
            assertFalse(model.sending)
            assertTrue(model.writeError)
            assertNull(model.success)
            model.submit("local-fixture", null, "Local term", "Local definition")
            assertEquals(2, writes)
            assertFalse(model.writeError)
            completion!!(null)
            assertFalse(model.sending)
            assertEquals("local-fixture", model.success)
            model.consumeSuccess()
            assertNull(model.success)
        }
    }
}
