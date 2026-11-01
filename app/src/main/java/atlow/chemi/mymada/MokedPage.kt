package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import atlow.chemi.mymada.fragments.MainActivity1
import atlow.chemi.mymada.fragments.MainActivity2
import atlow.chemi.mymada.fragments.MainActivity3
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.settings.DetectionControls

class MokedPage : AppCompatActivity() {
    private var selected by mutableIntStateOf(1)
    private var detecting by mutableStateOf(false)
    private var prompt by mutableStateOf(false)
    private lateinit var detection: MenuItem
    private fun page(index: Int) = if (SettingsPage.isRTL()) 2 - index else index
    fun selectPage(index: Int, smooth: Boolean = false) {
        require(index in 0..2)
        findViewById<ViewPager>(R.id.viewpager).setCurrentItem(page(index), smooth)
    }
    val currentPage: Int get() = selected
    private val preferences by lazy { getSharedPreferences("Settings", 0) }
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "Mapp") detecting = preferences.getBoolean("Mapp", false)
    }
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!preferences.getBoolean("realM", false)) {
            val official = packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency")
            if (official == null || packageManager.queryIntentActivities(official, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
                startActivity(Intent(this, lockPage::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
                return
            }
        }
        val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
        if (label != 0) setTitle(label)
        setContentView(R.layout.activity_moked_page)
        val menu = PopupMenu(this, View(this)).menu
        menuInflater.inflate(R.menu.popup_menu, menu)
        detection = menu.findItem(R.id.one).apply { isCheckable = true }
        detecting = preferences.getBoolean("Mapp", false)
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        prompt = savedInstanceState?.getBoolean("dispatch-region-prompt") ?: !preferences.getBoolean("pickedMoked", false)
        val pager = findViewById<ViewPager>(R.id.viewpager)
        // Native pager is the fragment-state/swipe bridge. Host bars and prompt are Compose.
        // The outer CoordinatorLayout owns system bars; prevent pre-R redispatch loops.
        if (Build.VERSION.SDK_INT < 30) pager.setOnApplyWindowInsetsListener { _, insets -> insets.consumeSystemWindowInsets().consumeStableInsets() }
        val pages = listOf(MainActivity1(), MainActivity2(), MainActivity3())
        pager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getCount() = pages.size
            override fun getItem(position: Int): Fragment = pages[page(position)]
        }
        // Reverse physical pages for RTL instead of mirrored Views, which yield zero
        // accessibility bounds for Compose descendants on API24.
        if (savedInstanceState == null) selectPage(if (intent.getBooleanExtra("msg", false)) 0 else 1)
        selected = page(pager.currentItem)
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) { selected = page(position) }
        })
        findViewById<ComposeView>(R.id.dispatch_header).setContent {
            atlow.chemi.mymada.integrations.IntegrationTheme {
                val background = colorResource(R.color.ColorPrimary)
                val foreground = colorResource(R.color.White)
                Column(Modifier.background(background)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(onClick = { finish() }) { Icon(painterResource(R.drawable.ic_back), contentDescription = getString(androidx.appcompat.R.string.abc_action_bar_up_description), tint = foreground) }
                        Text(title.toString(), color = foreground, fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, modifier = Modifier.weight(1f).padding(start = 24.dp))
                        IconButton(onClick = { DetectionControls.toggle(this@MokedPage, detection) }, modifier = Modifier.testTag("dispatch-detection")) {
                            Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off), contentDescription = detection.title.toString(), tint = foreground)
                        }
                        IconButton(onClick = { startActivity(Intent(this@MokedPage, SettingsPage::class.java)) }, modifier = Modifier.testTag("dispatch-settings")) {
                            Icon(painterResource(R.drawable.ic_settings), contentDescription = menu.findItem(R.id.action_settings).title.toString(), tint = foreground)
                        }
                    }
                    @Suppress("DEPRECATION")
                    TabRow(selectedTabIndex = selected, containerColor = background, contentColor = foreground,
                        divider = {}, indicator = { positions ->
                            Box(Modifier.tabIndicatorOffset(positions[selected]).fillMaxWidth().height(2.dp).background(foreground))
                        }) {
                        listOf(R.string.button_a, R.string.button_b, R.string.button_i).forEachIndexed { index, labelId ->
                            var tabWidth by remember { mutableIntStateOf(0) }
                            Tab(selected == index, onClick = { selectPage(index, true) }, modifier = Modifier.heightIn(min = 48.dp).onSizeChanged { tabWidth = it.width }.testTag("dispatch-tab-$index"),
                                selectedContentColor = foreground, unselectedContentColor = foreground.copy(alpha = .7f)) {
                                Box(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                    val label = getString(labelId).uppercase()
                                    val style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp)
                                    val width = androidx.compose.ui.text.rememberTextMeasurer().measure(label, style).size.width
                                    // TabLayout reduces a label from 14sp to 12sp when it would wrap.
                                    val nativePadding = with(androidx.compose.ui.platform.LocalDensity.current) { 32.dp.roundToPx() }
                                    Text(label, style = style, fontSize = if (tabWidth > 0 && width > tabWidth - nativePadding) 12.sp else 14.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        }
                    }
                }
                if (prompt) AlertDialog(onDismissRequest = { prompt = false }, title = { Text(getString(R.string.pickMokTtl)) }, text = { Text(getString(R.string.pickMokTxt)) },
                    dismissButton = { TextButton(onClick = { prompt = false }, modifier = Modifier.testTag("dispatch-region-skip")) { Text(getString(R.string.skip)) } },
                    confirmButton = { TextButton(onClick = {
                        prompt = false
                        startActivity(Intent(this@MokedPage, SettingsPage::class.java).putExtra("pickMoked", true))
                    }) { Text(getString(R.string.goToSet)) } })
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("dispatch-region-prompt", prompt)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onDestroy()
    }
}
