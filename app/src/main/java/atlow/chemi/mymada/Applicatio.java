package atlow.chemi.mymada;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.Application;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;


import atlow.chemi.mymada.helpers.FontsOverride;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.recieversAndServices.BootCompleteReceiver;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

import static android.service.notification.NotificationListenerService.requestRebind;

public class Applicatio extends Application {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    protected void attachBaseContext(Context base) {
        atlow.chemi.mymada.settings.PreferenceMigration.migrate(base);
        super.attachBaseContext(LocalHelper.onAttachApplication(base));
    }
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(atlow.chemi.mymada.settings.LocaleRefresh.INSTANCE);
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/opensanshebrew.ttf");
        makeRec();
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",0);
        boolean mapp = sharedPreferences.getBoolean("Mapp", false);
        if (mapp) {
            SMSMonitorService.start(this);
            try {
                tryConnectService(getApplicationContext());
            } catch (NoClassDefFoundError e) {
//                Crashlytics.log("Connecting service failed");
//                Crashlytics.logException(e);
            }
        }
    }
    public static void tryConnectService(Context mContext) {
        atlow.chemi.mymada.calls.ListenerAccess.rebind(mContext);
    }
    private void makeRec() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver receiver;
        receiver = new GrobootRec();
        IntentFilter filter = new IntentFilter("MADA_APP");
        filter.addAction("atlow.chemi.MyMada.MADA_APP");
        broadcastManager.registerReceiver(receiver, filter);
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter2.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter2.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter2.addAction(Intent.ACTION_PACKAGE_REPLACED);
        BroadcastReceiver bcr = new BootCompleteReceiver();
        broadcastManager.registerReceiver(bcr, filter2);
    }
}
