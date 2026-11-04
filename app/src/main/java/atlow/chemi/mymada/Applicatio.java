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
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",0);
        boolean mapp = sharedPreferences.getBoolean("Mapp", false);
        if (mapp) {
            Intent serviceIntent = new Intent(getApplicationContext(), SMSMonitorService.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            try {
                tryConnectService(getApplicationContext());
            } catch (NoClassDefFoundError e) {
//                Crashlytics.log("Connecting service failed");
//                Crashlytics.logException(e);
            }
        }
        makeRec();
    }
    public static void tryConnectService(Context mContext) {
        toggleNotificationListenerService(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName = new ComponentName(mContext, NotificationService.class);
            requestRebind(componentName);
        }
    }
    public static void toggleNotificationListenerService(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PackageManager pm = mContext.getPackageManager();
            ComponentName componentName = new ComponentName(mContext, NotificationService.class);
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
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
