package atlow.chemi.mymada.recieversAndServices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.PendingIntentCompat;

import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import atlow.chemi.mymada.MokedPage;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.smsRes;
import atlow.chemi.mymada.calls.CallAlerts;
import atlow.chemi.mymada.calls.AlertDecision;


public class GrobootRec extends BroadcastReceiver {
    protected String title, text, Grp3 = "grpTog", CnlWin = "notWithWin";
    protected static String string = "T1";
    protected static PendingIntent madasApp;
    protected Context context;
    public static Boolean testAp = false;
    public long[] lng = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static int NOTIF_ID = 5000;
    protected int callT, notNum = 0;
    public static float vo = 0;
    protected boolean callType, cut, vib, sou, app;

    public GrobootRec() {
    }

    public static void madasApp(PendingIntent pendingIntent) {
        madasApp = pendingIntent;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final SharedPreferences sp = context.getSharedPreferences("Settings", 0);
        boolean win = sp.getBoolean("win", true);
        boolean not = sp.getBoolean("not", true);
        boolean ShabbatM = sp.getBoolean("killS", false);
        Uri url = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Intent hachtava = new Intent(context, MokedPage.class);
        hachtava.putExtra("msg", true);
        hachtava.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        hachtava.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent launchIntent = new Intent(context, MokedPage.class);
        if (isCallable(context.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency")))
            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        PendingIntent pendingIntent = PendingIntentCompat.getActivity(context, (int) System.currentTimeMillis(), hachtava, 0, false);
        PendingIntent testPI = PendingIntentCompat.getActivity(context, (int) System.currentTimeMillis(), launchIntent, 0, false);
        if (intent != null) {
            try {
                text = intent.getCharSequenceExtra("text").toString();
            } catch (NullPointerException e) {
                text = "";
//                Crashlytics.log("text extra is null!\n" + e);
            }
            callT = intent.getIntExtra("callT", 1);
            Log.d("callT", "" + callT);
            string = CallAlerts.suffix(callT);
            if (intent.hasExtra("sourceIntent")) madasApp = intent.getParcelableExtra("sourceIntent");
            Log.d("callT", "t6 is: " + callType);
            smsRes.newC(text);
            smsRes.alerT(madasApp);
        }
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        AlertDecision decision = CallAlerts.decide(sp.getAll(), callT, hour, am == null ? -1 : am.getRingerMode(), testAp);
        callType = decision.getEnabled();
        vib = decision.getOverlayVibrate();
        sou = decision.getSound();
        app = decision.getCustomSound();
        Uri uri = Uri.parse(decision.getRingtone());
        if (not && callType) {
            String tick = string.equals("T4") || string.equals("T6") ? "התקבלה הודעה חדש באפליקציה!" : "התקבל אירוע חדש באפליקציה!";
            String titl = string.equals("T4") || string.equals("T6") ? "הודעה חדשה באפליקציה!" : "אירוע חדש באפליקציה!";
            NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CnlWin).setContentTitle(titl)
                    .setContentText("לחץ כאן בכדי להכתיב טפסים.").setSmallIcon(R.drawable.ic_siren)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setVibrate(lng).setSound(null)
                    .setContentIntent(pendingIntent).setAutoCancel(true).addAction(R.drawable.ic_edit, "הכתב", pendingIntent)
                    .setTicker(tick).setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            builder.setGroup("Calls");
            builder.setGroupSummary(true);
            if (testAp)
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("בדיקת אפליקציה!")).addAction(R.drawable.ic_info, "פרטים", testPI);
            else if (madasApp != null) {
                builder.addAction(R.drawable.ic_info, "פרטים", madasApp);
            }
            if (!win) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setChannelId(CnlWin);
                }
                Notification mNotification = builder.build();
                /*if (sp.getBoolean("loop", false)) {
                    mNotification.flags |= Notification.FLAG_INSISTENT;
                }*/
                if (sp.getBoolean("ride", false)) {
                    notifManager.cancelAll();
                    notifManager.notify(notNum, mNotification);
                } else {
                    notNum = ++NOTIF_ID;
                    notifManager.notify(NOTIF_ID, mNotification);
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                    //If permission is granted start floating widget service
                    Intent service = new Intent(context , FloatingWidgetService.class);
                    service.putExtra("vib", vib);
                    service.putExtra("sou", sou);
                    service.putExtra("loop", sp.getBoolean("loop", false));
                    service.putExtra("uri", app ? uri.toString() : url.toString());
                    service.putExtra("timeK", sp.getString("timeK", "6"));
                    service.putExtra("ShabbatM", ShabbatM);
                    context.startService(service);
                }
            }
            /**/
            if (win && (callType || testAp)) {
                Intent i = new Intent(context, smsRes.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                i.putExtra("notNum", notNum);
                i.putExtra("loop", sp.getBoolean("loop", false));
                i.putExtra("callT", callT);
                context.startActivity(i);
            }
            testAp = false;
        }
        else if (!not && win && (callType || testAp)) {
            Intent i = new Intent(context, smsRes.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            i.putExtra("notNum", notNum);
            i.putExtra("loop", sp.getBoolean("loop", false));
            i.putExtra("callT", callT);
            context.startActivity(i);
        }
    }
    private boolean isCallable(Intent intent) {
        try {
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
