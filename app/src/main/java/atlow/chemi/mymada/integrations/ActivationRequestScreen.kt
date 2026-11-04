package atlow.chemi.mymada.integrations

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun ActivationRequestScreen(busy: Boolean, confirm: (String, String, String) -> Unit) {
    var fields by rememberSaveable { mutableStateOf(listOf("", "", "")) }
    var touched by rememberSaveable { mutableStateOf(listOf(false, false, false)) }
    val font = FontFamily(Font(R.font.opensans_hebrew))
    val errors = fields.mapIndexed { index, value ->
        when {
            value.isBlank() -> listOf(R.string.emailNone, R.string.emailNameNone, R.string.emailMsgNone)[index]
            index == 0 && !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> R.string.emailInvalid
            else -> 0
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text(stringResource(R.string.codeNote), fontFamily = font, fontSize = 18.sp, lineHeight = 22.sp,
                color = colorResource(R.color.materialGrey600), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp))
            fields.forEachIndexed { index, value ->
                IntegrationField(value, { next ->
                    fields = fields.toMutableList().also { it[index] = next }
                    touched = touched.toMutableList().also { it[index] = true }
                }, enabled = !busy, singleLine = index != 2, minLines = if (index == 2) 11 else 1,
                    label = stringResource(listOf(R.string.from, R.string.name, R.string.note2)[index]),
                    error = if (touched[index] && errors[index] != 0) stringResource(errors[index]) else null,
                    textStyle = TextStyle(fontFamily = font),
                    keyboardOptions = KeyboardOptions(keyboardType = if (index == 0) KeyboardType.Email else KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth().testTag("activation-request-$index"))
            }
        }
        Button(onClick = {
            touched = listOf(true, true, true)
            if (errors.all { it == 0 }) confirm(fields[0].trim(), fields[1].trim(), fields[2].trim())
        }, enabled = !busy, colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.ColorNice)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).heightIn(min = 60.dp).testTag("activation-request-submit")) {
            Text(stringResource(R.string.succ), fontFamily = font, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(R.drawable.ic_send), contentDescription = null)
        }
    }
}
