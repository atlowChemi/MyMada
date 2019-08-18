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

import android.provider.Settings;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import atlow.chemi.mymada.MokedPage;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.smsRes;


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
        SharedPreferences notSP = context.getSharedPreferences("notifications", 0);
        boolean win = sp.getBoolean("win", true);
        boolean not = sp.getBoolean("not", true);
        boolean ShabbatM = sp.getBoolean("killS", false);
        boolean stat = sp.getBoolean("stat", false);
        boolean statVib = sp.getBoolean("statVib", false);
        boolean silent = true;
        boolean silentVib = true;
        Uri url = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            silent = false;
        if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
            silentVib = false;
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
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), hachtava, 0);
        PendingIntent testPI = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), launchIntent, 0);
        if (intent != null) {
            try {
                text = intent.getCharSequenceExtra("text").toString();
            } catch (NullPointerException e) {
                text = "";
                Crashlytics.log("text extra is null!\n" + e);
            }
            callT = intent.getIntExtra("callT", 1);
            Log.d("callT", "" + callT);
            switch (callT) {
                case 1:
                    callType = sp.getBoolean("callT1", true);
                    string = "T1";
                    break;
                case 2:
                    callType = sp.getBoolean("callT2", true);
                    string = "T2";
                    break;
                case 3:
                    callType = sp.getBoolean("callT3", true);
                    string = "T3";
                    break;
                case 4:
                    callType = sp.getBoolean("callT4", true);
                    string = "T4";
                    break;
                case 5:
                    callType = sp.getBoolean("callT3", true);
                    string = "T3";
                    break;
                case 6:
                    callType = sp.getBoolean("callT6", true);
                    string = "T6";
                    break;
                default:
                    callType = sp.getBoolean("callT1", true);
                    string = "T1";
                    break;
            }
            Log.d("callT", "t6 is: " + callType);
            smsRes.newC(text);
            smsRes.alerT(madasApp);
        }
        cut = sp.getBoolean("cut", false);
        if (testAp) {
            callType = true;
        }
        Log.d("callT", "t6 is now: " + callType);
        Uri uri;
        if (string.equals("T6") || string.equals("T4")) {
            vib = sp.getBoolean("NvibT4", true);
            sou = sp.getBoolean("NsouT4", true);
            app = sp.getBoolean("NappT4", false);
            uri = Uri.parse(sp.getString("NuRiT4", ""));
        } else if (cut) {
            vib = sp.getBoolean("Nvib" + string, true);
            sou = sp.getBoolean("Nsou" + string, true);
            app = sp.getBoolean("Napp" + string, false);
            uri = Uri.parse(sp.getString("NuRi" + string, ""));
        } else {
            callType = true;
            vib = sp.getBoolean("Nvib", true);
            sou = sp.getBoolean("Nsou", true);
            app = sp.getBoolean("Napp", false);
            uri = Uri.parse(sp.getString("NuRi", ""));
        }
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int sleep = sp.getInt("sleep", 0);
        int nightStart = sp.getInt("nightStart", 23);
        int nightEnd = sp.getInt("nightEnd", 7);
        if (hour <= nightEnd || hour >= nightStart) {
            switch (sleep) {
                case 1:
                case 4:
                case 7:
                    sou = false;
                    break;
                case 2:
                case 5:
                case 8:
                    sou = false;
                    vib = false;
                    break;
            }
        } else {
            switch (sleep) {
                case 3:
                case 4:
                case 5:
                    sou = false;
                    break;
                case 6:
                case 7:
                case 8:
                    sou = false;
                    vib = false;
                    break;
            }
        }
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
                    service.putExtra("vib", vib && (!statVib || !silentVib));
                    service.putExtra("sou", sou && (!stat || !silent));
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
