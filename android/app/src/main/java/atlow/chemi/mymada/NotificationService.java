package atlow.chemi.mymada;

import static atlow.chemi.mymada.Constants.CALL_RECEIVER_ACTION;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Arrays;
import java.util.Objects;

import atlow.chemi.mymada.ReactModules.SharedPreferencesModule;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;


public class NotificationService extends NotificationListenerService {
    private final String[] pkgNames = new String[] {"mdaemergency", "mda.health", "atlow.mymadaadmin"};
    private enum CallType {
        OnCall(1),
        Group(2),
        Shift(3),
        Administrative(4),
        OldMessageSyntax(5),
        Service(6);

        public final int value;
        private CallType(int value) {
            this.value = value;
        }
    }

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

        Intent intent = new Intent(CALL_RECEIVER_ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(getNotificationInfo(notification));
        intent.putExtra("pack", notifyingPkg);

        var broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        GrobootRec.madasApp(notification.contentIntent);
        // TODO: don't broadcast, instead call headless JS task?
        broadcastManager.sendBroadcast(intent);
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification)  {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || statusBarNotification == null || statusBarNotification.getPackageName() == null || !statusBarNotification.getPackageName().contains("atlow.chemi.mymada")) {
            return;
        }
        var manager = getApplicationContext().getSystemService(NotificationManager.class);
        var notification = statusBarNotification.getNotification();
        var channelId = notification.getChannelId();
        if (channelId.contains("generated") && manager != null) {
            manager.deleteNotificationChannel(channelId);
        }
    }

    @Override
    public void onListenerConnected() {
            try {
                getActiveNotifications();
            } catch (Exception e) {
                Log.e("getActiveNotification", Objects.requireNonNull(e.getMessage()));
                // TODO: Crashlytics.logException(e);
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

    @NonNull
    private Bundle getNotificationInfo(Notification notification) {
        var data = new Bundle();
        var notificationText = NotificationCompat.getContentText(notification);
        var text = notificationText == null ? "" : notificationText.toString();
        var notificationTitle = NotificationCompat.getContentTitle(notification);
        var title = notificationTitle == null ? "" : notificationTitle.toString();

        data.putString("text", text);
        data.putString("title", title);
        data.putString("ticker", notification.tickerText == null ? "" : notification.tickerText.toString());

        if (notificationTitle == null || title.equals("מגן דוד אדום") || title.equals("מערך הכוננים הלאומי")) {
            return data;
        }
        data.putString("time", title.replaceAll("ֿֿ[*](אירוע חדש|New Event|הודעה מנהלתית חדשה)[*]", ""));
        data.putInt("callT", getCallType(text, title).value);

        return data;
    }

    private CallType getCallType(@NonNull String text, @NonNull String title) {
        if (text.contains("על מידע זה חל חיסיון רפואי")) {
            return CallType.OldMessageSyntax;
        } else if (text.contains("שירות") || text.contains("כלכלה") || text.contains("תפילה")) {
            return CallType.Service;
        } else if (title.contains("*הודעה מנהלתית חדשה*") || text.contains("מנהלתי") || text.contains("מינהלתי") || text.contains("משמרת בנה"))   {
            return CallType.Administrative;
        } else if (title.contains("*אירוע חדש*") || title.contains("*New Event*")) {
            if(text.contains("הודעת קבוצת")) {
                return CallType.Group;
            } else if(text.contains("הודעת צוות")) {
                return CallType.Shift;
            }
        }
        return CallType.OnCall;
    }
}
