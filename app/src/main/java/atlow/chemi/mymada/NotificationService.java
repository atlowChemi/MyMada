package atlow.chemi.mymada;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Objects;

import static atlow.chemi.mymada.Applicatio.manager;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class NotificationService extends NotificationListenerService {
    protected String text;
    Context context;
    public static boolean isNotificationAccessEnabled = false;
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        Boolean Mapp = sp.getBoolean("Mapp", false);
        if (Mapp && (statusBarNotification.getPackageName().contains("mdaemergency")
                || statusBarNotification.getPackageName().contains("mda.health")
                || statusBarNotification.getPackageName().equals("atlow.mymadaadmin"))) {
            Bundle extras = statusBarNotification.getNotification().extras;
            String pack = statusBarNotification.getPackageName();
            String ticker;
            try {
                ticker = statusBarNotification.getNotification().tickerText.toString();
            }catch (Exception e)    {
                ticker = "";
            }
            String title = extras.getString("android.title");
            try {
                if (extras.getCharSequence("android.text") != null)
                    text = Objects.requireNonNull(extras.getCharSequence("android.text")).toString();
                else
                    text = "תקלה";
            }catch (Exception e)  {
                text = "\nתקלה!";
                Crashlytics.log("notification text is null!\n" + e);
            }
            Intent intent = new Intent("MADA_APP");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("pack", pack);
            intent.putExtra("ticker", ticker);
            intent.putExtra("title", title);
            intent.putExtra("text", text);
            if (title != null) {
                if (title.equals("מגן דוד אדום")) {
                    return;
                }
                //conan, group, mishmeret, minhala, oldType, sherut.
                if (title.contains("*אירוע חדש*")) {
                    if (text.contains("שירות") || text.contains("כלכלה") || text.contains("תפילה"))
                        intent.putExtra("callT", 6);
                    else if (text.contains("מנהלתי") || text.contains("מינהלתי") || text.contains("משמרת בנה"))
                        intent.putExtra("callT", 4);
                    else if(text.contains("אירוע חדש"))
                        intent.putExtra("callT",1);
                    else if(text.contains("הודעת קבוצת"))
                        intent.putExtra("callT",2);
                    else if(text.contains("הודעת צוות"))
                        intent.putExtra("callT",3);
                    else if (text.contains("על מידע זה חל חיסיון רפואי")) {
                        intent.putExtra("callT", 5);
                    }
                    else {
                        if (text.contains("על מידע זה חל חיסיון רפואי"))
                            intent.putExtra("callT", 5);
                        else
                            intent.putExtra("callT", 1);
                    }
                    String time = title.replace("*אירוע חדש*", "");
                    intent.putExtra("time", time);
                    GrobootRec.madasApp(statusBarNotification.getNotification().contentIntent);
                    sendBroadcast(intent);
                }else if (title.contains("*הודעה מנהלתית חדשה*"))   {
                    intent.putExtra("callT", 4);
                    if (text.contains("שירות") || text.contains("כלכלה") || text.contains("תפילה"))
                        intent.putExtra("callT", 6);
                    if (text.contains("על מידע זה חל חיסיון רפואי"))
                        intent.putExtra("callT", 5);
                    String time = title.replace("*הודעה מנהלתית חדשה*", "");
                    intent.putExtra("time", time);
                    GrobootRec.madasApp(statusBarNotification.getNotification().contentIntent);
                    sendBroadcast(intent);
                }
            } else {
                GrobootRec.madasApp(statusBarNotification.getNotification().contentIntent);
                sendBroadcast(intent);
            }
        }
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification)  {
        //nothing yet;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && statusBarNotification.getPackageName().equals("atlow.chemi.mymada")) {
            Notification notification = statusBarNotification.getNotification();
            if (notification.getChannelId().contains("generated")) {
                manager.deleteNotificationChannel(notification.getChannelId());
                Log.v("searchable", notification.getChannelId());
            }
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("REBIND", "NotificationListenerService onStartCommand begin");
        if (intent != null)
            Applicatio.tryConnectService();
        return START_STICKY;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getActiveNotifications();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.N)
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Notification listener disconnected - requesting rebind
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }
}
