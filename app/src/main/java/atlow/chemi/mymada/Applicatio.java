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

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Locale;

import static android.service.notification.NotificationListenerService.requestRebind;

public class Applicatio extends MultiDexApplication {
    public double d = 12;
    public static final String Grp1 = "grpCalls", Grp2 = "grpOthers", Grp3 = "grpTog", CnlBg = "runInBg", Cnl101 = "101Calls",
            CnlMinhala = "minhala", CnlConan = "conan", CnlGroup = "group", CnlMishmeret = "mishmeret", CnlWin = "notWithWin";
    public static NotificationManager manager;
    protected BroadcastReceiver receiver;
    private static Context mContext;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        createNotificationChannel();
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
        SharedPreferences.Editor spe = sharedPreferences.edit();
        spe.putString("fcm", FirebaseInstanceId.getInstance().getToken());
        spe.apply();
        boolean mapp = sharedPreferences.getBoolean("Mapp", false);
        if (mapp) {
            Intent serviceIntent = new Intent(getApplicationContext(), SMSMonitorService.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            tryConnectService();
        }

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

    private void createNotificationChannel()    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SharedPreferences notSP = getSharedPreferences("notifications", 0);
            AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setLegacyStreamType(AudioManager.STREAM_ALARM).build();
            NotificationChannelGroup group3 = new NotificationChannelGroup(Grp3, "התראות מקרים");
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");

            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);
            NotificationChannel serviceChannel2 = new NotificationChannel("Push", "הודעות פוש", NotificationManager.IMPORTANCE_HIGH);
            serviceChannel2.enableVibration(true);
            serviceChannel2.setBypassDnd(true);
            serviceChannel2.setDescription("התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'");
            serviceChannel2.enableLights(true);
            serviceChannel2.setShowBadge(true);
            serviceChannel2.setLightColor(Color.RED);
            serviceChannel2.setGroup(Grp2);

            NotificationChannel serviceChannel4 = new NotificationChannel(CnlWin, "זיהוי הודעות", NotificationManager.IMPORTANCE_LOW);
            serviceChannel4.enableVibration(false);
            serviceChannel4.setBypassDnd(false);
            serviceChannel4.setDescription("התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון");
            serviceChannel4.enableLights(false);
            serviceChannel4.setShowBadge(true);
            serviceChannel4.setLightColor(Color.RED);
            serviceChannel4.setGroup(Grp3);

            manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannelGroup(group3);
                manager.createNotificationChannelGroup(group2);
                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(serviceChannel2);
                manager.createNotificationChannel(serviceChannel4);
            }
        }
    }
    public static void tryConnectService() {
        toggleNotificationListenerService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName = new ComponentName(mContext, NotificationService.class);
            requestRebind(componentName);
        }
    }
    public static void toggleNotificationListenerService() {
        PackageManager pm = mContext.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(mContext, NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
}