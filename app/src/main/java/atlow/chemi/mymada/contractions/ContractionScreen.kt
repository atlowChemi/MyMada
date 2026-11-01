package atlow.chemi.mymada.contractions

import android.graphics.Typeface
import android.content.res.Configuration
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.roundToInt
import atlow.chemi.mymada.R

@Composable
fun ContractionScreen(entries: List<ContractionEntry>, showActiveRow: Boolean, dialog: Int, onToggle: () -> Unit, onDialog: (Boolean) -> Unit) {
    val assets = LocalContext.current.assets
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val negativeColor = colorResource(R.color.OldPrimary).toArgb()
    val currentOnDialog = rememberUpdatedState(onDialog)
    val font = remember(assets) { FontFamily(Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")) }
    val active = entries.lastOrNull()?.end == null && entries.isNotEmpty()
    val buttonTextSize = with(LocalDensity.current) { 30.sp.toPx().roundToInt().toSp() }
    val scroll = rememberLazyListState()
    LaunchedEffect(entries.lastOrNull()) { scroll.scrollToItem(0) }
    MaterialTheme {
        CompositionLocalProvider(LocalTextStyle provides TextStyle(fontFamily = font, color = Color.Black,
            platformStyle = PlatformTextStyle(includeFontPadding = true))) {
            Box(Modifier.fillMaxSize()) {
                val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                val history: @Composable (Modifier) -> Unit = { modifier ->
                    LazyColumn(modifier.testTag("contraction-history"), state = scroll, contentPadding = PaddingValues(horizontal = 9.dp)) {
                        items(entries.filter { showActiveRow || it.end != null }.asReversed(), key = { it.number }) { entry ->
                            Box(Modifier.padding(top = if (entry == entries.first()) 9.dp else 5.dp, bottom = (5 + 4f / 9).dp)
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(6.dp)).clip(RoundedCornerShape(6.dp))
                                .background(colorResource(R.color.White))) {
                                Column(Modifier.fillMaxWidth().padding(start = 50.dp).padding(8.dp)) {
                                    if (entry.end != null) {
                                        HistoryText(stringResource(R.string.ContracLength, duration(entry.end - entry.start)), 19, title = true)
                                        Spacer(Modifier.height(8.dp))
                                        HistoryText(entry.previous?.let { stringResource(R.string.ContracBetween, duration(entry.start - it)) }.orEmpty(), 16)
                                    } else {
                                        Text(stringResource(R.string.endContract), fontSize = 19.sp, color = colorResource(R.color.album_title))
                                        LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp), color = colorResource(R.color.Ocean))
                                    }
                                }
                                Row(Modifier.matchParentSize()) {
                                    Box(Modifier.width(50.dp).fillMaxHeight().background(colorResource(R.color.ColorAqua)), contentAlignment = Alignment.Center) {
                                        HistoryText(entry.number.toString(), 20, centered = true)
                                    }
                                }
                            }
                        }
                    }
                }
                val control: @Composable (Modifier) -> Unit = { modifier ->
                    Box(modifier.background(colorResource(R.color.buttonMaterialDark)).padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(180.dp).clip(CircleShape)
                            .background(if (active) androidx.compose.ui.graphics.Color(0xffdadada) else colorResource(R.color.White))
                            .clickable(role = Role.Button, onClick = onToggle).testTag("contraction-toggle"), contentAlignment = Alignment.Center) {
                            if (active) CircularProgressIndicator(Modifier.fillMaxSize(), color = colorResource(R.color.Ocean), strokeWidth = 4.dp)
                            Text(stringResource(if (active) R.string.endContract else R.string.startContract),
                                fontSize = buttonTextSize, textAlign = TextAlign.Center, color = colorResource(R.color.Ocean))
                        }
                    }
                }
                if (landscape) Row(Modifier.fillMaxSize()) {
                    history(Modifier.weight(.64f).fillMaxHeight())
                    control(Modifier.weight(.36f).fillMaxHeight())
                } else Column(Modifier.fillMaxSize()) {
                    history(Modifier.weight(1f).fillMaxWidth())
                    control(Modifier.fillMaxWidth())
                }
            }
            if (dialog != 0) DisposableEffect(context, configuration, dialog, negativeColor) {
                // All three states used the native alert host; Compose's default window is narrower.
                val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
                    .setMessage(dialog).setCancelable(false)
                    .setPositiveButton(R.string.ok) { _, _ -> currentOnDialog.value(true) }
                if (dialog != R.string.ContracDeleteEmpty) {
                    builder.setNegativeButton(R.string.dialog_no) { _, _ -> currentOnDialog.value(false) }
                }
                val alert = builder.create()
                alert.setOnShowListener {
                    if (dialog != R.string.ContracDeleteEmpty) {
                        val typeface = Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")
                        alert.findViewById<android.widget.TextView>(android.R.id.message)?.typeface = typeface
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                            this.typeface = typeface
                            setTextColor(negativeColor)
                        }
                    }
                }
                alert.show()
                onDispose { alert.dismiss() }
            }
        }
    }
}

/** Platform text preserves 12822's Latin/Hebrew fallback metrics inside the Compose history. */
@Composable
private fun HistoryText(text: String, size: Int, title: Boolean = false, centered: Boolean = false) {
    AndroidView(factory = { context ->
        object : AppCompatTextView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                // Target29 uses fallback spacing for StaticLayout, not BoringLayout.
                // Target33+ enables it for both; preserve the published distinction.
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    val direction = if (layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL)
                        android.text.TextDirectionHeuristics.FIRSTSTRONG_RTL else android.text.TextDirectionHeuristics.FIRSTSTRONG_LTR
                    val boring = android.text.BoringLayout.isBoring(text, paint)
                    setFallbackLineSpacing(direction.isRtl(text, 0, text.length) || boring == null ||
                        boring.width > MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight)
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }.apply {
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            setTextSize(TypedValue.COMPLEX_UNIT_PX,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size.toFloat(), resources.displayMetrics).roundToInt().toFloat())
            if (title) setTextColor(context.getColor(R.color.album_title))
            if (centered) gravity = android.view.Gravity.CENTER
        }
    }, update = { it.text = text }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun duration(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    return if (seconds >= 60) stringResource(R.string.ContracMinutes, seconds / 60, seconds % 60)
    else stringResource(R.string.ContracSecondes, seconds)
}
