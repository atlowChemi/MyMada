package atlow.chemi.mymada.about

import android.graphics.Typeface
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.BuildConfig
import atlow.chemi.mymada.R
import atlow.chemi.mymada.ui.ScreenToolbar

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
    // Let the existing window's ic_bgg artwork show through, as in the published native layout.
    Scaffold(containerColor = Color.Transparent, topBar = {
        ScreenToolbar(stringResource(R.string.title_activity_info), detecting, onBack, onDetection, onSettings)
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.TversionNumber, BuildConfig.VERSION_NAME), color = Color.Black,
                style = TextStyle(fontFamily = font, fontSize = 18.sp, lineBreak = LineBreak.Paragraph, hyphens = Hyphens.Auto,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)))
            // Original 8dp text margin plus CardView's radius/elevation compatibility inset.
            Spacer(Modifier.height(22.dp))
            Surface(onClick = onPrivacy, modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth().height(85.dp),
                shape = RoundedCornerShape(23.dp), color = Color.White, shadowElevation = 5.dp) {
                Image(painterResource(R.drawable.privacy), stringResource(R.string.title_activity_privacy),
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew RTL large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Composable
private fun AboutPreview() {
    MaterialTheme { AboutScreen(true, {}, {}, {}, {}) }
}
