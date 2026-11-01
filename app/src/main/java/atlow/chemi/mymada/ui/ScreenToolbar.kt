package atlow.chemi.mymada.ui

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

/** The published AppCompat toolbar geometry, shared by the two Compose information screens. */
@Composable
internal fun ScreenToolbar(title: String, detecting: Boolean, onBack: () -> Unit,
    onDetection: () -> Unit, onSettings: () -> Unit) {
    val titleFont = remember { FontFamily(Typeface.create("sans-serif-medium", Typeface.NORMAL)) }
    Row(Modifier.shadow(4.dp).background(colorResource(R.color.ColorPrimary)).statusBarsPadding()
        .testTag("information-toolbar").height(56.dp),
        verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.width(56.dp)) {
            Icon(painterResource(R.drawable.privacy_back),
                stringResource(androidx.appcompat.R.string.abc_action_bar_up_description), tint = Color.White)
        }
        Text(title, Modifier.weight(1f).padding(start = 16.dp), color = Color.White,
            style = TextStyle(fontSize = 20.sp, fontFamily = titleFont,
                platformStyle = PlatformTextStyle(includeFontPadding = true)),
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = onDetection, modifier = Modifier.semantics {
            toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
        }) {
            Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off),
                stringResource(R.string.pref_title_enable), tint = Color.White)
        }
        IconButton(onClick = onSettings) {
            Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings), tint = Color.White)
        }
    }
}
