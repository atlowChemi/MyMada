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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
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
            override fun getItem(position: Int): Fragment = pages[position]
        }
        if (SettingsPage.isRTL()) pager.rotationY = 180f
        if (savedInstanceState == null) pager.setCurrentItem(if (intent.getBooleanExtra("msg", false)) 0 else 1, false)
        selected = pager.currentItem
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) { selected = position }
        })
        findViewById<ComposeView>(R.id.dispatch_header).setContent {
            atlow.chemi.mymada.integrations.IntegrationTheme {
                val background = colorResource(R.color.ColorPrimary)
                val foreground = colorResource(R.color.White)
                Column(Modifier.background(background)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(onClick = { finish() }) { Icon(painterResource(R.drawable.ic_back), contentDescription = getString(androidx.appcompat.R.string.abc_action_bar_up_description), tint = foreground) }
                        Text(title.toString(), color = foreground, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { DetectionControls.toggle(this@MokedPage, detection) }, modifier = Modifier.testTag("dispatch-detection")) {
                            Icon(painterResource(if (detecting) R.drawable.ic_detect_on else R.drawable.ic_detect_off), contentDescription = detection.title.toString(), tint = foreground)
                        }
                        IconButton(onClick = { startActivity(Intent(this@MokedPage, SettingsPage::class.java)) }, modifier = Modifier.testTag("dispatch-settings")) {
                            Icon(painterResource(R.drawable.ic_settings), contentDescription = menu.findItem(R.id.action_settings).title.toString(), tint = foreground)
                        }
                    }
                    @Suppress("DEPRECATION")
                    TabRow(selectedTabIndex = selected, containerColor = background, contentColor = foreground) {
                        listOf(R.string.button_a, R.string.button_b, R.string.button_i).forEachIndexed { index, labelId ->
                            Tab(selected == index, onClick = { pager.currentItem = index }, modifier = Modifier.testTag("dispatch-tab-$index"),
                                text = { Text(getString(labelId), fontSize = 14.sp) })
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
