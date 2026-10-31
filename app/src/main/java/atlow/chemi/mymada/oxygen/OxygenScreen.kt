package atlow.chemi.mymada.oxygen

import android.graphics.Typeface
import android.content.res.Configuration
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.FixedTextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Compose owns layout and saved input; native widgets preserve published typography/decoration. */
@Composable
internal fun OxygenScreen() {
    var atm by rememberSaveable { mutableStateOf("100") }
    var psi by rememberSaveable { mutableStateOf("1500") }
    var large by rememberSaveable { mutableStateOf(false) }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val margin = if (landscape) 8.dp else 16.dp
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val inputs: @Composable ColumnScope.() -> Unit = {
            AndroidView(factory = { context ->
                (LayoutInflater.from(context).inflate(R.layout.oxygen_tank_selector, null) as Spinner).apply {
                    id = R.id.OxyTypeS
                    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item,
                        resources.getStringArray(R.array.oxySizes)).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, row: Long) {
                            large = position == 1
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }
                }
            }, update = { it.setSelection(if (large) 1 else 0) },
                modifier = Modifier.padding(start = margin, end = margin, top = if (landscape) 8.dp else 10.dp).fillMaxWidth().height(54.dp))
            PressureInput(atm, R.id.ATM, R.string.atm, OxygenCalculation.MAX_ATM,
                Modifier.padding(start = margin, end = margin, top = if (landscape) 8.dp else 0.dp)) {
                atm = it
                psi = OxygenCalculation.psiFromAtm(it).orEmpty()
            }
            PressureInput(psi, R.id.PSI, R.string.psi, OxygenCalculation.MAX_PSI,
                Modifier.padding(start = margin, end = margin, top = 10.dp)) {
                psi = it
                atm = OxygenCalculation.atmFromPsi(it).orEmpty()
            }
        }
        val result: @Composable () -> Unit = {
                OxygenLabel(OxygenCalculation.minutes(atm, large).toString(), 50f, android.graphics.Color.WHITE,
                    Modifier.padding(top = 15.dp), R.id.txtOxyMin)
                OxygenLabel(stringResource(R.string.oxyLeft), 25f, android.graphics.Color.WHITE, Modifier)
                OxygenLabel(stringResource(R.string.lpmExp), 15f, 0xffd6d6d6.toInt(), Modifier.padding(bottom = 10.dp))
        }
        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(.64f).verticalScroll(rememberScrollState()), content = inputs)
                Box(Modifier.weight(.36f).fillMaxHeight().background(colorResource(R.color.buttonMaterialDark)),
                    contentAlignment = Alignment.Center) {
                    Column(Modifier.verticalScroll(rememberScrollState())) { result() }
                }
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight)) {
                inputs()
                Spacer(Modifier.weight(1f))
                Column(Modifier.fillMaxWidth().background(colorResource(R.color.buttonMaterialDark))) { result() }
            }
        }
    }
}

@Composable
private fun PressureInput(value: String, viewId: Int, label: Int, maximum: Int, modifier: Modifier,
                          onChange: (String) -> Unit) {
    val currentChange by rememberUpdatedState(onChange)
    AndroidView(factory = { context ->
        TextInputLayout(context).apply {
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            addView(FixedTextInputEditText(context).apply {
                id = viewId
                isSaveEnabled = false
                layoutParams = LinearLayout.LayoutParams(-1, -2)
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
                    val proposed = dest.replaceRange(dstart, dend, source.subSequence(start, end)).toString()
                    if (OxygenCalculation.validInput(proposed, maximum)) {
                        error = null
                        null
                    } else {
                        error = context.getString(R.string.oxygen_pressure_range, maximum)
                        dest.subSequence(dstart, dend)
                    }
                })
                hint = context.getString(label)
                typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
                doAfterTextChanged { text ->
                    if (hasFocus() && OxygenCalculation.validInput(text.toString(), maximum)) currentChange(text.toString())
                }
            })
        }
    }, update = {
        val edit = it.editText!!
        if (edit.text.toString() != value) {
            edit.setText(value)
            edit.setSelection(value.length)
        }
    }, modifier = modifier.fillMaxWidth())
}

@Composable
private fun OxygenLabel(value: String, size: Float, color: Int, modifier: Modifier, viewId: Int = View.NO_ID) {
    AndroidView(factory = { context -> TextView(context).apply {
        id = viewId
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_PX, (size * resources.displayMetrics.scaledDensity + 0.5f).toInt().toFloat())
        setTextColor(color)
        typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
    } }, update = { it.text = value }, modifier = modifier.fillMaxWidth())
}

@Preview(name = "English", locale = "en", widthDp = 411, heightDp = 650, showBackground = true)
@Preview(name = "Hebrew large", locale = "he", fontScale = 1.5f, widthDp = 411, heightDp = 650, showBackground = true)
@Preview(name = "Landscape", locale = "en", widthDp = 720, heightDp = 330, showBackground = true)
@Composable
private fun OxygenPreview() = OxygenScreen()
