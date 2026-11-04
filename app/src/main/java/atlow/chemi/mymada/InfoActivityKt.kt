package atlow.chemi.mymada

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.widget.PopupMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import atlow.chemi.mymada.about.AboutScreen
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.MobileAds
import java.lang.ref.WeakReference

class InfoActivityKt : AppCompatActivity() {
    private val _requestPermissionSetting = 101
    private lateinit var detect:MenuItem
    private var goToPriv: InterstitialAd? = null
    private var detecting by mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetTitle()
        MobileAds.initialize(applicationContext) {}
        loadPrivacyAd()
        // Keep detection/service behavior in its existing handler until #24.
        val menu = PopupMenu(this, View(this)).menu
        onCreateOptionsMenu(menu)
        setContent {
            LifecycleResumeEffect(Unit) {
                val preferences = getSharedPreferences("Settings", 0)
                fun refresh() {
                    detecting = preferences.getBoolean("Mapp", false)
                    atlow.chemi.mymada.settings.DetectionControls.render(detect, detecting)
                }
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == "Mapp") refresh()
                }
                preferences.registerOnSharedPreferenceChangeListener(listener)
                refresh()
                onPauseOrDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
            }
            MaterialTheme {
                AboutScreen(detecting, ::finish, {
                    onOptionsItemSelected(detect)
                    detecting = detect.isChecked
                }, { onOptionsItemSelected(menu.findItem(R.id.action_settings)) }, ::privacy)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocalHelper.onAttach(base))
    }
    private fun resetTitle() {
        try {
            val label = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
            if (label != 0)
                setTitle(label)
        }
        catch (e: PackageManager.NameNotFoundException) {
//            Crashlytics.logException(e)
        }

    }
    private fun privacy() {
        val ad = goToPriv
        goToPriv = null
        if (ad == null) {
            openPrivacy()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadPrivacyAd()
                openPrivacy()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onAdDismissedFullScreenContent()
            }
        }
        ad.show(this)
    }
    private fun loadPrivacyAd() {
        // An offline ad request can outlive recreation; do not retain the old Activity/view tree.
        val activity = WeakReference(this)
        InterstitialAd.load(applicationContext, "ca-app-pub-7600269766434003/4483563620",
            AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    activity.get()?.let { if (!it.isDestroyed) it.goToPriv = ad }
                }
            })
    }
    private fun openPrivacy() {
        val intent = Intent(applicationContext, PrivacyActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("Settings", 0)
        val icon = if (sp.getBoolean("Mapp", false)) R.drawable.ic_detect_on else R.drawable.ic_detect_off
        if(::detect.isInitialized)
            detect.setIcon(icon)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val sp = getSharedPreferences("Settings", 0)
        menuInflater.inflate(R.menu.popup_menu, menu)
        val icon = if (sp.getBoolean("Mapp", false)) R.drawable.ic_detect_on else R.drawable.ic_detect_off
        detect = menu.findItem(R.id.one)
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setIcon(icon)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val sp = getSharedPreferences("Settings", 0)
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.one -> {
                atlow.chemi.mymada.settings.DetectionControls.toggle(this, item)
                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsPage::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun isCallable(intent: Intent): Boolean {
        return try {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0
        }
        catch (e: Exception) {
//            Crashlytics.logException(e)
            false
        }
    }
}
