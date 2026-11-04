package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatDialogGeometryTest {
    @get:Rule val compose = createComposeRule()

    private fun chat(direction: LayoutDirection, editing: Boolean) {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                MaterialTheme { ChatDialogScreen(editing, "Fixture", {}, false, true, {}) }
            }
        }
        val dialog = compose.onNodeWithTag(if (editing) "chat-name-dialog" else "chat-disclaimer-dialog")
            .fetchSemanticsNode().boundsInRoot
        val action = compose.onNodeWithTag("chat-dialog-confirm").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("Native chat action stays physically right", action.center.x > dialog.center.x)
    }

    @Test fun rtlDisclaimerActionIsRight() = chat(LayoutDirection.Rtl, false)
    @Test fun rtlRenameActionIsRight() = chat(LayoutDirection.Rtl, true)
    @Test fun ltrDisclaimerActionIsRight() = chat(LayoutDirection.Ltr, false)
    @Test fun ltrRenameActionIsRight() = chat(LayoutDirection.Ltr, true)

    @Test fun otherRtlDialogsKeepLogicalEnd() {
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    IntegrationDialog("Fixture", Modifier.width(320.dp).testTag("dialog"), buttons = {
                        TextButton({}, Modifier.testTag("action")) { Text("OK") }
                    }) { Text("Unchanged body direction") }
                }
            }
        }
        val dialog = compose.onNodeWithTag("dialog").fetchSemanticsNode().boundsInRoot
        val action = compose.onNodeWithTag("action").fetchSemanticsNode().boundsInRoot
        assertTrue("Default End must remain left in RTL", action.center.x < dialog.center.x)
    }
}
