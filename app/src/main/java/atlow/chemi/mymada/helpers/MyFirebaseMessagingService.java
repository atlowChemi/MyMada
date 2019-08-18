package atlow.chemi.mymada.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.codeC;
import atlow.chemi.mymada.contactChatActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static String token;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String code, msg, title, type, fireKey, name;
        type = remoteMessage.getData().get("type");
        code = remoteMessage.getData().get("code");
        fireKey = remoteMessage.getData().get("key");
        name = remoteMessage.getData().get("name");
        msg = remoteMessage.getData().get("msg");
        title = remoteMessage.getData().get("title");
        if (type != null && type.equals("newChat"))
            sendToChat(msg,name);
        else if (type != null && type.equals("newC"))
            sendNotification(1,msg,title,code);
        else if (code.length() > 0)
            sendNotification(code, fireKey, 5);
        else if (msg.length() > 0 && title.length() > 0)
            sendNotification(msg, title);
    }
    private void sendNotification(int type, String msg, String title, String code) {
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Push").setContentTitle("בקשת קוד חדשה!")
                .setContentText(title).setSmallIcon(R.drawable.ic_siren)
                .setLargeIcon(BitmapFactory.decodeResource(getApplication().getResources(), R.mipmap.ic_launcher))
                .setContentIntent(null).setAutoCancel(true).addAction(R.drawable.ic_edit, "אשר קוד", null)
                .setTicker("בקשה חדשה לקוד הפעלה!").setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
    }
    private void sendNotification(String msg, String title) {
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)new NotificationCompat.Builder(this, "Push")
                .setSmallIcon(R.drawable.ic_siren).setContentTitle("הודעה חדשה!")
                .setContentText(title).setStyle(new NotificationCompat.BigTextStyle().bigText(msg)
                        .setSummaryText("לחץ כאן להפעלת הקוד")).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(null);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
        }
    }
    private void sendNotification(String code, String fireKey, int differ) {
        Intent intent = new Intent(this, codeC.class);
        intent.putExtra("code", code);
        intent.putExtra("fireKey", fireKey);
        intent.putExtra("toCode", true);
        Log.d("KEYERR", "the key is: " + fireKey);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)new NotificationCompat.Builder(this, "Push")
                .setSmallIcon(R.drawable.ic_siren).setContentTitle("קוד ההפעלה אושר")
                .setContentText("קוד ההפעלה שלך הינו: " + code).setStyle(new NotificationCompat.BigTextStyle().bigText("קוד ההפעלה שלך הינו: " + code)
                        .setSummaryText("לחץ כאן להפעלת הקוד")).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int c = Integer.parseInt(code.replaceAll("[\\D]",""));
        if (notificationManager != null) {
            notificationManager.notify(c, notificationBuilder.build());
        }
    }

    private void sendToChat(String msg, String name) {
        Intent notificationIntent = new Intent(getApplicationContext(), contactChatActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "Push")
                .setContentTitle("הודעת צ'אט חדשה!").setContentText("הודעה חדשה מאת ההנהלה").setSmallIcon(R.drawable.ic_siren)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(getApplication().getResources(), R.mipmap.ic_launcher))
                .setContentIntent(content).setAutoCancel(true).setTicker("הודעה חדשה מאת ההנהלה")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        getSharedPreferences("Settings", 0).edit().putString("fcm", s).apply();
    }
    public static void getToken(Context context) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Crashlytics.log("didn't succeed getting token");
                Crashlytics.logException(task.getException());
                return;
            }
            context.getSharedPreferences("Settings", 0).edit().putString("fcm", task.getResult().getToken()).apply();
        });
    }
}