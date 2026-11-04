package atlow.chemi.mymada.settings

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import atlow.chemi.mymada.R

internal val toolOptions = listOf(
    "contrac" to R.string.button_f, "pulse" to R.string.button_c, "metro" to R.string.button_d,
    "vital" to R.string.button_e, "oxy" to R.string.button_g, "apgar" to R.string.button_ag,
    "glaz" to R.string.button_gl, "dict" to R.string.button_dict, "prot" to R.string.button_p,
)

@Composable
internal fun ToolSettingsScreen(selected: Set<String>, onToggle: (String) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 30.dp)) {
        NativeSettingsLabel(stringResource(R.string.pref_description_tools), 14f, R.color.Ocean,
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 7.dp))
        toolOptions.forEachIndexed { index, (key, label) ->
            val title = stringResource(label)
            Column(Modifier.padding(top = if (index == 0) 8.dp else 3.dp)) {
                Row(Modifier.fillMaxWidth().heightIn(min = 59.dp)
                    .semantics(mergeDescendants = true) { text = AnnotatedString(title) }
                    .toggleable(key in selected, role = Role.Switch, onValueChange = { onToggle(key) }),
                    verticalAlignment = Alignment.CenterVertically) {
                    NativeSettingsLabel(title, 21f, android.R.color.black,
                        Modifier.weight(1f).padding(start = 8.dp, end = 8.dp, top = 2.dp), accessible = false)
                    // Keep the published thumb/track shape, tint and shadow; Compose owns toggle semantics.
                    AndroidView(factory = { context ->
                        SwitchCompat(ContextThemeWrapper(context, R.style.CKS)).apply {
                            layoutParams = ViewGroup.LayoutParams(-2, -2)
                            isClickable = false
                            isFocusable = false
                            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                        }
                    }, update = { it.isChecked = key in selected },
                        modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp))
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colorResource(R.color.ColorBlueAlpha)))
            }
        }
    }
}

@Composable
private fun NativeSettingsLabel(label: String, size: Float, color: Int, modifier: Modifier, accessible: Boolean = true) {
    AndroidView(factory = { context ->
        object : AppCompatTextView(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    val boring = android.text.BoringLayout.isBoring(text, paint)
                    setFallbackLineSpacing(boring == null || (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED &&
                        boring.width > MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight))
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }.apply {
            // XML sp dimensions round to pixels before TextView measures the native labels.
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, kotlin.math.round(
                android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP, size, resources.displayMetrics)))
            typeface = Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf")
            // Native target29 does not add target35 locale minimums to these label line boxes.
            if (android.os.Build.VERSION.SDK_INT >= 35) setLocalePreferredLineHeightForMinimumUsed(false)
            setTextColor(androidx.core.content.ContextCompat.getColor(context, color))
            importantForAccessibility = if (accessible) View.IMPORTANT_FOR_ACCESSIBILITY_YES else View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }, update = { it.text = label }, modifier = modifier)
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Composable
private fun ToolSettingsPreview() = ToolSettingsScreen(toolOptions.map { it.first }.toSet()) { }
