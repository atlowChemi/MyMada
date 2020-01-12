package atlow.chemi.mymada.recieversAndServices;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.crashlytics.android.Crashlytics;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.fragments.MetroActivity;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MetroService.class);
        String action = intent.getStringExtra("action");
        if(action != null) {
            if(action.equals("widget") && ! MetroService.running) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager manager = context.getSystemService(NotificationManager.class);
                    if(manager != null && manager.getNotificationChannel("runInBg") == null) {
                        NotificationChannelGroup group2 = new NotificationChannelGroup("grpOthers", "התראות שונות");
                        NotificationChannel serviceChannel = new NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
                        serviceChannel.enableVibration(false);
                        serviceChannel.enableLights(false);
                        serviceChannel.setShowBadge(false);
                        serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
                        serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
                        serviceChannel.setGroup("grpOthers");
                        manager.createNotificationChannelGroup(group2);
                        manager.createNotificationChannel(serviceChannel);
                    }
                }
                try {
                    ContextCompat.startForegroundService(context, serviceIntent);
                } catch(Exception e) {
                    Crashlytics.log("failed starting metronome widget");
                    Crashlytics.logException(e);
                }
            } else if(action.equals("runInBG")) {
                context.stopService(serviceIntent);
                context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).apply();
            } else {
                if(MetroActivity.bound)
                    Toast.makeText(context, R.string.MetroBound, Toast.LENGTH_SHORT).show();
                else
                    context.stopService(serviceIntent);
            }
        }
    }
}
