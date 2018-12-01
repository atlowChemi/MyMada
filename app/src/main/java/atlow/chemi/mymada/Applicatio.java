package atlow.chemi.mymada;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.app.Application;
import android.graphics.Color;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Locale;

public class Applicatio extends MultiDexApplication {
    public double d = 12;
    public static final String Grp1 = "grpCalls", Grp2 = "grpOthers", CnlBg = "runInBg", Cnl101 = "101Calls";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        super.onCreate();
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
        String lng = sharedPreferences.getString("lng","iw");
        Resources resources = getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = new Locale(lng);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private void createNotificationChannel()    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelGroup group1 = new NotificationChannelGroup(Grp1, "התראות אודות מקרים");
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");

            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);

            NotificationChannel serviceChannel2 = new NotificationChannel(Cnl101, "זיהוי אירועי חירום", NotificationManager.IMPORTANCE_HIGH);
            serviceChannel2.enableVibration(true);
            serviceChannel2.setBypassDnd(true);
            serviceChannel2.setDescription("התראות עבור אירועי חירום שיתקבלו");
            //serviceChannel2.setLightColor();
            serviceChannel2.enableLights(true);
            serviceChannel2.setShowBadge(true);
            serviceChannel2.setLightColor(Color.RED);
            serviceChannel2.setGroup(Grp1);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannelGroup(group1);
            manager.createNotificationChannelGroup(group2);
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(serviceChannel2);
        }
    }
}