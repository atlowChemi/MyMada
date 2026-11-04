package atlow.chemi.mymada.home

import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.cardview.widget.CardView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** Compose presentation backed by the Kotlin launcher and native platform actions. */
object HomeContent {
    @JvmStatic fun install(activity: HomePage) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        val actions = HomeActions(activity)
        val preferences = activity.getSharedPreferences("Settings", 0)
        val menu = PopupMenu(activity, View(activity)).menu
        activity.onCreateOptionsMenu(menu)
        val detection = menu.findItem(R.id.one)
        activity.setContent {
            var detecting by remember { mutableStateOf(detection.isChecked) }
            val banner = remember {
                // Pending banner requests can outlive destroy(), just like interstitial loads.
                AdView(activity.applicationContext).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    adUnitId = activity.getString(R.string.banner_ad_unit_id)
                    loadAd(AdRequest.Builder().build())
                }
            }
            DisposableEffect(banner) { onDispose { banner.destroy() } }
            LifecycleResumeEffect(Unit) {
                detecting = preferences.getBoolean("Mapp", false)
                detection.isChecked = detecting
                banner.resume()
                onPauseOrDispose { banner.pause() }
            }
            MaterialTheme {
                HomeScreen(detecting, {
                    activity.onOptionsItemSelected(detection)
                    detecting = detection.isChecked
                }, { activity.onOptionsItemSelected(menu.findItem(R.id.action_settings)) }, actions::open) {
                    AndroidView(factory = { banner }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
internal fun HomeScreen(
    detecting: Boolean,
    onDetection: () -> Unit,
    onSettings: () -> Unit,
    onDestination: (String) -> Unit,
    banner: @Composable () -> Unit = {},
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val fontScale = LocalDensity.current.fontScale
    val appTitle = stringResource(R.string.app_name)
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { View(it).apply { setBackgroundResource(R.drawable.ic_bgg) } },
            modifier = Modifier.fillMaxSize())
        Scaffold(containerColor = Color.Transparent, topBar = {
            Row(Modifier.fillMaxWidth().shadow(4.dp).background(colorResource(R.color.ColorPrimary))
                .windowInsetsPadding(WindowInsets.statusBars).heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically) {
                AndroidView(factory = { context ->
                    AppCompatTextView(ContextThemeWrapper(context, R.style.AppTheme_AppBarOverlay)).apply {
                        setTextAppearance(androidx.appcompat.R.style.TextAppearance_Widget_AppCompat_Toolbar_Title)
                        setTextColor(android.graphics.Color.WHITE)
                        gravity = android.view.Gravity.CENTER_VERTICAL
                        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    }
                }, update = { it.text = appTitle },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp).padding(start = 16.dp)
                        .semantics { text = AnnotatedString(appTitle) })
                IconButton(onClick = onDetection, modifier = Modifier.semantics {
                    toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
                }) {
                    Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off),
                        stringResource(R.string.pref_title_enable), tint = Color.White)
                }
                IconButton(onClick = onSettings) {
                    Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings), tint = Color.White)
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Box(Modifier.weight(1f)) {
                    AndroidView(factory = { View(it).apply { setBackgroundResource(R.drawable.curve_toolbar_bg) } },
                        modifier = Modifier.fillMaxWidth().height(130.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(start = 11.dp, end = 11.dp, top = 22.dp, bottom = 10.dp),
                    ) {
                        item(key = "tol", span = { GridItemSpan(maxLineSpan) }) {
                            HomeTile(R.string.title_activity_utills_and_tools, R.drawable.new_tol, 0, screenWidth.value * .409f, fontScale) { onDestination("tol") }
                        }
                        item(key = "mok") {
                            HomeTile(R.string.title_activity_moked_page, R.drawable.new_mok, 1, screenWidth.value * .4f, fontScale) { onDestination("mok") }
                        }
                        item(key = "tes") {
                            HomeTile(R.string.test_beep, R.drawable.new_tes, 2, screenWidth.value * .4f, fontScale) { onDestination("tes") }
                        }
                        item(key = "con") {
                            HomeTile(R.string.title_activity_contact, R.drawable.new_con, 3, screenWidth.value * .4f, fontScale) { onDestination("con") }
                        }
                        item(key = "inf") {
                            HomeTile(R.string.title_activity_info, R.drawable.new_inf, 4, screenWidth.value * .4f, fontScale) { onDestination("inf") }
                        }
                    }
                }
                // The published 12822 screen reserves the banner band even when an offline ad cannot load.
                Box(Modifier.fillMaxWidth().heightIn(min = 50.dp)
                    .background(colorResource(R.color.ColorPrimary)).testTag("homeBanner")) { banner() }
            }
        }
    }
}

@Composable
private fun HomeTile(label: Int, image: Int, position: Int, height: Float, fontScale: Float, onClick: () -> Unit) {
    val title = stringResource(label)
    val density = LocalDensity.current.density
    // Match the original RecyclerView's physical offsets, including its full-span first item.
    Column(Modifier.absolutePadding(left = if (position % 2 == 0) 7.dp else 3.5.dp,
        right = if (position % 2 == 0) 3.5.dp else 7.dp,
        top = if (position == 0) 7.dp else 0.dp, bottom = 7.dp).semantics {
            role = Role.Button
            text = AnnotatedString(title)
        }.clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().height(height.dp).shadow(5.dp, RoundedCornerShape(27.dp), clip = false)) {
            AndroidView(factory = { context ->
                CardView(context).apply {
                    radius = 27 * density
                    // Compose owns the shadow: native child elevation is not drawn reliably on API 24.
                    cardElevation = 0f
                    setCardBackgroundColor(android.graphics.Color.WHITE)
                    addView(View(context).apply { setBackgroundResource(image) },
                        android.widget.FrameLayout.LayoutParams(-1, -1))
                    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                }
            }, modifier = Modifier.fillMaxSize())
        }
        // Original artwork contains raster text. Supply scalable text when the user requests larger fonts.
        if (fontScale > 1f) Text(title, modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp).clearAndSetSemantics {},
            color = Color.Black, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew RTL large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Preview(name = "English landscape", locale = "en", widthDp = 640, heightDp = 360)
@Composable
private fun HomePreview() {
    MaterialTheme { HomeScreen(false, {}, {}, {}) }
}
