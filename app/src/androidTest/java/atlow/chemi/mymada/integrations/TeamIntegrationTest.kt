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
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
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
            scenario.onActivity {
                it.selectPage(1)
            }
            android.util.Log.i("TeamFixture", "Open add")
            compose.onNodeWithTag("team-add").performClick()
            scenario.onActivity { team(it).childFragmentManager.executePendingTransactions() }
            instrumentation.waitForIdleSync()
            capture("add", language)
            compose.onNodeWithTag("member-save").performClick()
            compose.onNodeWithText(LocalHelper.onAttach(context).getString(R.string.emailNameNone)).assertIsDisplayed()
            compose.onNodeWithTag("member-name").performTextReplacement("Fixture Member")
            compose.onNodeWithTag("member-role").performClick()
            compose.onNodeWithTag("member-role-2").performClick()
            scenario.recreate()
            android.util.Log.i("TeamFixture", "Restored draft")
            compose.onNodeWithTag("member-name").assertTextContains("Fixture Member")
            compose.onNodeWithTag("member-role").assertTextContains(TeamContract.roles[2])
            compose.onNodeWithTag("member-save").performClick()
            instrumentation.waitForIdleSync()
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            capture("member", language)
            scenario.recreate()
            compose.onNodeWithTag("team-menu-0").performClick()
            android.util.Log.i("TeamFixture", "Open edit menu")
            compose.onNodeWithTag("team-edit-0").performClick()
            scenario.onActivity { team(it).childFragmentManager.executePendingTransactions() }
            compose.onNodeWithTag("member-name").assertTextContains("Fixture Member")
            compose.onNodeWithTag("member-name").performTextReplacement("Edited Member")
            compose.onNodeWithTag("member-role").performClick()
            compose.onNodeWithTag("member-role-6").performClick()
            compose.onNodeWithTag("member-save").performClick()
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
                assertEquals("צוות אמבולנס ירושלים 1234\nשירות לאומי - Edited Member\nחובש - Member1\nפראמדיק - Member2", confirmation.requireArguments().getString("body"))
            }
            compose.onNodeWithTag("dispatch-cancel").performClick()
            scenario.onActivity {
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
