package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun FormScreen(values: List<String>, update: (Int, String) -> Unit, pickCodes: () -> Unit, preview: (String) -> Unit) {
    var submitted by rememberSaveable { mutableStateOf(false) }
    val errors = FormContract.errors(values)
    val font = FontFamily(Font(R.font.opensans_hebrew))
    val keyboard = LocalSoftwareKeyboardController.current
    val labels = listOf(R.string.ambN, R.string.visa, R.string.address, R.string.HN, R.string.HT, R.string.SUM, R.string.Fna, R.string.Lna)
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            labels.forEachIndexed { index, label ->
                IntegrationField(values[index], { update(index, it) }, label = stringResource(label),
                    error = if (submitted && errors[index] != 0) stringResource(errors[index]) else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = when (index) {
                        0, 1, 4 -> KeyboardType.Decimal
                        3, 5 -> KeyboardType.Number
                        else -> KeyboardType.Text
                    }), keyboardActions = if (index == 7) KeyboardActions(onNext = { keyboard?.hide(); pickCodes() }) else KeyboardActions.Default,
                    modifier = Modifier.fillMaxWidth().testTag("form-field-$index"))
            }
            OutlinedButton(onClick = { keyboard?.hide(); pickCodes() }, modifier = Modifier.fillMaxWidth().testTag("form-codes")) {
                Text(values[8].ifEmpty { stringResource(R.string.pickC) }, fontFamily = font)
            }
            if (submitted && errors[8] != 0) Text(stringResource(errors[8]), color = MaterialTheme.colorScheme.error)
        }
        Button(onClick = {
            submitted = true
            if (errors.all { it == 0 }) { keyboard?.hide(); preview(FormContract.body(values)) }
        }, colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.ColorNice)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 9.dp, end = 9.dp, top = 11.dp, bottom = 3.dp).heightIn(min = 68.dp).testTag("form-send")) {
            Text(stringResource(R.string.send), fontFamily = font, fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_send), contentDescription = null)
        }
    }
}
