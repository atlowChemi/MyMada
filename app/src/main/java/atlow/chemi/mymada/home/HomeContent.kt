package atlow.chemi.mymada.home

import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import atlow.chemi.mymada.HomePage
import atlow.chemi.mymada.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/** Compose owns presentation; HomePage/HomeActions temporarily retain native platform behavior. */
object HomeContent {
    @JvmStatic fun install(activity: HomePage) {
        activity.enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        val actions = HomeActions(activity)
        val preferences = activity.getSharedPreferences("Settings", 0)
        val menu = PopupMenu(activity, View(activity)).menu
        activity.onCreateOptionsMenu(menu)
        val detection = menu.findItem(R.id.one)
        activity.setContent {
            var detecting by remember { mutableStateOf(detection.isChecked) }
            val banner = remember {
                AdView(activity).apply {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    detecting: Boolean,
    onDetection: () -> Unit,
    onSettings: () -> Unit,
    onDestination: (String) -> Unit,
    banner: @Composable () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.title_activity_home_page)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.ColorPrimary),
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
            actions = {
                IconButton(onClick = onDetection, modifier = Modifier.semantics {
                    toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
                }) {
                    Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off),
                        stringResource(R.string.pref_title_enable))
                }
                IconButton(onClick = onSettings) {
                    Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings))
                }
            },
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyVerticalGrid(
                // Scaled minimum width keeps long translated labels readable at large font sizes.
                columns = GridCells.Adaptive((144 * LocalDensity.current.fontScale).dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "tol", span = { GridItemSpan(maxLineSpan) }) {
                    HomeTile(R.string.title_activity_utills_and_tools, R.drawable.ic_advanced) { onDestination("tol") }
                }
                item(key = "mok") {
                    HomeTile(R.string.title_activity_moked_page, R.drawable.ic_send) { onDestination("mok") }
                }
                item(key = "tes") {
                    HomeTile(R.string.test_beep, R.drawable.ic_notifications) { onDestination("tes") }
                }
                item(key = "con") {
                    HomeTile(R.string.title_activity_contact, R.drawable.ic_pref_send) { onDestination("con") }
                }
                item(key = "inf") {
                    HomeTile(R.string.title_activity_info, R.drawable.ic_info) { onDestination("inf") }
                }
            }
            banner()
        }
    }
}

@Composable
private fun HomeTile(label: Int, icon: Int, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().semantics { role = Role.Button }) {
        Column(
            Modifier.fillMaxWidth().heightIn(min = 112.dp).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
            Text(stringResource(label), textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew RTL large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Preview(name = "English landscape", locale = "en", widthDp = 640, heightDp = 360)
@Composable
private fun HomePreview() {
    MaterialTheme { HomeScreen(false, {}, {}, {}) }
}
