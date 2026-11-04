package atlow.chemi.mymada.integrations

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import atlow.chemi.mymada.R

/** Published TextInputLayout/EditText metrics without Material3's filled-field padding. */
@Composable
internal fun IntegrationField(value: String, update: (String) -> Unit, modifier: Modifier = Modifier,
    label: String? = null, enabled: Boolean = true, singleLine: Boolean = true, minLines: Int = 1,
    error: String? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = TextStyle(),
    inputMinHeight: androidx.compose.ui.unit.Dp = 41.dp,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val accent = colorResource(R.color.ColorAqua)
    val color = if (error != null) Color.Red else if (focused) accent else colorResource(R.color.ColorBlue)
    val style = TextStyle(fontFamily = FontFamily(Font(R.font.opensans_hebrew)), fontSize = 18.sp,
        color = Color.Black, letterSpacing = 0.sp).merge(textStyle)
    BasicTextField(value, update, enabled = enabled, singleLine = singleLine, minLines = minLines,
        textStyle = style, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        visualTransformation = visualTransformation, interactionSource = interaction, cursorBrush = SolidColor(accent),
        modifier = modifier.semantics { if (label != null) contentDescription = label; if (error != null) error(error) },
        decorationBox = { field -> Column {
        if (label != null) Box(Modifier.fillMaxWidth().heightIn(min = 11.dp).padding(horizontal = 4.dp)) {
            if (value.isNotEmpty() || focused) Text(label, color = color, fontSize = 12.sp, lineHeight = 14.sp)
        }
        Box(Modifier.fillMaxWidth()) {
            Spacer(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 4.dp)
                .padding(bottom = 8.dp).height(if (focused) 2.dp else 1.dp).background(color))
            Box(Modifier.fillMaxWidth().heightIn(min = inputMinHeight).padding(horizontal = 4.dp, vertical = 8.dp)) {
                if (value.isEmpty() && !focused && label != null) Text(label, style = style, color = accent)
                field()
            }
        }
        if (error != null) Text(error, color = Color.Red, fontSize = 12.sp)
    } })
}
