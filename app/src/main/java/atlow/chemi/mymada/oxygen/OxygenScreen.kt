package atlow.chemi.mymada.oxygen

import android.graphics.Typeface
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(rememberScrollState()).heightIn(min = maxHeight)) {
            AndroidView(factory = { context ->
                Spinner(context, null, androidx.appcompat.R.attr.spinnerStyle).apply {
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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp).fillMaxWidth().height(54.dp))
            PressureInput(atm, R.id.ATM, R.string.atm, 3, Modifier.padding(horizontal = 16.dp)) {
                atm = it
                OxygenCalculation.psiFromAtm(it)?.let { converted -> psi = converted }
            }
            PressureInput(psi, R.id.PSI, R.string.psi, 5,
                Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp)) {
                psi = it
                OxygenCalculation.atmFromPsi(it)?.let { converted -> atm = converted }
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().background(colorResource(R.color.buttonMaterialDark))) {
                OxygenLabel(OxygenCalculation.minutes(atm, large).toString(), 50f, android.graphics.Color.WHITE,
                    Modifier.padding(top = 15.dp), R.id.txtOxyMin)
                OxygenLabel(stringResource(R.string.oxyLeft), 25f, android.graphics.Color.WHITE, Modifier)
                OxygenLabel(stringResource(R.string.lpmExp), 15f, 0xffd6d6d6.toInt(), Modifier.padding(bottom = 10.dp))
            }
        }
    }
}

@Composable
private fun PressureInput(value: String, viewId: Int, label: Int, digits: Int, modifier: Modifier,
                          onChange: (String) -> Unit) {
    val currentChange by rememberUpdatedState(onChange)
    AndroidView(factory = { context ->
        TextInputLayout(context).apply {
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            addView(FixedTextInputEditText(context).apply {
                id = viewId
                isSaveEnabled = false
                layoutParams = ViewGroup.LayoutParams(-1, -2)
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(InputFilter.LengthFilter(digits))
                hint = context.getString(label)
                typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
                doAfterTextChanged { text ->
                    if (hasFocus() && OxygenCalculation.validInput(text.toString(), digits)) currentChange(text.toString())
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
        textSize = size
        setTextColor(color)
        typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
    } }, update = { it.text = value }, modifier = modifier.fillMaxWidth())
}

@Preview(name = "English", locale = "en", widthDp = 411, heightDp = 650, showBackground = true)
@Preview(name = "Hebrew large", locale = "he", fontScale = 1.5f, widthDp = 411, heightDp = 650, showBackground = true)
@Composable
private fun OxygenPreview() = OxygenScreen()
