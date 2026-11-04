package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
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
    Surface {
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(if (editingName) R.string.writeName else R.string.plsNote), fontSize = 20.sp)
            if (editingName) TextField(draft, update, singleLine = true, label = { Text(stringResource(R.string.name)) },
                isError = invalid, supportingText = if (invalid) ({ Text(stringResource(if (draft.trim().isEmpty()) R.string.emailNameNone else R.string.chatNameNum)) }) else null,
                modifier = Modifier.padding(vertical = 16.dp).testTag("chat-name-draft"))
            else Text(stringResource(R.string.contactMsg), modifier = Modifier.padding(vertical = 16.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = confirm, enabled = !editingName || ready, modifier = Modifier.testTag("chat-dialog-confirm")) { Text(stringResource(R.string.ok)) }
                }
            }
        }
    }
}
