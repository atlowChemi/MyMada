package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun TeamMemberScreen(name: String, updateName: (String) -> Unit, role: Int, updateRole: (Int) -> Unit,
    invalid: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val font = FontFamily(Font(R.font.opensans_hebrew))
    Surface {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
            Text(stringResource(R.string.Nteam), fontSize = 20.sp)
            TextField(name, updateName, singleLine = true, label = { Text(stringResource(R.string.name), fontFamily = font) },
                isError = invalid, supportingText = if (invalid) ({ Text(stringResource(R.string.emailNameNone)) }) else null,
                modifier = Modifier.fillMaxWidth().testTag("member-name"))
            Box {
                TextButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().testTag("member-role")) { Text(TeamContract.roles[role], fontFamily = font) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TeamContract.roles.forEachIndexed { index, label ->
                        DropdownMenuItem(text = { Text(label, fontFamily = font) }, onClick = { updateRole(index); expanded = false }, modifier = Modifier.testTag("member-role-$index"))
                    }
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel, modifier = Modifier.testTag("member-cancel")) { Text(stringResource(R.string.ret), color = colorResource(R.color.OldPrimary), fontFamily = font) }
                    TextButton(onClick = onSave, modifier = Modifier.testTag("member-save")) { Text(stringResource(R.string.ok), fontFamily = font) }
                }
            }
        }
    }
}
