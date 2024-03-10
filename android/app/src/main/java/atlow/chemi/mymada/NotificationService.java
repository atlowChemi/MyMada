package atlow.chemi.mymada;

import android.content.ComponentName;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.Objects;

import atlow.chemi.mymada.ReactModules.MadaNotificationsModule;
import atlow.chemi.mymada.ReactModules.SharedPreferencesModule;
import atlow.chemi.mymada.helpers.HeadlessJsTaskWorker;
import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;

public class NotificationService extends NotificationListenerService {
    private final String[] pkgNames = new String[] {"mdaemergency", "mda.health", "atlow.mymadaadmin"};

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        var notifyingPkg = statusBarNotification.getPackageName();
        var notification = statusBarNotification.getNotification();
        if (notifyingPkg == null || notification == null) {
            return;
        }
        var isCallRecognitionOn = SharedPreferencesModule.isCallRecognitionOn(getApplicationContext());
        if (!isCallRecognitionOn || Arrays.stream(pkgNames).noneMatch(notifyingPkg::contains)) {
            return;
        }

        var notificationText = NotificationCompat.getContentText(notification);
        var text = notificationText == null ? "" : notificationText.toString();
        var notificationTitle = NotificationCompat.getContentTitle(notification);
        var title = notificationTitle == null ? "" : notificationTitle.toString();
        var uuid = MadaNotificationsModule.setCallPendingIntent(notification.contentIntent);

        var data = new Data.Builder()
                .putString("uuid", uuid)
                .putString("text", text)
                .putString("title", title)
                .putString("type", "madaCallReceived")
                // .putString("ticker", notification.tickerText == null ? "" : notification.tickerText.toString())
                .build();
        var work = new OneTimeWorkRequest.Builder(HeadlessJsTaskWorker.class).setInputData(data).build();
        WorkManager.getInstance(getApplicationContext()).enqueue(work);
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification)  {
        if (statusBarNotification == null || statusBarNotification.getPackageName() == null || !statusBarNotification.getPackageName().contains("atlow.chemi.mymada")) {
            return;
        }
        var notification = statusBarNotification.getNotification();
        var channelId = NotificationCompat.getChannelId(notification);
        if (channelId == null || !channelId.contains("generated")) {
            return;
        }
        NotificationManagerCompat.from(getApplicationContext()).deleteNotificationChannel(channelId);
    }

    @Override
    public void onListenerConnected() {
        ReactNativeFirebaseCrashlyticsNativeHelper.log("getActiveNotification");
        try {
                getActiveNotifications();
            } catch (Exception e) {
                ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
            }
    }
    @Override
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }
        // Notification listener disconnected - requesting rebind
        requestRebind(new ComponentName(this, NotificationListenerService.class));
    }
}
