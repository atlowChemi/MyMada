package atlow.chemi.mymada.integrations

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

/** Native integration palette; retain Android font scaling while avoiding Material's new tint/spacing. */
@Composable
fun IntegrationTheme(content: @Composable () -> Unit) {
    val accent = colorResource(R.color.ColorNice)
    val text = MaterialTheme.typography
    MaterialTheme(colorScheme = lightColorScheme(primary = accent, secondary = accent, onPrimary = Color.White,
        surface = Color(0xffececec), onSurface = Color.Black, surfaceContainerHighest = Color.Transparent,
        onSurfaceVariant = accent, outline = Color(0xff789095), error = Color.Red),
        typography = text.copy(bodyLarge = text.bodyLarge.copy(letterSpacing = 0.sp),
            bodyMedium = text.bodyMedium.copy(letterSpacing = 0.sp), bodySmall = text.bodySmall.copy(letterSpacing = 0.sp),
            labelLarge = text.labelLarge.copy(letterSpacing = 0.sp), labelMedium = text.labelMedium.copy(letterSpacing = 0.sp)),
        content = content)
}
