package atlow.chemi.mymada.calls

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Typeface
import android.util.TypedValue
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import atlow.chemi.mymada.R
import kotlin.math.abs
import kotlin.math.roundToInt

/** Compose owns the popup layout; native leaves retain published text metrics and touch feedback. */
@Composable
internal fun CallPopupScreen(event: CallEvent, text: CallText, onClose: () -> Unit, onDetails: () -> Unit) {
    val context = LocalContext.current
    val themed = remember(context) { ContextThemeWrapper(context, R.style.MyMaterialTheme_SmsRes) }
    val bars = WindowInsets.systemBars
    val density = LocalDensity.current
    val barColor = colorResource(R.color.OldPrimary)
    CompositionLocalProvider(LocalContext provides themed) {
        Layout(modifier = Modifier.fillMaxSize().drawBehind {
            // Edge-to-edge targets must paint the published bar colors behind transparent system bars.
            drawRect(barColor, size = Size(size.width, bars.getTop(density).toFloat()))
            val bottom = bars.getBottom(density).toFloat()
            drawRect(barColor, Offset(0f, size.height - bottom), Size(size.width, bottom))
        }.systemBarsPadding(), content = {
            CallTextView(if (event.type == 4 || event.type == 6) "הודעה חדשה!" else "אירוע חדש!",
                30f, R.id.newC, R.color.White, Modifier.background(colorResource(R.color.OldPrimary)), padding = 15)
            CallTextView(when (event.type) { 2 -> "קבוצה"; 3, 5 -> "משמרת"; 4 -> "הודעה מנהלתית"; 6 -> "שירות"; else -> "כונן" },
                19f, R.id.msgType, R.color.ColorAqua, padding = 15, flash = event.type == 4)
            CallTextView(text.address, 35f, R.id.Address, padding = 15, lines = if (text.plain) 5 else 3)
            val codes = SpannableString(if (text.plain) "" else "${text.dispatchCode}\n${text.medicalCode}").apply {
                if (!text.plain) {
                    setSpan(ForegroundColorSpan(themed.getColor(R.color.ColorPrimary)), text.dispatchCode.length, length, 0)
                    setSpan(StyleSpan(Typeface.BOLD), text.dispatchCode.length, length, 0)
                }
            }
            CallTextView(codes, 27f, R.id.KodR)
            CallTextView(stringResource(R.string.time, text.time), 22f, R.id.SMS)
            Image(painterResource(R.drawable.ic_mda), stringResource(R.string.hello_world), Modifier.fillMaxWidth().height(60.dp))
            CallTextView(stringResource(R.string.app_name), 18f, R.id.name, R.color.materialGrey600)
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CallButton(R.string.Mrec2, R.id.button, Modifier.weight(1f).height(50.dp), onClose)
                CallButton(R.string.Minf, R.id.buttonI, Modifier.weight(1f).height(50.dp), onDetails)
            }
        }) { measurables, constraints ->
            val children = measurables.map { it.measure(Constraints(minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth)) }
            layout(constraints.maxWidth, constraints.maxHeight) {
                val heading = children[0]
                val type = children[1]
                val address = children[2]
                val codes = children[3]
                val time = children[4]
                val logo = children[5]
                val name = children[6]
                val buttons = children[7]
                heading.place(0, 0)
                val typeY = heading.height + 15.dp.roundToPx()
                type.place(0, typeY)
                val addressY = typeY + type.height
                address.place(0, addressY)
                val buttonsY = constraints.maxHeight - 8.dp.roundToPx() - buttons.height
                buttons.place(0, buttonsY)
                val nameY = buttonsY - 20.dp.roundToPx() - name.height
                name.place(0, nameY)
                val logoY = nameY - 20.dp.roundToPx() - logo.height
                logo.place(0, logoY)
                val timeY = logoY - 35.dp.roundToPx() - time.height
                time.place(0, timeY)
                val codesTop = addressY + address.height + 20.dp.roundToPx()
                codes.place(0, codesTop + (timeY - 8.dp.roundToPx() - codesTop - codes.height) / 2)
            }
        }
    }
}

@Composable
private fun CallTextView(value: CharSequence, size: Float, viewId: Int, color: Int = R.color.Black,
    modifier: Modifier = Modifier, padding: Int = 0, lines: Int? = null, flash: Boolean = false) {
    var animator by remember { mutableStateOf<ObjectAnimator?>(null) }
    AndroidView(modifier = modifier, factory = { context ->
        object : AppCompatTextView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                matchPublishedSpacing(widthMeasureSpec)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }.apply {
            id = viewId
            setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, resources.displayMetrics).roundToInt().toFloat())
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            setTextColor(context.getColor(color))
            val inset = (padding * resources.displayMetrics.density + .5f).toInt()
            setPadding(0, inset, 0, inset)
            if (lines != null) {
                maxLines = lines
                ellipsize = TextUtils.TruncateAt.END
                movementMethod = ScrollingMovementMethod()
                isVerticalScrollBarEnabled = true
                scrollBarSize = (4 * resources.displayMetrics.density + .5f).toInt()
                if (android.os.Build.VERSION.SDK_INT >= 29) verticalScrollbarTrackDrawable =
                    android.graphics.drawable.ColorDrawable(context.getColor(R.color.ColorPrimary))
            }
            if (flash) {
                startAnimation(AlphaAnimation(0f, 1f).apply {
                    duration = 150; startOffset = 100; repeatMode = Animation.REVERSE; repeatCount = Animation.INFINITE
                })
                animator = ObjectAnimator.ofInt(this, "textColor", android.graphics.Color.RED, android.graphics.Color.BLUE).apply {
                    duration = 350; repeatMode = ValueAnimator.REVERSE; repeatCount = ValueAnimator.INFINITE
                    setEvaluator(ArgbEvaluator()); start()
                }
            }
        }
    }, update = { it.text = value }, onRelease = { animator?.cancel(); it.clearAnimation() })
}

@Composable
private fun CallButton(label: Int, viewId: Int, modifier: Modifier, action: () -> Unit) {
    val onClick by rememberUpdatedState(action)
    AndroidView(modifier = modifier, factory = { context ->
        object : AppCompatButton(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                matchPublishedSpacing(widthMeasureSpec)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }.apply {
            id = viewId
            setText(label)
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_PX, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                20f, resources.displayMetrics).roundToInt().toFloat())
            setTextColor(context.getColor(R.color.White))
            setBackgroundResource(R.drawable.btns_new)
            setOnClickListener { onClick() }
            // Published drag cancellation uses physical pixels, not dp or platform touch slop.
            var x = 0f; var y = 0f; var moved = false
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> { x = event.x; y = event.y; moved = false; view.alpha = .4f; true }
                    MotionEvent.ACTION_MOVE -> { if (abs(x - event.x) > 140 || abs(y - event.y) > 50) { moved = true; view.alpha = 1f }; true }
                    MotionEvent.ACTION_UP -> { view.alpha = 1f; if (!moved) view.performClick(); true }
                    MotionEvent.ACTION_CANCEL -> { moved = true; view.alpha = 1f; true }
                    else -> false
                }
            }
        }
    })
}

private fun android.widget.TextView.matchPublishedSpacing(widthMeasureSpec: Int) {
    // Match target29's StaticLayout/BoringLayout distinction, as in HistoryText.
    if (android.os.Build.VERSION.SDK_INT >= 28) {
        val direction = if (layoutDirection == View.LAYOUT_DIRECTION_RTL)
            android.text.TextDirectionHeuristics.FIRSTSTRONG_RTL else android.text.TextDirectionHeuristics.FIRSTSTRONG_LTR
        val boring = android.text.BoringLayout.isBoring(text, paint)
        setFallbackLineSpacing(direction.isRtl(text, 0, text.length) || boring == null ||
            boring.width > View.MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight)
    }
}
