package atlow.chemi.mymada.tools.pulse

import android.content.res.Configuration
import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.FixedTextInputEditText
import com.google.android.material.textfield.TextInputLayout

@Composable
internal fun PulseScreen(input: String, result: String, invalid: Boolean, seconds: Int,
    onInput: (String) -> Unit, onTimer: () -> Unit) {
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val fields: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = if (landscape) 8.dp else 16.dp)) {
            Spacer(Modifier.height(if (landscape) 8.dp else 10.dp))
            PulseField(input, false, invalid, onInput)
            Spacer(Modifier.height(if (landscape) 8.dp else 10.dp))
            PulseField(result, true, false, {})
        }
    }
    val timer: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier.testTag("pulse-timer").background(colorResource(R.color.buttonMaterialDark))
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.pulseClickHint), onClick = onTimer),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PulseText(seconds.toString(), 50f, Modifier.fillMaxWidth().padding(top = 15.dp), R.id.txtTimerSecond)
            PulseText(stringResource(R.string.seconds), 25f, Modifier.fillMaxWidth())
            PulseText(stringResource(R.string.pulseClickHint), 15f,
                Modifier.fillMaxWidth().padding(bottom = 10.dp), color = 0xffcfcfcf.toInt())
        }
    }
    if (landscape) {
        Row(Modifier.fillMaxSize()) {
            fields(Modifier.weight(.64f).fillMaxHeight())
            timer(Modifier.weight(.36f).fillMaxHeight())
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            fields(Modifier.weight(1f).fillMaxWidth())
            timer(Modifier.fillMaxWidth())
        }
    }
}

/** Keep published floating labels, underline, error placement and native keyboard behavior. */
@Composable
private fun PulseField(value: String, output: Boolean, invalid: Boolean, onInput: (String) -> Unit) {
    val change by rememberUpdatedState(onInput)
    AndroidView(factory = { context ->
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        TextInputLayout(context).apply {
            id = if (output) R.id.fullPulseATIL else R.id.shortPulseATIL
            typeface = font
            addView(FixedTextInputEditText(context).apply {
                id = if (output) R.id.fullPulseA else R.id.fifteenPulseA
                layoutParams = android.widget.LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                hint = context.getString(if (output) R.string.fullPulse else R.string.pulse)
                inputType = if (output) InputType.TYPE_NULL else InputType.TYPE_CLASS_NUMBER
                isEnabled = !output
                typeface = font
                if (!output) {
                    filters = arrayOf(InputFilter.LengthFilter(2))
                    imeOptions = EditorInfo.IME_ACTION_DONE
                }
                setText(value)
                if (!output) doAfterTextChanged { change(it.toString()) }
            })
        }
    }, update = {
        if (it.editText?.text.toString() != value) it.editText?.setText(value)
        it.error = if (invalid) it.context.getString(R.string.err_pulse) else null
        it.isErrorEnabled = invalid
        // Material 1.14 adds caption top padding absent from the published native field.
        val caption = it.findViewById<android.view.View>(com.google.android.material.R.id.textinput_error)
        ((caption?.parent as? android.view.View)?.parent as? android.view.View)?.let { area ->
            area.setPaddingRelative(area.paddingStart, 0, area.paddingEnd, area.paddingBottom)
        }
    }, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PulseText(value: String, size: Float, modifier: Modifier, viewId: Int = android.view.View.NO_ID,
    color: Int = android.graphics.Color.WHITE) {
    AndroidView(factory = { context ->
        AppCompatTextView(context).apply {
            id = viewId
            textSize = size
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            gravity = Gravity.CENTER
            setTextColor(color)
        }
    }, update = { it.text = value }, modifier = modifier)
}

@Preview(name = "English", locale = "en", widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Hebrew", locale = "he", widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Hebrew large", locale = "he", fontScale = 1.5f, widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Landscape", locale = "en", widthDp = 720, heightDp = 330, showBackground = true)
@Composable
private fun PulsePreview() = PulseScreen("", "", false, 15, {}, {})
