package atlow.chemi.mymada

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import atlow.chemi.mymada.calls.ListenerAccess
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.recieversAndServices.GrobootRec
import atlow.chemi.mymada.calls.DetectionStatus
import atlow.chemi.mymada.settings.LocaleRefresh
import atlow.chemi.mymada.settings.PreferenceMigration

/** Keep the installed Application identity; register call execution before rebinding listeners. */
class Applicatio : Application() {
    override fun attachBaseContext(base: Context) {
        PreferenceMigration.migrate(base)
        super.attachBaseContext(LocalHelper.onAttachApplication(base))
    }
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(LocaleRefresh)
        registerActivityLifecycleCallbacks(DetectionStatus)
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("enabled_notification_listeners"), false,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) { ListenerAccess.stopIfRevoked(this@Applicatio) }
            })
        LocalBroadcastManager.getInstance(this).registerReceiver(GrobootRec(), IntentFilter("MADA_APP").apply {
            addAction("atlow.chemi.MyMada.MADA_APP")
        })
        if (getSharedPreferences("Settings", 0).getBoolean("Mapp", false)) {
            DetectionStatus.refresh(this)
            ListenerAccess.rebind(this)
        }
    }
    companion object { init { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true) } }
}
