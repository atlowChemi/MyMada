package atlow.chemi.mymada;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.app.Application;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Locale;

import static android.service.notification.NotificationListenerService.requestRebind;

public class Applicatio extends MultiDexApplication {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/opensanshebrew.ttf");
        }catch (Exception e)    {
            e.printStackTrace();
        }
        try {
            FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/opensanshebrew.ttf");
        }catch (Exception e)    {
            e.printStackTrace();
        }
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",0);
        boolean mapp = sharedPreferences.getBoolean("Mapp", false);
        if (mapp) {
            Intent serviceIntent = new Intent(getApplicationContext(), SMSMonitorService.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            try {
                tryConnectService(getApplicationContext());
            } catch (NoClassDefFoundError e) {
                Crashlytics.log("Connecting service failed");
                Crashlytics.logException(e);
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
            pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }
    private void makeRec() {
        BroadcastReceiver receiver;
        receiver = new GrobootRec();
        IntentFilter filter = new IntentFilter("MADA_APP");
        filter.addAction("atlow.chemi.MyMada.MADA_APP");
        this.registerReceiver(receiver, filter);
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter2.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter2.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter2.addAction(Intent.ACTION_PACKAGE_REPLACED);
        BroadcastReceiver bcr = new BootCompleteReceiver();
        this.registerReceiver(bcr, filter2);
    }
}