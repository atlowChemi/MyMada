package atlow.chemi.mymada.recieversAndServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.PendingIntentCompat;
import androidx.core.app.NotificationManagerCompat;

import atlow.chemi.mymada.HomePage;
import atlow.chemi.mymada.R;


public class SMSMonitorService extends Service {
    protected String string, Grp2 = "grpOthers", CnlBg = "runInBg";
    public NotificationManager manager;
    public static Boolean testB = false;
    public Context context;
    protected boolean app;


    @Override
    public void onCreate() {
        super.onCreate();
        Intent callSNI = new Intent(this, HomePage.class);
        callSNI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntentCompat.getActivity(this, 1, callSNI, 0, false);
        Intent btn = new Intent(this, ActionReceiver.class);
        btn.putExtra("action", "runInBG");
        PendingIntent pBtn = PendingIntentCompat.getBroadcast(SMSMonitorService.this, 13, btn, PendingIntent.FLAG_UPDATE_CURRENT, false);
//        Crashlytics.log("sending SmsMonitor notif");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            setNotifChannels();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CnlBg)
                .setContentTitle(getResources().getString(R.string.BGServiceTtl))
                .setTicker(getResources().getString(R.string.BGServiceTicker))
                .setContentText(getResources().getString(R.string.BGServiceTxt))
                .setContentIntent(pendingIntent)
                .setGroup("BG")
                .setPriority(NotificationManagerCompat.IMPORTANCE_MIN)
                .addAction(R.drawable.ic_close, getString(R.string.MetroStop), pBtn)
                .setShowWhen(false)
                .setColor(Color.argb(50, 76, 119, 247))
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true);
        try {
            builder.setSmallIcon(R.drawable.ic_noti);
//            Crashlytics.log("Loaded small icon");
        }catch (Exception e) {
//            Crashlytics.log("Error SmsMonitor Notification!");
//            Crashlytics.logException(e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_MIN);
        }
        Notification notification = builder.build();
        startForeground(78, notification);
//        Crashlytics.log("started foreground");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setNotifChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");
            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);
            manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannelGroup(group2);
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
