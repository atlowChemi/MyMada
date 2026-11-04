package atlow.chemi.mymada.settings

import android.content.SharedPreferences
import android.os.Build
import android.text.BoringLayout
import android.util.TypedValue
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import atlow.chemi.mymada.R

internal fun Fragment.settingsContent(id: Int, content: @Composable () -> Unit) = ComposeView(requireContext()).apply {
    this.id = id
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent(content)
}

/** Native alert window/insets only; all content, choices and actions are Compose. */
internal fun DialogFragment.settingsDialog(id: Int, content: @Composable () -> Unit): android.app.Dialog =
    AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
        .setView(settingsContent(id, content)).create()

@Composable internal fun SharedPreferences.observeSettings(): Map<String, *> {
    var values by remember(this) { mutableStateOf(all) }
    DisposableEffect(this) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, _ -> values = prefs.all }
        registerOnSharedPreferenceChangeListener(listener)
        values = all
        onDispose { unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return values
}

@Composable internal fun SettingsText(text: String, size: Int, modifier: Modifier = Modifier, color: Color = Color.Black, minLines: Int = 1) {
    val pixels = with(LocalDensity.current) { size.sp.roundToPx().toFloat() }
    Box(modifier.semantics { this.text = AnnotatedString(text) }, propagateMinConstraints = true) {
        AndroidView(factory = { context ->
            object : AppCompatTextView(context) {
                override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        // Native12822 uses fallback spacing for StaticLayout, not BoringLayout.
                        // Open Sans Hebrew's ascent exceeds its top: enabling it for both adds
                        // 12px per English settings row at 420dpi. Keep wrapped/RTL fallback.
                        val boring = BoringLayout.isBoring(this.text, paint)
                        setFallbackLineSpacing(boring == null || (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED && boring.width >
                            MeasureSpec.getSize(widthMeasureSpec) - compoundPaddingLeft - compoundPaddingRight))
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
            }.apply {
                typeface = ResourcesCompat.getFont(context, R.font.opensans_hebrew)
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                if (Build.VERSION.SDK_INT >= 35) setLocalePreferredLineHeightForMinimumUsed(false)
            }
        }, update = {
            it.text = text
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixels)
            it.setTextColor(color.toArgb())
            it.minLines = minLines
        }, modifier = Modifier.clearAndSetSemantics {})
    }
}

@Composable internal fun SettingsList(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 30.dp), content = content)
}

@Composable internal fun SettingsHeading(title: Int) {
    SettingsText(stringResource(title), 14, Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 7.dp), colorResource(R.color.Ocean))
}

@Composable internal fun SettingsRow(tag: String, title: Int, description: String? = null,
    checked: Boolean? = null, enabled: Boolean = true, top: Int = 3, minLines: Int = 1,
    divider: Boolean = true, onClick: () -> Unit) {
    val interaction = if (checked != null) Modifier.toggleable(checked, enabled, Role.Switch) { onClick() }
        else Modifier.clickable(enabled = enabled, onClick = onClick)
    Column(Modifier.fillMaxWidth().padding(top = top.dp).alpha(if (enabled) 1f else .4f).testTag(tag).then(interaction)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = if (divider) 3.dp else 0.dp)) {
                val titlePadding = when (title) {
                    R.string.pref_title_volume -> PaddingValues(top = 14.dp, bottom = 20.dp)
                    R.string.pref_title_ringtone -> PaddingValues(vertical = 15.dp)
                    else -> PaddingValues(top = 14.dp, bottom = 16.dp)
                }
                SettingsText(stringResource(title), 21, Modifier.fillMaxWidth().then(if (description == null) Modifier.padding(titlePadding) else Modifier))
                if (description != null) SettingsText(description, 16, Modifier.fillMaxWidth().padding(top = 1.dp), minLines = minLines)
            }
            if (checked != null) {
                // Published CKS thumb/track/shadow; Compose owns the row and switch semantics.
                AndroidView(factory = { SwitchCompat(ContextThemeWrapper(it, R.style.CKS)).apply {
                    isClickable = false; isFocusable = false
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                } }, update = { it.isChecked = checked }, modifier = Modifier.padding(end = 8.dp, top = 8.dp, bottom = 8.dp))
            }
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colorResource(R.color.ColorBlueAlpha)))
    }
}

@Composable internal fun SettingsLink(tag: String, title: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 15.dp).heightIn(min = 60.dp)
        .shadow(7.dp, RoundedCornerShape(15.dp)).background(Color.White, RoundedCornerShape(15.dp))
        .testTag(tag).clickable(onClick = onClick).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        SettingsText(stringResource(title), 22, Modifier.weight(1f))
        SettingsText(">", 30)
    }
}

@Composable internal fun SettingsDialogContent(title: String? = null, cancel: () -> Unit,
    confirmLabel: Int? = null, confirm: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        if (title != null) SettingsText(title, 18, Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 8.dp))
        content()
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.widthIn(min = 64.dp).heightIn(min = 48.dp).testTag("dialog-cancel").clickable(onClick = cancel).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                SettingsText(stringResource(R.string.ret).uppercase(locale), 14, color = colorResource(R.color.ColorAqua))
            }
            if (confirmLabel != null) Box(Modifier.widthIn(min = 64.dp).heightIn(min = 48.dp).testTag("dialog-confirm").clickable(onClick = confirm).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                SettingsText(stringResource(confirmLabel).uppercase(locale), 14, color = colorResource(R.color.ColorAqua))
            }
        }
    }
}
