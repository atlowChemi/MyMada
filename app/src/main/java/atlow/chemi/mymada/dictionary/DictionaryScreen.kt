package atlow.chemi.mymada.dictionary

import android.content.DialogInterface
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import atlow.chemi.mymada.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout

@Composable
internal fun DictionaryScreen(model: DictionaryModel, query: String, addRequest: Int) {
    // Strings keep editor/details restorable without a parcelable document schema.
    var key by rememberSaveable { mutableStateOf("") }
    var originalName by rememberSaveable { mutableStateOf("") }
    var originalDefinition by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var definition by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf("") }
    var handledAdd by remember { mutableIntStateOf(addRequest) }
    val view = LocalView.current
    LaunchedEffect(addRequest) {
        if (handledAdd != addRequest) {
            handledAdd = addRequest
            model.clearWriteError()
            key = model.newKey()
            name = ""; definition = ""; mode = "add"
        }
    }
    LaunchedEffect(model.success) {
        if (model.success != null) {
            val edited = model.submittedEdit
            if (model.success == key) mode = ""
            Snackbar.make(view, if (edited) R.string.successEditWord else R.string.successAddWord, Snackbar.LENGTH_LONG)
                .setTextColor(android.graphics.Color.WHITE).show()
            model.consumeSuccess()
        }
    }
    MaterialTheme {
        Column(Modifier.fillMaxSize()) {
            val status = when {
                model.readError -> R.string.dictionary_read_error
                model.loading -> R.string.dictionary_loading
                model.cached -> R.string.dictionary_cached
                else -> null
            }
            if (status != null) Text(stringResource(status), Modifier.padding(8.dp), color = Color.Black)
            if (model.readError) TextButton(onClick = model::retry) { Text(stringResource(R.string.dictionary_retry)) }
            LazyColumn(Modifier.weight(1f)) {
                items(dictionarySearch(model.entries, query), key = { it.key }) { entry ->
                    Column(Modifier.fillMaxWidth().background(Color.White).clickable {
                        model.clearWriteError()
                        key = entry.key; originalName = entry.name; originalDefinition = entry.definition
                        name = entry.name; definition = entry.definition; mode = "details"
                    }.semantics(mergeDescendants = true) { text = AnnotatedString(entry.name) }) {
                        AndroidView(factory = { context ->
                            AppCompatTextView(context).apply {
                                textSize = 16f
                                typeface = Typeface.DEFAULT_BOLD
                                setTextColor(android.graphics.Color.BLACK)
                                gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
                                importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                            }
                        }, update = { it.text = entry.name }, modifier = Modifier.fillMaxWidth()
                            .padding(start = 32.dp, end = 24.dp, top = 8.dp).heightIn(min = 48.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colorResource(R.color.GreyNav)))
                    }
                }
            }
        }
        if (mode == "details") DictionaryDetails(originalName, originalDefinition, { mode = "edit" }, { mode = "" })
        if (mode == "add" || mode == "edit") DictionaryEditor(mode == "edit", name, definition,
            originalName, originalDefinition, model.sending, model.writeError,
            { name = it }, { definition = it }, { mode = "" }) {
            model.submit(key, if (mode == "edit") DictionaryEntry(key, originalName, originalDefinition) else null, name, definition)
        }
    }
}

/** Native rendering retains published geometry/fonts; Compose owns saved state and lifetime. */
@Composable
private fun DictionaryDetails(name: String, definition: String, edit: () -> Unit, dismiss: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(name, definition) {
        val dialog = AlertDialog.Builder(context, R.style.AlertDialogStyle).setTitle(name).setMessage(definition)
            .setPositiveButton(R.string.editDictionary) { _, _ -> edit() }
            .setNeutralButton("סגור") { _, _ -> dismiss() }.create()
        dialog.setOnCancelListener { dismiss() }
        dialog.show()
        dialog.nativeDictionaryTitleAndButtons()
        dialog.findViewById<TextView>(android.R.id.message)?.typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        onDispose { dialog.dismiss() }
    }
}

@Composable
private fun DictionaryEditor(edit: Boolean, name: String, definition: String, originalName: String,
    originalDefinition: String, sending: Boolean, failed: Boolean, onName: (String) -> Unit,
    onDefinition: (String) -> Unit, dismiss: () -> Unit, submit: () -> Unit) {
    val context = LocalContext.current
    val currentSubmit by rememberUpdatedState(submit)
    val currentDismiss by rememberUpdatedState(dismiss)
    val pendingText = stringResource(R.string.dictionary_pending)
    val failureText = stringResource(R.string.dictionary_write_error)
    var dialog by remember { mutableStateOf<AlertDialog?>(null) }
    DisposableEffect(Unit) {
        val content = LayoutInflater.from(context).inflate(R.layout.new_word_dialog, null)
        val term = content.findViewById<EditText>(R.id.wordName)
        val meaning = content.findViewById<EditText>(R.id.wordDefinition)
        val termLayout = content.findViewById<TextInputLayout>(R.id.nameTil)
        val meaningLayout = content.findViewById<TextInputLayout>(R.id.definitionTil)
        // Native FontsOverride replaced DEFAULT/MONOSPACE with this asset globally.
        val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
        term.typeface = font
        meaning.typeface = font
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            term.setLocalePreferredLineHeightForMinimumUsed(false)
            meaning.setLocalePreferredLineHeightForMinimumUsed(false)
        }
        termLayout.typeface = font
        meaningLayout.typeface = font
        term.setText(name); meaning.setText(definition)
        term.doAfterTextChanged { onName(it.toString()) }
        meaning.doAfterTextChanged { onDefinition(it.toString()) }
        val alert = AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(if (edit) "עריכת מילה" else "הוספת מילה").setView(content)
            .setNegativeButton("ביטול") { _, _ -> currentDismiss() }
            .setPositiveButton(if (edit) "שמור" else "הוסף", null).create()
        alert.setOnCancelListener { currentDismiss() }
        alert.show()
        alert.nativeDictionaryTitleAndButtons()
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val n = term.text.toString().trim(); val d = meaning.text.toString().trim()
            val unchanged = edit && n == originalName && d == originalDefinition
            termLayout.error = when { n.isEmpty() -> "לא כתבת מונח"; unchanged -> "לא שינית את שם המונח"; else -> null }
            meaningLayout.error = when { d.isEmpty() -> "לא כתבת הגדרה למונח!"; unchanged -> "לא שינית את ההגדרה למונח!"; else -> null }
            for (layout in listOf(termLayout, meaningLayout)) {
                layout.isErrorEnabled = layout.error != null
                // Material's new caption top inset was absent from the published dialog.
                val caption = layout.findViewById<TextView>(com.google.android.material.R.id.textinput_error)
                // Native error TextAppearance uses named sans-serif, not the globally replaced defaults.
                caption?.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                layout.isExpandedHintEnabled = layout.error == null
                ((caption?.parent as? android.view.View)?.parent as? android.view.View)?.let { area ->
                    area.setPaddingRelative(area.paddingStart, 0, area.paddingEnd, area.paddingBottom)
                }
            }
            if (n.isNotEmpty() && d.isNotEmpty() && !unchanged) currentSubmit()
        }
        dialog = alert
        onDispose { alert.dismiss(); dialog = null }
    }
    SideEffect {
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = !sending
        dialog?.findViewById<EditText>(R.id.wordName)?.isEnabled = !sending
        dialog?.findViewById<EditText>(R.id.wordDefinition)?.isEnabled = !sending
        dialog?.findViewById<TextInputLayout>(R.id.definitionTil)?.helperText = when {
            sending -> pendingText
            failed -> failureText
            else -> null
        }
    }
}

private fun AlertDialog.nativeDictionaryTitleAndButtons() {
    // Keep the historical dialog typeface local; shared dialogs have separate acceptance.
    val font = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
    findViewById<TextView>(androidx.appcompat.R.id.alertTitle)?.typeface = font
    // Reuse the Contractions correction: target35's screen-height qualifiers include system bars.
    val legacyMinimum = if (android.os.Build.VERSION.SDK_INT >= 35) {
        val metrics = context.getSystemService(android.view.WindowManager::class.java).currentWindowMetrics
        val bars = metrics.windowInsets.getInsetsIgnoringVisibility(
            android.view.WindowInsets.Type.systemBars() or android.view.WindowInsets.Type.displayCutout())
        val legacy = android.content.res.Configuration(context.resources.configuration).apply {
            screenHeightDp = ((metrics.bounds.height() - bars.top - bars.bottom) / context.resources.displayMetrics.density).toInt()
        }
        val theme = androidx.appcompat.view.ContextThemeWrapper(context.createConfigurationContext(legacy), R.style.AlertDialogStyle)
        val attributes = theme.obtainStyledAttributes(null, intArrayOf(android.R.attr.minHeight), androidx.appcompat.R.attr.buttonBarButtonStyle, 0)
        val minimum = attributes.getDimensionPixelSize(0, getButton(DialogInterface.BUTTON_POSITIVE).minHeight)
        attributes.recycle()
        minimum
    } else null
    for (button in listOf(DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE, DialogInterface.BUTTON_NEUTRAL))
        getButton(button)?.apply {
            typeface = font
            legacyMinimum?.let { minHeight = it; minimumHeight = it }
        }
}
