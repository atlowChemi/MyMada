package atlow.chemi.mymada.tools.metronome

import android.graphics.Typeface
import android.content.res.Configuration
import android.util.TypedValue
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import atlow.chemi.mymada.R
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt

@Composable
internal fun MetronomeScreen(seconds: Long, running: Boolean, visible: Boolean, onToggle: () -> Unit) {
    val label = stringResource(if (running) R.string.MetroStop else R.string.startT)
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(running, visible) {
        if (running && visible) {
            val easing = Easing { ((cos((it + 1) * Math.PI) / 2) + .5).toFloat() }
            pulse.snapTo(1.05f)
            while (true) {
                pulse.animateTo(1f, tween(650, easing = easing))
                pulse.animateTo(1.05f, tween(350, easing = easing))
            }
        } else pulse.snapTo(1f)
    }
    val progress by animateFloatAsState((seconds % 120).toFloat() / 119,
        tween(80, easing = Easing { 1 - (1 - it) * (1 - it) }), label = "Two-minute cycle")
    val shadow = colorResource(R.color.ShadowEnd)
    val track = colorResource(R.color.Grey)
    val accent = colorResource(R.color.OldPrimary)
    Layout(modifier = Modifier.fillMaxSize().testTag("metronome-toggle")
        .semantics { contentDescription = label }
        .clickable(role = Role.Button, onClickLabel = label, onClick = onToggle).padding(16.dp), content = {
        // Native text retains the installed font's padding, fallback and large-font metrics.
        AndroidView(factory = { context ->
            object : AppCompatTextView(context) {
                override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                    // Preserve target29's fallback metrics, including wrapped/RTL headings.
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        val direction = if (layoutDirection == View.LAYOUT_DIRECTION_RTL)
                            android.text.TextDirectionHeuristics.FIRSTSTRONG_RTL else android.text.TextDirectionHeuristics.FIRSTSTRONG_LTR
                        val boring = android.text.BoringLayout.isBoring(text, paint)
                        setFallbackLineSpacing(direction.isRtl(text, 0, text.length) || boring == null ||
                            boring.width > MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight)
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
            }.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    18f, resources.displayMetrics).roundToInt().toFloat())
                typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
                setText(R.string.startT)
            }
        })
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(10.dp)) {
                scale(pulse.value) {
                    drawCircle(shadow)
                    drawCircle(Color.White, radius = size.width / 2 - 5.dp.roundToPx())
                }
                val ringWidth = size.width - if (landscape) 2 * (25.dp.roundToPx() - 10.dp.roundToPx()) else 0
                val thickness = ringWidth / 17
                val radius = ringWidth / 2.5f + thickness / 2
                drawCircle(track, radius, style = Stroke(thickness))
                drawArc(accent, 0f, progress * 360, false,
                    Offset(center.x - radius, center.y - radius), Size(radius * 2, radius * 2),
                    style = Stroke(thickness))
            }
            AndroidView(factory = { context ->
                AppCompatTextView(context).apply {
                    id = R.id.metro_Clock
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        if (landscape) 50f else 80f, resources.displayMetrics).roundToInt().toFloat())
                    setTextColor(android.graphics.Color.BLACK)
                    gravity = Gravity.CENTER
                    textDirection = View.TEXT_DIRECTION_LTR
                }
            }, update = {
                val time = if (seconds >= 3600) String.format(Locale.ROOT, "%02d\n%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
                    else String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
                if (android.os.Build.VERSION.SDK_INT >= 28) it.setFallbackLineSpacing('\n' in time)
                it.text = SpannableString(time).apply {
                    if ('\n' in time) setSpan(RelativeSizeSpan(.6f), time.indexOf('\n'), length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            })
        }
    }) { measurables, constraints ->
        val heading = measurables[0].measure(Constraints(maxWidth = constraints.maxWidth))
        val top = 10.dp.roundToPx()
        val below = top + heading.height
        val diameter = if (landscape) minOf(constraints.maxWidth, (constraints.maxHeight - below).coerceAtLeast(0))
            else constraints.maxWidth
        val circle = measurables[1].measure(Constraints.fixed(diameter, diameter))
        layout(constraints.maxWidth, constraints.maxHeight) {
            heading.place(((constraints.maxWidth - heading.width) / 2f).roundToInt(), top)
            circle.place((constraints.maxWidth - circle.width) / 2,
                below + (constraints.maxHeight - below - circle.height) / 2)
        }
    }
}
