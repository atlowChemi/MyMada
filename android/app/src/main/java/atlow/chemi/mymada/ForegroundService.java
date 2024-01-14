package atlow.chemi.mymada;

import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_START;
import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_STOP;
import static atlow.chemi.mymada.Constants.NOTIFICATION_CONFIG;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ForegroundService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        var action = intent.getAction();
        if (action == null) return START_NOT_STICKY;
        if (action.equals(ACTION_FOREGROUND_SERVICE_START)) {
            if (intent.getExtras() != null && intent.getExtras().containsKey(NOTIFICATION_CONFIG)) {
                var notificationConfig = intent.getExtras().getBundle(NOTIFICATION_CONFIG);
                if (notificationConfig != null && notificationConfig.containsKey("id")) {
                    var notification = NotificationHelper.getInstance(getApplicationContext())
                            .buildNotification(getApplicationContext(), notificationConfig);

                    startForeground((int)notificationConfig.getDouble("id"), notification);
                }
            }
        } else if (action.equals(ACTION_FOREGROUND_SERVICE_STOP)) {
            stopSelf();
        }
        return START_NOT_STICKY;

    }
}