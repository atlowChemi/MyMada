package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.fragments.*
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.settings.DetectionControls
import atlow.chemi.mymada.settings.SettingsChoiceDialog
import java.util.Locale

/** Installed component identity; FragmentManager owns the restored settings destination. */
class SettingsPage : AppCompatActivity() {
    private lateinit var shell: atlow.chemi.mymada.tools.ToolsWindow
    private var selected = R.id.notification
    private val preferences get() = getSharedPreferences("Settings", 0)

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shell = atlow.chemi.mymada.tools.ToolsWindow(this, R.menu.settings_menu)
        val restored = when (supportFragmentManager.findFragmentById(R.id.frame)) {
            is SettingsNotificationsSounds -> R.id.soundAndVib
            is SettingsAdvanced -> R.id.advanced
            is SettingsAmpm -> R.id.ampm
            is SettingsTools -> R.id.tools
            else -> R.id.notification
        }
        selected = savedInstanceState?.getInt("destination", restored) ?: R.id.notification
        shell.selected = selected
        shell.install(false, ::onNavigationItemSelected) {
            if (savedInstanceState == null) displayFragment(SettingsNotifications(), selected, false)
        }
        if (savedInstanceState == null) {
            overlayPrompt()
            if (intent.getBooleanExtra("pickMoked", false)) SettingsChoiceDialog.show(this, true)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        preferences.edit().putBoolean("oldU", true).apply()
    }

    override fun onResume() {
        super.onResume()
        shell.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("destination", selected)
        super.onSaveInstanceState(outState)
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
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
        return true
    }

    fun displayFragment(fragment: Fragment, toCheck: Int) = displayFragment(fragment, toCheck, true)

    private fun displayFragment(fragment: Fragment, toCheck: Int, animate: Boolean) {
        selected = toCheck
        supportFragmentManager.beginTransaction().apply {
            if (animate) setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top)
            replace(R.id.frame, fragment, "settings:$toCheck")
        }.commit()
        shell.selected = toCheck
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        if (::shell.isInitialized) shell.title = title?.toString().orEmpty()
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

    fun isDeviceTablet() = atlow.chemi.mymada.tools.ToolsWindow.isDeviceTablet(this)

    companion object {
        @JvmStatic fun isRTL(): Boolean = isRTL(Locale.getDefault())
        @JvmStatic fun isRTL(locale: Locale): Boolean = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
    }
}
