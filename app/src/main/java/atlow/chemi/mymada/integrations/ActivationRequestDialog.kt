package atlow.chemi.mymada.integrations

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
fun ActivationRequestDialog(args: Bundle?, onCancel: () -> Unit, onConfirm: () -> Unit) {
    Surface {
        Column(Modifier.padding(24.dp)) {
            Text(stringResource(if (args == null) R.string.plsNote else R.string.suc), fontSize = 20.sp)
            val body = if (args == null) buildAnnotatedString { append(stringResource(R.string.plsNoteT)) }
            else buildAnnotatedString {
                withStyle(SpanStyle(color = Color.Red)) { append(stringResource(R.string.emailAlert)); append("\n\n") }
                withStyle(SpanStyle(color = Color(0xff238bad))) { append(stringResource(R.string.sender)) }
                append(args.getString("name").orEmpty()); append(" - "); append(args.getString("email").orEmpty()); append("\n\n")
                withStyle(SpanStyle(color = Color(0xff238bad))) { append(stringResource(R.string.msg)) }
                append("\n"); append(args.getString("reason").orEmpty())
            }
            Text(body, modifier = Modifier.weight(1f, fill = false).padding(vertical = 16.dp)
                .verticalScroll(rememberScrollState()).testTag("activation-request-summary"))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (args != null) TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.ret), color = colorResource(R.color.OldPrimary))
                }
                TextButton(onClick = if (args == null) onCancel else onConfirm) { Text(stringResource(R.string.ok)) }
            }
        }
    }
}
