package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun ActivationScreen(busy: Boolean, activate: (String) -> Unit) {
    var digits by rememberSaveable { mutableStateOf(listOf("", "", "", "")) }
    val focus = remember { List(4) { FocusRequester() } }
    val label = stringResource(R.string.title_code)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(top = 100.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                digits.forEachIndexed { index, digit ->
                    IntegrationField(digit, { value ->
                        if (value.length <= 1 && value.all { it in '0'..'9' }) {
                            digits = digits.toMutableList().also { it[index] = value }
                            focus.getOrNull(if (value.isEmpty()) index - 1 else index + 1)?.requestFocus()
                        }
                    }, enabled = !busy, singleLine = true, textStyle = TextStyle(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f).focusRequester(focus[index]).testTag("activation-digit-$index")
                            .semantics { contentDescription = "$label ${index + 1}" })
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { activate(digits.joinToString("")) }, enabled = !busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("activation-submit")) {
                Text(stringResource(R.string.succ).uppercase(), fontSize = 20.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(R.font.opensans_hebrew)),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Normal)
            }
        }
    }
}
