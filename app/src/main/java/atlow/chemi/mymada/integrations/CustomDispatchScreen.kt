package atlow.chemi.mymada.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

@Composable
internal fun CustomDispatchScreen(onPreview: (String) -> Unit) {
    var body by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val hint = stringResource(R.string.toMoked)
    val error = stringResource(R.string.customMsgErr)
    val accent = colorResource(R.color.ColorNice)
    val font = FontFamily(Font(R.font.opensans_hebrew))
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
            Text(hint, color = if (invalid) Color.Red else accent, fontFamily = font, fontSize = 12.sp)
            BasicTextField(body, onValueChange = { body = it; if (it.isNotBlank()) invalid = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).padding(top = 4.dp).testTag("custom-body")
                    .semantics { contentDescription = hint; if (invalid) error(error) },
                textStyle = TextStyle(fontFamily = font, fontSize = 18.sp, color = Color.Black),
                cursorBrush = SolidColor(accent))
            Spacer(Modifier.fillMaxWidth().height(2.dp).background(if (invalid) Color.Red else accent))
            if (invalid) Text(error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.testTag("custom-error"))
        }
        Surface(onClick = { invalid = body.isBlank(); if (!invalid) onPreview(body) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 20.dp).heightIn(min = 60.dp).testTag("custom-send"),
            shape = RoundedCornerShape(15.dp), color = accent, shadowElevation = 4.dp) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.succ), color = Color.White, fontFamily = font, fontSize = 20.sp)
                Icon(painterResource(R.drawable.ic_send), contentDescription = null, tint = Color.White,
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp))
            }
        }
    }
}
