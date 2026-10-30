package atlow.chemi.mymada.privacy

import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.settings.DetectionControls

abstract class PrivacyDetectionActivity : AppCompatActivity() {
    private var detection: MenuItem? = null
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onResume() {
        super.onResume()
        detection?.let { DetectionControls.render(it, getSharedPreferences("Settings", 0).getBoolean("Mapp", false)) }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.popup_menu, menu)
        detection = menu.findItem(R.id.one).apply {
            isCheckable = true
            DetectionControls.render(this, getSharedPreferences("Settings", 0).getBoolean("Mapp", false))
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.one -> { DetectionControls.toggle(this, item, "privacy"); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsPage::class.java)); true }
        else -> super.onOptionsItemSelected(item)
    }
}
