package atlow.chemi.mymada.tools.glasgow

import android.content.res.Configuration
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import atlow.chemi.mymada.R
import atlow.chemi.mymada.classes.AnimatedColor
import kotlin.math.cos
import kotlin.math.roundToInt

internal val glasgowFields = listOf(
    R.string.eyeOpening to R.array.eyes, R.string.verbal to R.array.verb, R.string.motor to R.array.moto,
)

@Composable
internal fun GlasgowScreen() {
    var choices by rememberSaveable { mutableStateOf(IntArray(3)) }
    val score = glasgowScore(choices)
    val band = glasgowBand(score)
    var color by remember { mutableIntStateOf(band.color) }
    LaunchedEffect(band) {
        val transition = AnimatedColor(color, band.color)
        // Published native HSV transition and AccelerateDecelerateInterpolator, lifecycle-cancelled.
        animate(0f, 1f, animationSpec = tween(1000, easing = { (1 - cos(Math.PI * it)).toFloat() / 2 })) { value, _ ->
            color = transition.with(value)
        }
    }
    val configuration = LocalConfiguration.current
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val options: @Composable (Modifier) -> Unit = { modifier ->
        Column(if (configuration.fontScale > 1.2f && !landscape) modifier else modifier.verticalScroll(rememberScrollState())) {
            glasgowFields.forEachIndexed { index, (label, array) ->
                val title = stringResource(label)
                GlasgowText(title, 21f, Modifier.fillMaxWidth().padding(top = if (index == 1) 4.dp else 0.dp),
                    bold = true, centered = true, color = R.color.ColorAqua)
                GlasgowChoice(index, title, stringArrayResource(array), choices[index]) { value ->
                    choices = choices.copyOf().also { it[index] = value }
                }
            }
        }
    }
    val result: @Composable () -> Unit = {
        val description = stringResource(when (band) {
            GlasgowBand.DEEP -> R.string.GlasgowHigh
            GlasgowBand.MODERATE -> R.string.GlasgowMed
            GlasgowBand.LIGHT -> R.string.GlasgowLow
        })
        GlasgowText(stringResource(R.string.Glasgow, "$score ($description)"), 19f,
            Modifier.fillMaxWidth().testTag("glasgow-score").background(Color(color), RoundedCornerShape(15.dp)).padding(14.dp),
            announce = true)
    }
    // The existing host owns toolbar/system insets and window artwork.
    if (landscape) {
        Row(Modifier.fillMaxSize().padding(16.dp)) {
            options(Modifier.weight(.64f).fillMaxHeight().padding(start = 8.dp, top = 8.dp, bottom = 8.dp))
            Column(Modifier.weight(.36f).fillMaxHeight().padding(8.dp)) {
                result()
                Spacer(Modifier.height(8.dp))
                GlasgowInfo(Modifier.weight(1f))
            }
        }
    } else if (configuration.fontScale > 1.2f) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            options(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            result()
            Spacer(Modifier.height(8.dp))
            GlasgowInfo()
        }
    } else {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            options(Modifier.weight(1f).fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            result()
            Spacer(Modifier.height(8.dp))
            GlasgowInfo()
        }
    }
}

@Composable
private fun GlasgowChoice(index: Int, title: String, options: Array<String>, selected: Int, onSelect: (Int) -> Unit) {
    val selection by rememberUpdatedState(onSelect)
    AndroidView(factory = { context ->
        AppCompatSpinner(ContextThemeWrapper(context, R.style.GlasgowSpinnerTheme), null,
            androidx.appcompat.R.attr.spinnerStyle, Spinner.MODE_DROPDOWN).apply {
            tag = "glasgow-choice-$index"
            contentDescription = title
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            setSelection(selected)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = selection(position)
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
    }, update = { if (it.selectedItemPosition != selected) it.setSelection(selected) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag("glasgow-choice-$index"))
}

@Composable
private fun GlasgowInfo(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(15.dp))
        .padding(start = 7.dp, end = 14.dp, top = 14.dp, bottom = 14.dp).height(IntrinsicSize.Min)) {
        Image(painterResource(R.drawable.ic_info), contentDescription = null,
            colorFilter = ColorFilter.tint(Color.Black),
            modifier = Modifier.padding(horizontal = 2.dp).size(24.dp))
        Box(Modifier.width(1.dp).fillMaxHeight().background(Color.Black))
        Spacer(Modifier.width(10.dp))
        GlasgowText(stringResource(R.string.GlasgowInfo), 16f, Modifier.weight(1f).verticalScroll(rememberScrollState()))
    }
}

/** Native text metrics and the shipped font are part of the published appearance. */
@Composable
private fun GlasgowText(text: String, size: Float, modifier: Modifier, bold: Boolean = false,
    centered: Boolean = false, color: Int = android.R.color.black, announce: Boolean = false) {
    AndroidView(factory = { context ->
        AppCompatTextView(context).apply {
            // XML TextView textSize uses getDimensionPixelSize (whole pixels), not float SP.
            setTextSize(TypedValue.COMPLEX_UNIT_PX,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, resources.displayMetrics).roundToInt().toFloat())
            setTypeface(Typeface.createFromAsset(context.assets, "fonts/opensanshebrew.ttf"),
                if (bold) Typeface.BOLD else Typeface.NORMAL)
            gravity = if (centered) Gravity.CENTER else Gravity.START or Gravity.TOP
            setTextColor(context.getColor(color))
            if (announce) accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
    }, update = { it.text = text }, modifier = modifier)
}

@Preview(name = "English", locale = "en", widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Hebrew", locale = "he", widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Hebrew large text", locale = "he", fontScale = 1.5f, widthDp = 411, heightDp = 600, showBackground = true)
@Preview(name = "Landscape", locale = "en", widthDp = 720, heightDp = 330, showBackground = true)
@Composable
private fun GlasgowPreview() = GlasgowScreen()
