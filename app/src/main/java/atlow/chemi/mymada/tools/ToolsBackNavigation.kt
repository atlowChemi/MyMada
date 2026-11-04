package atlow.chemi.mymada.tools

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

/** Kotlin bridge while the installed Tools Activity is still Java. */
object ToolsBackNavigation {
    @JvmStatic fun install(activity: AppCompatActivity, drawer: DrawerLayout) {
        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START) &&
                    drawer.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    try { activity.onBackPressedDispatcher.onBackPressed() }
                    finally { isEnabled = true }
                }
            }
        })
    }
}
