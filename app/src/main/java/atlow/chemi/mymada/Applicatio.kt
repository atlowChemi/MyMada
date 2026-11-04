package atlow.chemi.mymada

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatDelegate
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import atlow.chemi.mymada.calls.ListenerAccess
import atlow.chemi.mymada.helpers.FontsOverride
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.recieversAndServices.GrobootRec
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService
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
        for (family in listOf("DEFAULT", "MONOSPACE", "SERIF", "SANS_SERIF"))
            FontsOverride.setDefaultFont(this, family, "fonts/opensanshebrew.ttf")
        LocalBroadcastManager.getInstance(this).registerReceiver(GrobootRec(), IntentFilter("MADA_APP").apply {
            addAction("atlow.chemi.MyMada.MADA_APP")
        })
        if (getSharedPreferences("Settings", 0).getBoolean("Mapp", false)) {
            SMSMonitorService.start(this)
            ListenerAccess.rebind(this)
        }
    }
    companion object { init { AppCompatDelegate.setCompatVectorFromResourcesEnabled(true) } }
}
