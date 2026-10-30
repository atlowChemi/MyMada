package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.fragments.*
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.helpers.MyDrawerLayout
import atlow.chemi.mymada.settings.DetectionControls
import atlow.chemi.mymada.settings.SettingsChoiceDialog
import com.google.android.material.navigation.NavigationView
import java.util.Locale
import kotlin.math.hypot

/** Installed component identity; FragmentManager owns the restored settings destination. */
class SettingsPage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawer: MyDrawerLayout
    private lateinit var navigation: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var selected = R.id.notification
    private var locked = false
    private val preferences get() = getSharedPreferences("Settings", 0)

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utillities_and_tools)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawer = findViewById(R.id.activity_Utils)
        navigation = findViewById(R.id.navigation)
        navigation.inflateMenu(R.menu.settings_menu)
        navigation.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        selected = savedInstanceState?.getInt("destination", R.id.notification) ?: R.id.notification
        if (savedInstanceState == null) displayFragment(SettingsNotifications(), selected, false)
        navigation.setCheckedItem(selected)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!locked && drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START)
                else finish()
            }
        })
        if (savedInstanceState == null) {
            overlayPrompt()
            if (intent.getBooleanExtra("pickMoked", false)) SettingsChoiceDialog.show(this, true)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
        preferences.edit().putBoolean("oldU", true).apply()
    }

    override fun onResume() {
        super.onResume()
        locked = isDeviceTablet()
        drawer.setDrawerLockMode(if (locked) DrawerLayout.LOCK_MODE_LOCKED_OPEN else DrawerLayout.LOCK_MODE_UNLOCKED)
        drawer.setScrimColor(if (locked) Color.TRANSPARENT else 0x99000000.toInt())
        toggle.syncState()
        DetectionControls.render(navigation.menu.findItem(R.id.detect), preferences.getBoolean("Mapp", false))
        if (preferences.getBoolean("WasLangChanged", false)) {
            preferences.edit().putBoolean("WasLangChanged", false).apply()
            recreate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("destination", selected)
        super.onSaveInstanceState(outState)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.homeDrawer -> { finish(); return true }
            R.id.detect -> { DetectionControls.toggle(this, item); return true }
            R.id.language -> { SettingsChoiceDialog.show(this, false); return true }
            R.id.mokedPick -> { SettingsChoiceDialog.show(this, true); return true }
            else -> {
                val fragment = when (item.itemId) {
                    R.id.notification -> SettingsNotifications()
                    R.id.soundAndVib -> SettingsNotificationsSounds()
                    R.id.advanced -> SettingsAdvanced()
                    R.id.ampm -> SettingsAmpm()
                    R.id.tools -> SettingsTools()
                    else -> return false
                }
                displayFragment(fragment, item.itemId)
            }
        }
        if (!locked) drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun displayFragment(fragment: Fragment, toCheck: Int) = displayFragment(fragment, toCheck, true)

    private fun displayFragment(fragment: Fragment, toCheck: Int, animate: Boolean) {
        selected = toCheck
        supportFragmentManager.beginTransaction().apply {
            if (animate) setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top)
            replace(R.id.frame, fragment, "settings:$toCheck")
        }.commit()
        navigation.setCheckedItem(toCheck)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && locked) { finish(); return true }
        return toggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    private fun overlayPrompt() {
        if (!Settings.canDrawOverlays(this)) DetectionControls.Prompt.show(this, R.string.permDesOverlay,
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
            title = R.string.permTitle, positive = R.string.permYes, request = 345)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 345) overlayPrompt()
    }

    fun isDeviceTablet(): Boolean {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            hypot(metrics.widthPixels / metrics.xdpi, metrics.heightPixels / metrics.ydpi) >= 6.5
    }

    companion object {
        @JvmStatic fun isRTL(): Boolean = isRTL(Locale.getDefault())
        @JvmStatic fun isRTL(locale: Locale): Boolean = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
    }
}
