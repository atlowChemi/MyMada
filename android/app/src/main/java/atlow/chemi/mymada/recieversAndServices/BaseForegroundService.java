package atlow.chemi.mymada.recieversAndServices;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_START;
import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_STOP;
import static atlow.chemi.mymada.Constants.NOTIFICATION_CONFIG;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ServiceCompat;

import java.util.Arrays;


import atlow.chemi.mymada.helpers.NotificationHelper;
public abstract class BaseForegroundService extends Service {
    private final String[] actions = new String[] {ACTION_FOREGROUND_SERVICE_START, ACTION_FOREGROUND_SERVICE_STOP};
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        var action = intent.getAction();
        if (action == null || Arrays.stream(actions).noneMatch(action::equals)) return START_STICKY;
        if (action.equals(ACTION_FOREGROUND_SERVICE_STOP)) {
            stopSelf();
            return START_STICKY;
        }
        var extras = intent.getExtras();
        if (extras == null || !extras.containsKey(NOTIFICATION_CONFIG)) {
            return START_STICKY;
        }
        var notificationConfig = extras.getBundle(NOTIFICATION_CONFIG);
        if (notificationConfig == null || !notificationConfig.containsKey("id")) {
            return START_STICKY;
        }

        var notification = NotificationHelper.from(getApplicationContext()).buildNotification(notificationConfig);
        ServiceCompat.startForeground(this, (int)notificationConfig.getDouble("id"), notification, FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        return START_STICKY;

    }
}

