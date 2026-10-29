package atlow.chemi.mymada

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import atlow.chemi.mymada.databinding.ActivityInfoBinding

class InfoActivityKt : AppCompatActivity(), View.OnTouchListener {
    private var oldX1 = 0f
    private var oldY1 = 0f
    private var isMove1 = false
    private val _requestPermissionSetting = 101
    private lateinit var detect:MenuItem
    private lateinit var goToPriv:InterstitialAd
    private lateinit var privBtn: Button
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetTitle()
        val binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        MobileAds.initialize(this) {}
        goToPriv = InterstitialAd(this)
        goToPriv.adUnitId = "ca-app-pub-7600269766434003/4483563620"
        goToPriv.loadAd(AdRequest.Builder().build())
        goToPriv.adListener = object : AdListener() {
            override fun onAdClosed() {
                goToPriv.loadAd(AdRequest.Builder().build())
                val intent = Intent(applicationContext, PrivacyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        val myTypeface = Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")
        binding.textView10.typeface = myTypeface
        binding.textView10.movementMethod = ScrollingMovementMethod()
        binding.textView10.text = resources.getString(R.string.TversionNumber, BuildConfig.VERSION_NAME)
        privBtn = binding.privB
        privBtn.setOnTouchListener(this)
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
    @Suppress("UNUSED_PARAMETER")
    fun privacy(_view: View) {
        if (goToPriv.isLoaded)
            goToPriv.show()
        else {
            val intent = Intent(applicationContext, PrivacyActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val windowWidth = window.decorView.measuredWidth
        if (windowWidth > privBtn.height * 4.5) {
            Log.e("window width", "do change")
            val params = privBtn.layoutParams
            params.width = privBtn.height * 4
            privBtn.layoutParams = params
        }
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
                val s = sp.edit()
                val contentResolver = applicationContext.contentResolver
                val enabledNotificationListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
                val packageName = applicationContext.packageName
                if (!item.isChecked) {
                    item.isChecked = true
                    item.setIcon(R.drawable.ic_detect_on)
                    /*new*/
                    val serviceIntent = Intent(this, SMSMonitorService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                    /*end new*/
                    val test = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                    val activityExists = isCallable(test)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (activityExists) {
                            try {
                                if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                                    val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                                    builder.setMessage(resources.getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(resources.getString(R.string.dialog_yes)) { _,_ ->
                                                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                                                startActivity(intent)
                                            }
                                            .setNegativeButton(resources.getString(R.string.dialog_no)) { dialog, _ -> dialog.cancel() }
                                    val alert = builder.create()
                                    alert.setOnShowListener {
                                        val cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
                                        cancelButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.ColorPrimary))
                                    }
                                    alert.show()
                                }
                                s.putBoolean("Mapp", true)
                            }
                            catch (e: ActivityNotFoundException) {
                                val intent = Intent(this, NotificationService::class.java)
                                val old = Intent(this, NotificationServiceOld::class.java)
                                stopService(intent)
                                startService(old)
                                if (!NotificationServiceOld.isAccessibilitySettingsOn(applicationContext)) {
                                    val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                                    builder.setMessage(resources.getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                                                val intent1 = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                                startActivityForResult(intent1, _requestPermissionSetting)
                                            }
                                            .setNegativeButton(resources.getString(R.string.dialog_no)) { dialog, _ -> dialog.cancel() }
                                    val alert = builder.create()
                                    alert.setOnShowListener {
                                        val cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
                                        cancelButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.ColorPrimary))
                                    }
                                    alert.show()
                                }
                                s.putBoolean("Mapp", true)
                            }

                        }
                        else {
                            val intent = Intent(this, NotificationService::class.java)
                            val old = Intent(this, NotificationServiceOld::class.java)
                            stopService(intent)
                            startService(old)
                            if (!NotificationServiceOld.isAccessibilitySettingsOn(applicationContext)) {
                                val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                                builder.setMessage(resources.getString(R.string.dialog_help)).setCancelable(false)
                                        .setPositiveButton(resources.getString(R.string.dialog_yes)) { _, _ ->
                                            val inten = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                            startActivityForResult(inten, _requestPermissionSetting)
                                        }
                                        .setNegativeButton(resources.getString(R.string.dialog_no)) { dialog, _ -> dialog.cancel() }
                                val alert = builder.create()
                                alert.setOnShowListener {
                                    val cancelButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
                                    cancelButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.ColorPrimary))
                                }
                                alert.show()
                            }
                            s.putBoolean("Mapp", true)
                        }
                    } else {
                        s.putBoolean("Mapp", true)
                    }
                } else {
                    val serviceIntent = Intent(this, SMSMonitorService::class.java)
                    stopService(serviceIntent)
                    item.isChecked = false
                    s.putBoolean("Mapp", false)
                    item.setIcon(R.drawable.ic_detect_off)
                }
                s.apply()
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
    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                val x = oldX1 - event.x > 140 || event.x - oldX1 > 140
                val y = oldY1 - event.y > 50 || event.y - oldY1 > 50
                if (x || y) {
                    isMove1 = true
                    privBtn.alpha = 1f
                }
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                oldY1 = event.y
                oldX1 = event.x
                isMove1 = false
                privBtn.alpha = 0.4f
                return true
            }
            MotionEvent.ACTION_UP -> {
                privBtn.alpha = 1f
                if (!isMove1)
                    privBtn.performClick()
                return true
            }
        }
        return false
    }
}
