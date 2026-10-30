package atlow.chemi.mymada.tools.vitals

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import atlow.chemi.mymada.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

@Composable
internal fun VitalsScreen(ranges: List<VitalRanges> = nativeVitals) {
    var age by rememberSaveable { mutableIntStateOf(0) }
    var pulse by rememberSaveable { mutableStateOf("") }
    var breath by rememberSaveable { mutableStateOf("") }
    var sys by rememberSaveable { mutableStateOf("") }
    var dia by rememberSaveable { mutableStateOf("") }
    var interacted by rememberSaveable { mutableStateOf(false) }
    val range = ranges[age]
    val pressure = pressureStatus(sys, dia, range)
    val ages = LocalContext.current.resources.getStringArray(R.array.goodVitals)
    val info = stringResource(R.string.goodVitalsOLD, range.pulse.low, range.pulse.high,
        range.breath.low, range.breath.high, range.systolic.low, range.systolic.high,
        range.diastolic.low, range.diastolic.high, ages[age])
    val large = LocalConfiguration.current.fontScale > 1.2f || LocalConfiguration.current.screenHeightDp < 480
    val landscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
    val fields: @Composable () -> Unit = {
            val direction = LocalLayoutDirection.current
            AndroidView(factory = { ctx ->
                RadioGroup(ctx).apply {
                    orientation = RadioGroup.HORIZONTAL
                    listOf(R.string.radio_old, R.string.radio_kid, R.string.radio_baby).forEachIndexed { i, label ->
                        addView(RadioButton(ctx).apply {
                            id = listOf(R.id.old, R.id.kid, R.id.baby)[i]
                            setText(label)
                            typeface = Typeface.createFromAsset(ctx.assets, "fonts/opensanshebrew.ttf")
                        }, RadioGroup.LayoutParams(0, -2, 1f))
                    }
                    setOnCheckedChangeListener { _, id ->
                        val selected = listOf(R.id.old, R.id.kid, R.id.baby).indexOf(id)
                        if (age != selected) { age = selected; interacted = true }
                    }
                }
            }, update = {
                it.layoutDirection = if (direction == LayoutDirection.Rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
                it.check(listOf(R.id.old, R.id.kid, R.id.baby)[age])
            }, modifier = Modifier.fillMaxWidth())
            VitalInput(pulse, { pulse = it; interacted = true }, R.string.fullPulse, R.id.pulsa, range.pulse.contains(pulse), R.id.breathing, interacted)
            VitalInput(breath, { breath = it; interacted = true }, R.string.breathing, R.id.breathing, range.breath.contains(breath), R.id.systolic, interacted)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(Modifier.fillMaxWidth()) {
                    VitalInput(sys, { sys = it; interacted = true }, R.string.systolic, R.id.systolic, pressure.first,
                        R.id.diastolic, interacted, Modifier.weight(.95f))
                    AndroidView(factory = { TextView(it).apply { text = "/"; textSize = 40f } }, modifier = Modifier.weight(.1f))
                    VitalInput(dia, { dia = it; interacted = true }, R.string.diastolic, R.id.diastolic, pressure.second,
                        null, interacted, Modifier.weight(.95f))
                }
            }
    }
    val cards: @Composable () -> Unit = {
        Spacer(Modifier.height(8.dp))
        VitalInfo(info)
        Spacer(Modifier.height(8.dp))
        VitalInfo(stringResource(R.string.vitalsAlert))
    }
    if (landscape) {
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { fields() }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { cards() }
        }
    } else {
        Column(Modifier.fillMaxSize().then(if (large) Modifier.verticalScroll(rememberScrollState()) else Modifier).padding(16.dp)) {
            Column(if (large) Modifier else Modifier.weight(1f).verticalScroll(rememberScrollState())) { fields() }
            cards()
        }
    }
}

@Composable
private fun VitalInput(value: String, onChange: (String) -> Unit, label: Int, viewId: Int,
    status: Boolean?, nextId: Int?, interacted: Boolean, modifier: Modifier = Modifier.fillMaxWidth()) {
    val change by rememberUpdatedState(onChange)
    AndroidView(factory = { context ->
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        TextInputLayout(context).apply {
            typeface = font
            addView(TextInputEditText(context).apply {
                id = viewId; setHint(label); typeface = font
                inputType = InputType.TYPE_CLASS_NUMBER
                imeOptions = if (nextId == null) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
                nextId?.let { nextFocusForwardId = it; nextFocusDownId = it }
                filters = arrayOf(InputFilter.LengthFilter(3), InputFilter { source, start, end, dest, dstart, dend ->
                    val candidate = dest.substring(0, dstart) + source.subSequence(start, end) + dest.substring(dend)
                    if (validVitalInput(candidate)) null else ""
                })
                doAfterTextChanged {
                    val text = it.toString()
                    change(text)
                    if (viewId == R.id.systolic && hasFocus() &&
                        (text.length == 3 || (text.length == 2 && !text.startsWith("1"))))
                        rootView.findViewById<View>(R.id.diastolic)?.requestFocus()
                }
            }, LinearLayout.LayoutParams(-1, -2))
        }
    }, update = { layout ->
        val edit = layout.editText!!
        if (edit.text.toString() != value) { edit.setText(value); edit.setSelection(value.length) }
        val color = when (status) { true -> Color.rgb(70,178,58); false -> Color.rgb(191,21,22); null -> Color.rgb(50,167,195) }
        edit.setTextColor(color)
        // Published initial underlines use theme state colors until the first calculation.
        edit.backgroundTintList = if (!interacted) null else ColorStateList.valueOf(color)
        layout.defaultHintTextColor = ColorStateList.valueOf(color)
        edit.contentDescription = layout.context.getString(label) + when(status) {
            true -> ": " + layout.context.getString(R.string.vitalsTakin)
            false -> ": " + layout.context.getString(R.string.vitalsNotTakin)
            null -> ""
        }
    }, modifier = modifier)
}

@Composable
private fun VitalInfo(text: String) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    AndroidView(factory = { context ->
        LinearLayout(context).apply {
            val density = resources.displayMetrics.density
            fun dp(value: Int) = (value * density + .5f).toInt()
            orientation = LinearLayout.HORIZONTAL
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.btns_new)!!.mutate().apply {
                (this as android.graphics.drawable.GradientDrawable).setColor(Color.WHITE)
            }
            setPaddingRelative(dp(7), dp(14), dp(14), dp(14))
            addView(ImageView(context).apply {
                setImageResource(R.drawable.ic_info); setColorFilter(Color.BLACK)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }, LinearLayout.LayoutParams(dp(24), dp(24)).apply { marginStart = dp(2); marginEnd = dp(2) })
            addView(View(context).apply { setBackgroundColor(Color.BLACK) },
                LinearLayout.LayoutParams(dp(1), -1).apply { marginEnd = dp(10) })
            addView(TextView(context).apply {
                textSize = 16f; setTextColor(Color.BLACK)
                typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            }, LinearLayout.LayoutParams(-1, -2))
        }
    }, update = {
        it.layoutDirection = if (rtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        (it.getChildAt(2) as TextView).text = text
    }, modifier = Modifier.fillMaxWidth())
}

@Preview(locale = "en", showBackground = true)
@Preview(locale = "he", fontScale = 1.5f, showBackground = true)
@Composable
private fun VitalsPreview() = VitalsScreen()
