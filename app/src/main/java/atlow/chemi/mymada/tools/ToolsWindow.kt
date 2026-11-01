package atlow.chemi.mymada.tools

import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.iterator
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.LocalHelper
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.hypot

/** Compose owns navigation; FragmentManager still owns each feature and its saved state. */
class ToolsWindow(private val activity: AppCompatActivity, menuResource: Int) {
    val menu: Menu = PopupMenu(activity, View(activity)).menu.apply { activity.menuInflater.inflate(menuResource, this) }
    var selected by mutableIntStateOf(0)
    var title by mutableStateOf(activity.title.toString())
    private val preferences = activity.getSharedPreferences("Settings", 0)
    private var featureMenu by mutableStateOf<Menu?>(null)
    private var feature: Fragment? = null
    private var searchExpanded by mutableStateOf(false)

    fun onResume() {
        val changed = preferences.getBoolean("WasLangChanged", false)
        val language = Locale.forLanguageTag(LocalHelper.getLanguage(activity)).language
        if (changed || activity.resources.configuration.locales[0].language != language) {
            preferences.edit().putBoolean("WasLangChanged", false).apply()
            activity.recreate()
        }
    }

    fun install(tools: Boolean, onSelect: (MenuItem) -> Boolean, onReady: () -> Unit) {
        val attributes = activity.obtainStyledAttributes(intArrayOf(androidx.appcompat.R.attr.actionBarSize))
        val toolbarHeight = (attributes.getDimensionPixelSize(0, 0) / activity.resources.displayMetrics.density).dp
        attributes.recycle()
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentResumed(manager: FragmentManager, fragment: Fragment) {
                if (fragment.id != R.id.frame) return
                feature = fragment
                val themed = ContextThemeWrapper(activity, R.style.AppTheme_AppBarOverlay)
                val popup = PopupMenu(themed, View(themed))
                featureMenu = popup.menu.apply {
                    fragment.onCreateOptionsMenu(this, popup.menuInflater)
                    fragment.onPrepareOptionsMenu(this)
                }
                searchExpanded = (featureMenu?.findItem(R.id.app_bar_search)?.actionView as? SearchView)?.query?.isNotEmpty() == true
                if (searchExpanded) (featureMenu?.findItem(R.id.app_bar_search)?.actionView as SearchView).apply {
                    isIconified = false
                    clearFocus()
                }
            }
            override fun onFragmentViewDestroyed(manager: FragmentManager, fragment: Fragment) {
                if (feature === fragment) { featureMenu = null; feature = null; searchExpanded = false }
            }
        }, false)
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        activity.setContent {
            var detecting by remember { mutableStateOf(preferences.getBoolean("Mapp", false)) }
            DisposableEffect(preferences) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    if (key == "Mapp") detecting = prefs.getBoolean("Mapp", false)
                }
                preferences.registerOnSharedPreferenceChangeListener(listener)
                onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            MaterialTheme {
                val drawer = rememberDrawerState(DrawerValue.Closed)
                val openingOrOpen = drawer.targetValue == DrawerValue.Open
                val scope = rememberCoroutineScope()
                val locked = isDeviceTablet(activity)
                val titleSize = with(LocalDensity.current) {
                    dimensionResource(androidx.appcompat.R.dimen.abc_text_size_title_material_toolbar).toSp()
                }
                val titleFont = remember { FontFamily(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)) }
                BackHandler(!locked && drawer.isOpen) { scope.launch { drawer.close() } }
                BackHandler(searchExpanded && !drawer.isOpen) { collapseSearch() }
                Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))) {
                    Row(Modifier.fillMaxWidth().zIndex(1f).shadow(4.dp).background(colorResource(R.color.ColorPrimary))
                        .statusBarsPadding().height(toolbarHeight), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(modifier = Modifier.width(56.dp).height(toolbarHeight).testTag("shell-navigation"), onClick = {
                            if (searchExpanded) collapseSearch()
                            else if (locked) activity.finish()
                            else scope.launch { if (openingOrOpen) drawer.close() else drawer.open() }
                        }) {
                            if (locked || searchExpanded || openingOrOpen) Icon(painterResource(R.drawable.ic_back),
                                stringResource(if (openingOrOpen && !locked && !searchExpanded) R.string.navigation_drawer_close else R.string.back), tint = Color.White)
                            else Canvas(Modifier.size(24.dp).semantics {
                                contentDescription = activity.getString(R.string.navigation_drawer_open)
                            }) {
                                for (y in listOf(6f, 12f, 18f)) drawLine(Color.White,
                                    Offset(3.dp.toPx(), y.dp.toPx()), Offset(21.dp.toPx(), y.dp.toPx()), 2.dp.toPx())
                            }
                        }
                        val search = featureMenu?.findItem(R.id.app_bar_search)?.actionView as? SearchView
                        if (searchExpanded && search != null) key(search) {
                            AndroidView(factory = { search.apply { isIconified = false } },
                                modifier = Modifier.weight(1f).fillMaxHeight())
                        }
                        else {
                        Text(title, Modifier.weight(1f).padding(start = 16.dp), style = nativeTextMetrics, color = Color.White, fontSize = titleSize,
                            fontFamily = titleFont, letterSpacing = 0.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (tools) {
                            IconButton(onClick = { onSelect(menu.findItem(R.id.one)) }, modifier = Modifier.testTag("shell-detection").semantics {
                                toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
                            }) { Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off), stringResource(R.string.pref_title_enable), tint = Color.White) }
                            IconButton(onClick = { onSelect(menu.findItem(R.id.action_settings)) }) {
                                Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.action_settings), tint = Color.White)
                            }
                        }
                        featureMenu?.let { actions ->
                            for (item in actions) if (item.isVisible) IconButton(onClick = {
                                if (item.itemId == R.id.app_bar_search) searchExpanded = true
                                else feature?.onOptionsItemSelected(item)
                            }, modifier = Modifier.testTag("shell-action-${item.itemId}")) {
                                item.icon?.let { icon -> Icon(remember(icon) { icon.toBitmap().asImageBitmap() },
                                    item.title.toString(), Modifier.size(24.dp), tint = Color.White) }
                            }
                        }
                        }
                    }
                    val navigation: @Composable () -> Unit = {
                        Column(Modifier.width(dimensionResource(R.dimen.navigation_drawer_width)).fillMaxHeight().background(Color.White)
                            .verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                            DrawerItems(menu, detecting) { item ->
                                if (onSelect(item) && item.itemId !in listOf(R.id.detect, R.id.language, R.id.mokedPick)) scope.launch { drawer.close() }
                            }
                        }
                    }
                    val content: @Composable () -> Unit = {
                        AndroidView(factory = {
                            // FragmentActivity's inflater attaches restored fragments to the new container.
                            activity.layoutInflater.inflate(R.layout.tools_fragment_host, null).also { onReady() }
                        }, modifier = Modifier.fillMaxSize().padding(top = if (tools) 0.dp else 5.dp))
                    }
                    if (locked) Row(Modifier.weight(1f)) { navigation(); Box(Modifier.weight(1f)) { content() } }
                    else ModalNavigationDrawer(drawerContent = navigation, drawerState = drawer,
                        scrimColor = Color(0x99000000), modifier = Modifier.weight(1f), content = content)
                }
            }
        }
    }

    private fun collapseSearch() {
        (featureMenu?.findItem(R.id.app_bar_search)?.actionView as? SearchView)?.apply {
            setQuery("", false)
            clearFocus()
            isIconified = true
        }
        searchExpanded = false
    }

    @Composable private fun DrawerItems(items: Menu, detecting: Boolean, select: (MenuItem) -> Unit) {
        for (item in items) {
            if (!item.isVisible || item.itemId == R.id.one || item.itemId == R.id.action_settings) continue
            if (item.hasSubMenu()) {
                HorizontalDivider(Modifier.padding(top = 8.dp), color = Color(0x1f000000))
                Box(Modifier.heightIn(min = 48.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                    Text(item.title.toString(), style = nativeTextMetrics, color = colorResource(R.color.ColorRedDark), fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.opensans_hebrew)), letterSpacing = drawerTracking)
                }
                DrawerItems(requireNotNull(item.subMenu), detecting, select)
            } else {
                val checked = item.itemId == selected
                val color = colorResource(if (checked) R.color.navPickedTextColor else R.color.navNotPickedTextColor)
                Row(Modifier.fillMaxWidth()
                    .testTag("shell-destination-${item.itemId}").semantics {
                        this.selected = checked
                        if (item.itemId == R.id.detect) toggleableState = if (detecting) ToggleableState.On else ToggleableState.Off
                    }
                    .clickable { select(item) }.padding(horizontal = 5.dp, vertical = 2.dp)
                    .background(if (checked) colorResource(R.color.navPickedColor) else Color.Transparent, RoundedCornerShape(14.dp))
                    .heightIn(min = 44.dp).padding(horizontal = 11.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (item.itemId == R.id.detect) {
                        Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off), null, Modifier.size(24.dp), tint = color)
                    } else item.icon?.let { icon ->
                        val bitmap = remember(icon) {
                            icon.layoutDirection = activity.resources.configuration.layoutDirection
                            icon.toBitmap().asImageBitmap()
                        }
                        Icon(bitmap, null, Modifier.size(24.dp), tint = color)
                    }
                    Text(item.title.toString(), Modifier.padding(start = 32.dp), style = nativeTextMetrics, color = color, fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.opensans_hebrew)), letterSpacing = drawerTracking, maxLines = 1, overflow = TextOverflow.Clip)
                }
            }
        }
    }

    companion object {
        private val nativeTextMetrics = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = true))
        // Production MaterialComponents.Body2 tracking, proportional to scaled text size.
        private val drawerTracking = (0.25f / 14f).em
        fun isDeviceTablet(activity: AppCompatActivity): Boolean {
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            return activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
                hypot(metrics.widthPixels / metrics.xdpi, metrics.heightPixels / metrics.ydpi) >= 6.5
        }
    }
}
