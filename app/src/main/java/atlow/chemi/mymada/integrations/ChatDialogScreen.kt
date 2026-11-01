package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun ChatDialogScreen(editingName: Boolean, draft: String, update: (String) -> Unit, invalid: Boolean, ready: Boolean, confirm: () -> Unit) {
    IntegrationDialog(stringResource(if (editingName) R.string.writeName else R.string.plsNote),
        Modifier.testTag(if (editingName) "chat-name-dialog" else "chat-disclaimer-dialog"),
        // Published chat forces only its lone action to the physical right in both locales.
        actionArrangement = Arrangement.Absolute.Right, buttons = {
            TextButton(onClick = confirm, enabled = !editingName || ready, modifier = Modifier.testTag("chat-dialog-confirm")) { Text(stringResource(R.string.ok)) }
        }) {
        if (editingName) IntegrationField(draft, update, label = stringResource(R.string.name),
            error = if (invalid) stringResource(if (draft.trim().isEmpty()) R.string.emailNameNone else R.string.chatNameNum) else null,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp).testTag("chat-name-draft"),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif))
        else Text(stringResource(R.string.contactMsg), modifier = Modifier.padding(horizontal = 24.dp))
    }
}
