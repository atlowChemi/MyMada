package atlow.chemi.mymada.about

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import atlow.chemi.mymada.BuildConfig
import atlow.chemi.mymada.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    detecting: Boolean,
    onBack: () -> Unit,
    onDetection: () -> Unit,
    onSettings: () -> Unit,
    onPrivacy: () -> Unit,
) {
    val assets = LocalContext.current.assets
    val font = remember(assets) { FontFamily(Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_activity_info)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.ColorPrimary),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.privacy_back),
                        stringResource(androidx.appcompat.R.string.abc_action_bar_up_description))
                }
            },
            actions = {
                IconButton(onClick = onDetection, modifier = Modifier.semantics {
                    toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
                }) {
                    Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off),
                        stringResource(R.string.pref_title_enable))
                }
                IconButton(onClick = onSettings) {
                    Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings))
                }
            },
        )
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.TversionNumber, BuildConfig.VERSION_NAME),
                fontFamily = font, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onPrivacy) { Text(stringResource(R.string.title_activity_privacy)) }
        }
    }
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew RTL large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Composable
private fun AboutPreview() {
    MaterialTheme { AboutScreen(true, {}, {}, {}, {}) }
}
