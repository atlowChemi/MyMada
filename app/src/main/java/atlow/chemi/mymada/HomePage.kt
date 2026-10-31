package atlow.chemi.mymada

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import atlow.chemi.mymada.helpers.rateUs
import atlow.chemi.mymada.home.HomeContent
import atlow.chemi.mymada.home.HomePrompt
import atlow.chemi.mymada.settings.DetectionControls
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging

/** Preserve the installed launcher, notification channels, and onboarding destinations. */
class HomePage : AppCompatActivity() {
    private var detection: MenuItem? = null
    private val preferences get() = getSharedPreferences("Settings", 0)

    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && MyFirebaseMessagingService.routeLauncher(this, intent)) {
            finish()
            return
        }
        setTitle(R.string.app_name)
        createChannels()
        rateUs.app_launched(this)
        MyFirebaseMessagingService.getToken(applicationContext)
        FirebaseMessaging.getInstance().subscribeToTopic("news")
        FirebaseAnalytics.getInstance(applicationContext)
        HomeContent.install(this)
        if (savedInstanceState != null) return
        when {
            !preferences.getBoolean("oldU", false) -> HomePrompt.show(this, R.string.newSettings, 0,
                R.string.title_activity_settings, Intent(this, SettingsPage::class.java), false, 0)
            !Settings.canDrawOverlays(this) -> overlayPrompt()
            !preferences.getBoolean("privacy", false) -> HomePrompt.show(this, R.string.privacyMsg, 0,
                R.string.title_activity_privacy, Intent(this, PrivacyActivity::class.java), false, 0)
        }
        if (preferences.getBoolean("Mapp", false) && Build.VERSION.SDK_INT >= 26 &&
            getSystemService(NotificationManager::class.java)?.getNotificationChannel("runInBg")?.importance != NotificationManager.IMPORTANCE_MIN) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName).putExtra(Settings.EXTRA_CHANNEL_ID, "runInBg")
            HomePrompt.show(this, R.string.importanceMsg, 0, R.string.importance, intent, false, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        detection?.let { DetectionControls.render(it, preferences.getBoolean("Mapp", false)) }
        if (preferences.getBoolean("WasLangChanged", false)) {
            preferences.edit().putBoolean("WasLangChanged", false).apply()
            recreate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.popup_menu, menu)
        detection = menu.findItem(R.id.one).apply {
            isCheckable = true
            DetectionControls.render(this, preferences.getBoolean("Mapp", false))
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.one -> { DetectionControls.toggle(this, item, "home"); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsPage::class.java)); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun overlayPrompt() = HomePrompt.show(this, R.string.permDesOverlay, R.string.permTitle,
        R.string.permYes, Intent(this, SettingsPage::class.java), true, 345)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        MyFirebaseMessagingService.routeLauncher(this, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 345 && resultCode != RESULT_OK && !Settings.canDrawOverlays(this)) overlayPrompt()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannelGroups(listOf(
            NotificationChannelGroup("grpTog", "התראות מקרים"),
            NotificationChannelGroup("grpOthers", "התראות שונות")))
        manager.createNotificationChannels(listOf(
            NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN).apply {
                enableVibration(false); enableLights(false); setShowBadge(false)
                description = "חיוני כדי לאפשר זיהוי מקרים"; group = "grpOthers"
            },
            NotificationChannel("Push", "הודעות פוש", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true); setBypassDnd(true); enableLights(true); setShowBadge(true)
                description = "התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'"
                lightColor = Color.RED; group = "grpOthers"
            },
            NotificationChannel("notWithWin", "זיהוי הודעות", NotificationManager.IMPORTANCE_LOW).apply {
                enableVibration(false); setBypassDnd(false); enableLights(false); setShowBadge(true)
                description = "התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון"
                lightColor = Color.RED; group = "grpTog"
            }))
    }
}
