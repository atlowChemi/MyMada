package atlow.chemi.mymada;

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

public class ActionReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("action").equals("widget") && !MetroService.running) {
            //Toast.makeText(context, "metro running is " + MetroService.running, Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                if (manager != null && manager.getNotificationChannel("runInBg") == null) {
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
            Intent serviceIntent = new Intent(context, MetroService.class);
            try {
                ContextCompat.startForegroundService(context, serviceIntent);
            } catch (Exception e) {
                Crashlytics.log("failed starting metronome widget");
                Crashlytics.logException(e);
            }
        }
        else {
            Intent serviceIntent = new Intent(context, MetroService.class);
            context.stopService(serviceIntent);
        }
    }
}
