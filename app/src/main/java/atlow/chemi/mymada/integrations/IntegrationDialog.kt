package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Shared native-alert spacing for the fragment-hosted Compose dialogs. */
@Composable
internal fun IntegrationDialog(title: String, modifier: Modifier = Modifier,
    actionArrangement: Arrangement.Horizontal = Arrangement.End,
    buttons: @Composable RowScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val type = MaterialTheme.typography
    MaterialTheme(typography = type.copy(
        bodyLarge = type.bodyLarge.copy(lineHeight = 19.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)),
        labelLarge = type.labelLarge.copy(fontWeight = FontWeight.Normal, platformStyle = PlatformTextStyle(includeFontPadding = true)),
    )) {
        Surface(modifier) {
            Column {
                Text(title, style = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, lineHeight = 22.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 8.dp))
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { content() }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = actionArrangement, content = buttons)
            }
        }
    }
}
