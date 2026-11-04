package atlow.chemi.mymada.integrations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import atlow.chemi.mymada.R

@Composable
fun TeamMemberScreen(name: String, updateName: (String) -> Unit, role: Int, updateRole: (Int) -> Unit,
    invalid: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    val font = FontFamily(Font(R.font.opensans_hebrew))
    val direction = LocalLayoutDirection.current
    val largeFont = LocalDensity.current.fontScale > 1f
    // Compose backports nonlinear scaling; match this device's native TextView conversion.
    val titlePx = android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,
        20f, LocalContext.current.resources.displayMetrics).roundToInt().toFloat()
    val titleSize = with(LocalDensity.current) { titlePx.toSp() }
    // Native FontsOverride also supplied the alert title and floating label, not just the input.
    val typography = MaterialTheme.typography
    MaterialTheme(typography = typography.copy(bodyLarge = typography.bodyLarge.copy(fontFamily = font))) {
    IntegrationDialog(stringResource(R.string.Nteam), modifier = Modifier.testTag("member-dialog"),
        titleStyle = TextStyle(fontFamily = font, fontSize = titleSize,
            platformStyle = PlatformTextStyle(includeFontPadding = false)), buttons = {
        TextButton(onClick = onCancel, modifier = Modifier.testTag("member-cancel")) {
            Text(stringResource(R.string.ret).uppercase(), color = colorResource(R.color.OldPrimary), fontFamily = font)
        }
        TextButton(onClick = onSave, modifier = Modifier.testTag("member-save")) {
            Text(stringResource(R.string.ok), fontFamily = font)
        }
    }) {
        val nameField: @Composable (Modifier) -> Unit = { modifier ->
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                IntegrationField(name, updateName, label = stringResource(R.string.teamN),
                    error = if (invalid) stringResource(R.string.emailNameNone) else null,
                    modifier = modifier.testTag("member-name"))
            }
        }
        val roleField: @Composable (Modifier) -> Unit = { modifier ->
            AndroidView(modifier = modifier.height(54.dp).testTag("member-role"), factory = { context ->
                AppCompatSpinner(ContextThemeWrapper(context, R.style.TeamSpinnerTheme), null,
                    androidx.appcompat.R.attr.spinnerStyle, Spinner.MODE_DROPDOWN).apply {
                    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, TeamContract.roles).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    setSelection(role)
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = updateRole(position)
                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }
                }
            }, update = { if (it.selectedItemPosition != role) it.setSelection(role) })
        }
        // Native places the name on the left in both locales; large text retains a stacked form.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            // IntegrationDialog already supplies 8dp below the title; native custom content adds 10dp total.
            if (largeFont) Column(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 15.dp)) {
                nameField(Modifier.fillMaxWidth())
                roleField(Modifier.fillMaxWidth())
            } else Row(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 15.dp)) {
                nameField(Modifier.weight(1f))
                roleField(Modifier.weight(1f))
            }
        }
    }
    }
}
