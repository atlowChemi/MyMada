package atlow.chemi.mymada.integrations

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.InputType
import android.text.BoringLayout
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.doOnLayout
import atlow.chemi.mymada.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Keep native floating-label, error-caption and multiline measurement together. */
@Composable
internal fun IntegrationField(value: String, update: (String) -> Unit, modifier: Modifier = Modifier,
    label: String? = null, enabled: Boolean = true, singleLine: Boolean = true, minLines: Int = 1,
    error: String? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = TextStyle(),
    inputMinHeight: androidx.compose.ui.unit.Dp = 41.dp,
    onClick: (() -> Unit)? = null,
) {
    // Activation's four unlabelled password cells have no TextInputLayout/error decoration.
    if (label == null) {
        val interaction = remember { MutableInteractionSource() }
        val focused by interaction.collectIsFocusedAsState()
        val accent = colorResource(R.color.ColorAqua)
        val color = if (focused) accent else colorResource(R.color.ColorBlue)
        BasicTextField(value, update, modifier = modifier, enabled = enabled, singleLine = singleLine,
            textStyle = TextStyle(fontFamily = FontFamily(Font(R.font.opensans_hebrew)), fontSize = 18.sp,
                color = androidx.compose.ui.graphics.Color.Black, letterSpacing = 0.sp).merge(textStyle),
            keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
            visualTransformation = visualTransformation, interactionSource = interaction, cursorBrush = SolidColor(accent),
            decorationBox = { field -> Box(Modifier.fillMaxWidth()) {
                Spacer(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 4.dp)
                    .padding(bottom = 8.dp).height(if (focused) 2.dp else 1.dp).background(color))
                Box(Modifier.fillMaxWidth().heightIn(min = inputMinHeight).padding(horizontal = 4.dp, vertical = 8.dp)) { field() }
            } })
        return
    }
    val change by rememberUpdatedState(update)
    val click by rememberUpdatedState(onClick)
    val actions by rememberUpdatedState(keyboardActions)
    val currentValue by rememberUpdatedState(value)
    var editor by remember { mutableStateOf<TextInputEditText?>(null) }
    var editRevision by remember { mutableIntStateOf(0) }
    AndroidView(modifier = modifier.semantics {
        if (label != null) contentDescription = label
        this[SemanticsProperties.Text] = listOfNotNull(label, error).map(::AnnotatedString)
        editableText = visualTransformation.filter(AnnotatedString(value)).text
        inputText = AnnotatedString(value)
        if (error != null) error(error)
        if (!enabled) disabled()
        if (visualTransformation is PasswordVisualTransformation) password()
        setText { if (enabled && onClick == null) { change(it.text); true } else false }
        insertTextAtCursor {
            if (enabled && onClick == null) {
                editor?.let { view -> view.text?.replace(view.selectionStart.coerceAtLeast(0), view.selectionEnd.coerceAtLeast(0), it.text) }
                true
            } else false
        }
        requestFocus { enabled && editor?.requestFocus() == true }
        onClick { if (enabled) { if (click != null) click?.invoke() else editor?.requestFocus(); true } else false }
    }, factory = { context ->
        TextInputLayout(context).apply {
            isHintAnimationEnabled = false
            val font = if (textStyle.fontFamily == FontFamily.SansSerif) Typeface.SANS_SERIF
                else ResourcesCompat.getFont(context, R.font.opensans_hebrew)
            typeface = font
            addView(TextInputEditText(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                inputType = when (keyboardOptions.keyboardType) {
                    KeyboardType.Email -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    KeyboardType.Number -> InputType.TYPE_CLASS_NUMBER
                    KeyboardType.Decimal -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    KeyboardType.NumberPassword -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    else -> InputType.TYPE_CLASS_TEXT or if (singleLine) 0 else InputType.TYPE_TEXT_FLAG_MULTI_LINE
                }
                setSingleLine(singleLine)
                if (!singleLine) this.minLines = minLines
                this.typeface = font
                // Published target29 sizes minLines from this font, not API35's locale minimum.
                if (Build.VERSION.SDK_INT >= 35) setLocalePreferredLineHeightForMinimumUsed(false)
                setTextColor(Color.BLACK)
                gravity = Gravity.TOP or if (textStyle.textAlign == TextAlign.Center) Gravity.CENTER_HORIZONTAL else Gravity.START
                if (inputMinHeight != 41.dp) minimumHeight = (inputMinHeight.value * resources.displayMetrics.density).toInt()
                if (visualTransformation is PasswordVisualTransformation) transformationMethod = PasswordTransformationMethod.getInstance()
                imeOptions = when (keyboardOptions.imeAction) {
                    ImeAction.Next -> EditorInfo.IME_ACTION_NEXT
                    ImeAction.Done -> EditorInfo.IME_ACTION_DONE
                    else -> EditorInfo.IME_ACTION_UNSPECIFIED
                }
                isFocusable = onClick == null
                isFocusableInTouchMode = onClick == null
                if (onClick != null) keyListener = null
                setOnClickListener { click?.invoke() }
                doAfterTextChanged {
                    if (it.toString() != currentValue) {
                        change(it.toString())
                        // Reconcile even when a controlled caller rejects an edit (e.g. ambulance length).
                        editRevision++
                    }
                }
                setOnEditorActionListener { _, action, _ ->
                    val callback = when (action) {
                        EditorInfo.IME_ACTION_NEXT -> actions.onNext
                        EditorInfo.IME_ACTION_DONE -> actions.onDone
                        else -> null
                    }
                    if (callback != null) {
                        callback.invoke(object : KeyboardActionScope {
                            override fun defaultKeyboardAction(imeAction: ImeAction) {
                                if (imeAction == ImeAction.Next) focusSearch(android.view.View.FOCUS_FORWARD)?.requestFocus()
                                else (context.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager)
                                    .hideSoftInputFromWindow(windowToken, 0)
                            }
                        })
                        true
                    } else false
                }
                editor = this
            })
        }
    }, update = { layout ->
        @Suppress("UNUSED_VARIABLE") val revision = editRevision
        layout.isHintEnabled = label != null
        layout.hint = label
        layout.isExpandedHintEnabled = error == null
        layout.isEnabled = enabled
        layout.isErrorEnabled = error != null
        layout.error = error
        layout.findViewById<android.widget.TextView>(com.google.android.material.R.id.textinput_error)?.let { caption ->
            if (Build.VERSION.SDK_INT >= 35) caption.setLocalePreferredLineHeightForMinimumUsed(false)
            // Material 1.0 (published native) aligns indicators to the editor with no extra top gap.
            // 1.14 adds a caption gap, including a larger font-scale-specific one.
            (caption.parent.parent as android.view.View).setPaddingRelative(
                layout.editText!!.paddingStart, 0, layout.editText!!.paddingEnd, 0)
            if (Build.VERSION.SDK_INT >= 28) caption.doOnLayout {
                // Target29 used fallback spacing for wrapped/RTL text, but not BoringLayout.
                val boring = BoringLayout.isBoring(caption.text, caption.paint)
                caption.setFallbackLineSpacing(boring == null || boring.width >
                    caption.width - caption.compoundPaddingLeft - caption.compoundPaddingRight)
            }
        }
        layout.editText!!.let {
            if (it.text.toString() != value) {
                it.setText(value)
                it.setSelection(value.length)
            }
        }
    })
}
