package atlow.chemi.mymada;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String code, msg, title, type, fireKey;
        type = remoteMessage.getData().get("type");
        code = remoteMessage.getData().get("code");
        fireKey = remoteMessage.getData().get("key");
        msg = remoteMessage.getData().get("msg");
        title = remoteMessage.getData().get("title");
        if (type != null && type.equals("newC"))
            sendNotification(1,msg,title,code);
        else if (code.length() > 0)
            sendNotification(code, fireKey, 5);
        else if (msg.length() > 0 && title.length() > 0)
            sendNotification(msg, title);
    }
    private void sendNotification(int type, String msg, String title, String code) {
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)new NotificationCompat.Builder(getApplicationContext(), "Push").setContentTitle("בקשת קוד חדשה!")
                .setContentText(title).setSmallIcon(R.drawable.ic_siren)
                .setLargeIcon(BitmapFactory.decodeResource(getApplication().getResources(), R.mipmap.ic_launcher))
                .setContentIntent(null).setAutoCancel(true).addAction(R.drawable.ic_edit, "אשר קוד", null)
                .setTicker("בקשה חדשה לקוד הפעלה!").setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
    }
    private void sendNotification(String msg, String title) {
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)new NotificationCompat.Builder(this, "Push")
                .setSmallIcon(R.drawable.ic_siren).setContentTitle("הודעה חדשה!")
                .setContentText(title).setStyle(new NotificationCompat.BigTextStyle().bigText(msg)
                        .setSummaryText("לחץ כאן להפעלת הקוד")).setAutoCancel(true).setSound(defaultSoundUri).setContentIntent(null);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int)System.currentTimeMillis(), notificationBuilder.build());
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
        notificationManager.notify(c, notificationBuilder.build());
    }
}