package atlow.chemi.mymada.integrations

import android.graphics.Bitmap
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.viewpager.widget.ViewPager
import atlow.chemi.mymada.MokedPage
import atlow.chemi.mymada.R
import atlow.chemi.mymada.fragments.MainActivity2
import atlow.chemi.mymada.helpers.LocalHelper
import com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import java.io.File

class TeamIntegrationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    @Test fun stateRestoresFourMemberLimitAndRejectsInvalidEntries() {
        instrumentation.runOnMainSync {
            val state = SavedStateHandle()
            val model = TeamModel(state)
            assertFalse(model.save(-1, "  ", 0))
            assertFalse(model.save(-1, "X", -1))
            repeat(4) { assertTrue(model.save(-1, "Name$it", it)) }
            assertFalse(model.save(-1, "Fifth", 4))
            val restored = TeamModel(SavedStateHandle(mapOf("members" to state.get<ArrayList<android.os.Bundle>>("members"))))
            assertEquals(model.members, restored.members)
            assertTrue(restored.save(2, "Edited", 6))
            assertEquals(6, restored.members[2].job)
            restored.delete(1)
            assertEquals(listOf("Name0", "Edited", "Name3"), restored.members.map { it.name })
            assertTrue(restored.save(-1, "Fourth", 0))
        }
    }
    @Test fun realTeamDialogDraftAndMembersSurviveRecreationAndPreviewIsLiteral() {
        val language = InstrumentationRegistry.getArguments().getString("captureLanguage", "en")
        context.getSharedPreferences("Settings", 0).edit().putBoolean("realM", true)
            .putBoolean("pickedMoked", true).putBoolean("Mapp", false).putInt("merhav", 0).commit()
        instrumentation.runOnMainSync { LocalHelper.setLocale(context, language) }
        ActivityScenario.launch(MokedPage::class.java).use { scenario ->
            fun team(activity: MokedPage) = activity.supportFragmentManager.fragments.filterIsInstance<MainActivity2>().single()
            fun dialog(activity: MokedPage) = (team(activity).childFragmentManager.findFragmentByTag("team-member") as MainActivity2.MemberDialog).requireDialog() as AlertDialog
            scenario.onActivity {
                it.findViewById<ViewPager>(R.id.viewpager).setCurrentItem(1, false)
            }
            android.util.Log.i("TeamFixture", "Open add")
            compose.onNodeWithTag("team-add").performClick()
            scenario.onActivity { team(it).childFragmentManager.executePendingTransactions() }
            instrumentation.waitForIdleSync()
            capture("add", language)
            scenario.onActivity {
                val dialog = dialog(it)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
                assertNotNull(dialog.findViewById<TextInputLayout>(R.id.addManTIL)!!.error)
                dialog.findViewById<EditText>(R.id.addManName)!!.setText("Fixture Member")
                dialog.findViewById<Spinner>(R.id.addManSpinner)!!.setSelection(2)
            }
            scenario.recreate()
            android.util.Log.i("TeamFixture", "Restored draft")
            scenario.onActivity {
                val dialog = dialog(it)
                assertEquals("Fixture Member", dialog.findViewById<EditText>(R.id.addManName)!!.text.toString())
                assertEquals(2, dialog.findViewById<Spinner>(R.id.addManSpinner)!!.selectedItemPosition)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            }
            instrumentation.waitForIdleSync()
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            capture("member", language)
            scenario.recreate()
            compose.onNodeWithTag("team-menu-0").performClick()
            android.util.Log.i("TeamFixture", "Open edit menu")
            compose.onNodeWithTag("team-edit-0").performClick()
            scenario.onActivity { team(it).childFragmentManager.executePendingTransactions() }
            scenario.onActivity {
                val dialog = dialog(it)
                assertEquals("Fixture Member", dialog.findViewById<EditText>(R.id.addManName)!!.text.toString())
                dialog.findViewById<EditText>(R.id.addManName)!!.setText("Edited Member")
                dialog.findViewById<Spinner>(R.id.addManSpinner)!!.setSelection(6)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            }
            scenario.onActivity {
                val model = ViewModelProvider(team(it))[TeamModel::class.java]
                assertEquals("Edited Member", model.members.single().name)
                repeat(3) { i -> assertTrue(model.save(-1, "Member$i", i)) }
            }
            compose.onNodeWithTag("team-add").assertDoesNotExist()
            android.util.Log.i("TeamFixture", "Four-member cap")
            compose.onNodeWithTag("team-menu-1").performClick()
            compose.onNodeWithTag("team-delete-1").performClick()
            compose.onNodeWithTag("team-add").assertExists()
            compose.onNodeWithTag("team-ambulance").performTextReplacement("1234")
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            compose.onNodeWithTag("team-send").performClick()
            scenario.onActivity { it.supportFragmentManager.executePendingTransactions() }
            instrumentation.waitForIdleSync()
            scenario.onActivity {
                val confirmation = it.supportFragmentManager.findFragmentByTag("dispatch-confirmation") as DispatchConfirmation
                assertEquals("צוות אמבולנס י-ם 1234:\nשירות לאומי - Edited Member\nחובש - Member1\nפראמדיק - Member2", confirmation.requireArguments().getString("body"))
                (confirmation.requireDialog() as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
                val model = ViewModelProvider(team(it))[TeamModel::class.java]
                repeat(model.members.size) { model.delete(0) }
            }
            compose.onNodeWithTag("team-empty").assertExists()
        }
    }
    private fun capture(name: String, language: String) {
        instrumentation.uiAutomation.waitForIdle(500, 5000)
        val image = instrumentation.uiAutomation.takeScreenshot()
        val directory = File(context.getExternalFilesDir(null), "team").apply { mkdirs() }
        File(directory, "$name-$language.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }
}
