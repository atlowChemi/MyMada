package atlow.chemi.mymada;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static atlow.chemi.mymada.Applicatio.Cnl101;
import static atlow.chemi.mymada.Applicatio.CnlConan;
import static atlow.chemi.mymada.Applicatio.CnlGroup;
import static atlow.chemi.mymada.Applicatio.CnlMinhala;
import static atlow.chemi.mymada.Applicatio.CnlMishmeret;
import static atlow.chemi.mymada.Applicatio.CnlWin;
import static atlow.chemi.mymada.Applicatio.Grp3;
import static atlow.chemi.mymada.Applicatio.manager;

public class GrobootRec extends BroadcastReceiver {
    protected String title, text, CNL;
    protected static String string = "T1";
    protected static PendingIntent madasApp;
    protected Context context;
    public static Boolean testAp = false;
    public long[] lng = {0, 0, 0, 0, 0, 0, 0, 0, 0}, lng2 = {0, 900L, 200, 900, 200, 900, 200, 900L, 200, 900, 200};
    private static int NOTIF_ID = 5000;
    private MediaPlayer mMediaPlayer;
    protected int currentVolume, callT, notNum = 0;
    public static float vo = 0;
    protected boolean callType, cut, vib, sou, app, OreoVib, OreoSou;
    private NotificationCompat.Builder builder;
    private Uri uri, url;
    private NotificationManager notifManager;
    public GrobootRec() {
    }
    public static void madasApp(PendingIntent pendingIntent)    {
        madasApp = pendingIntent;
    }
    @Override
    public void onReceive(final Context context, Intent intent) {
        final SharedPreferences sp = context.getSharedPreferences("Settings", 0);
        SharedPreferences notSP = context.getSharedPreferences("notifications", 0);
        boolean win = sp.getBoolean("win", true);
        boolean not = sp.getBoolean("not", true);
        boolean ShabbatM = sp.getBoolean("killS",false);
        boolean stat = sp.getBoolean("stat",false);
        boolean statVib = sp.getBoolean("statVib",false);
        boolean silent = true;
        boolean silentVib = true;
        url = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            silent = false;
        if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
            silentVib = false;
        Intent hachtava = new Intent(context,MokedPage.class);
        hachtava.putExtra("msg", true);
        hachtava.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        hachtava.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent launchIntent;
        try {
            launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }catch (Exception e)    {
            launchIntent = new Intent(context, MokedPage.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), hachtava, 0);
        PendingIntent testPI = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), launchIntent, 0);
        if (intent != null) {
            try {
                text = intent.getCharSequenceExtra("text").toString();
            }catch (NullPointerException e) {
                text = "";
                Crashlytics.log("text extra is null!\n" + e);
            }
            callT = intent.getIntExtra("callT", 1);
            Log.d("callT", "" + callT);
            switch (callT)  {
                case 1:
                    callType = sp.getBoolean("callT1", true);
                    string = "T1";
                    CNL = CnlConan + notSP.getInt(CnlConan, 0);
                    break;
                case 2:
                    callType = sp.getBoolean("callT2", true);
                    string = "T2";
                    CNL = CnlGroup + notSP.getInt(CnlGroup, 0);
                    break;
                case 3:
                    callType = sp.getBoolean("callT3", true);
                    string = "T3";
                    CNL = CnlMishmeret + notSP.getInt(CnlMishmeret, 0);
                    break;
                case 4:
                    callType = sp.getBoolean("callT4", true);
                    string = "T4";
                    CNL = CnlMinhala + notSP.getInt(CnlMinhala, 0);
                    break;
                case 5:
                    callType = sp.getBoolean("callT3", true);
                    string = "T3";
                    CNL = CnlMishmeret + notSP.getInt(CnlMishmeret, 0);
                    break;
                case 6:
                    callType = sp.getBoolean("callT6", true);
                    string = "T6";
                    CNL = CnlMinhala + notSP.getInt(CnlMinhala, 0);
                    break;
                default:
                    callType = sp.getBoolean("callT1", true);
                    string = "T1";
                    CNL = CnlConan + notSP.getInt(CnlConan, 0);
                    break;
            }
            Log.d("callT", "t6 is: " + callType);
            smsRes.newC(text);
            smsRes.alerT(madasApp);
        }
        cut = sp.getBoolean("cut", false);
        if(testAp)  {
            callType = true;
        }
        Log.d("callT", "t6 is now: " + callType);
        if (string.equals("T6") || string.equals("T4")) {
            vib = sp.getBoolean("NvibT4", true);
            sou = sp.getBoolean("NsouT4", true);
            app = sp.getBoolean("NappT4", false);
            uri = Uri.parse(sp.getString("NuRiT4", ""));
        }   else if (cut) {
            vib = sp.getBoolean("Nvib" + string, true);
            sou = sp.getBoolean("Nsou" + string, true);
            app = sp.getBoolean("Napp" + string, false);
            uri = Uri.parse(sp.getString("NuRi" + string, ""));
        }   else {
            callType = true;
            vib = sp.getBoolean("Nvib", true);
            sou = sp.getBoolean("Nsou", true);
            app = sp.getBoolean("Napp", false);
            uri = Uri.parse(sp.getString("NuRi", ""));
            CNL = Cnl101 + notSP.getInt(Cnl101, 0);
        }
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int sleep = sp.getInt("sleep", 0);
        int nightStart = sp.getInt("nightStart", 23);
        int nightEnd = sp.getInt("nightEnd", 7);
        if ((hour <= nightEnd || hour >= nightStart) || (hour <= nightEnd && hour >= nightStart)) {
            switch (sleep)  {
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
        }
        else  {
            switch (sleep)  {
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
        if (not && (callType)) {
            String tick = string.equals("T4") || string.equals("T6") ? "התקבלה הודעה חדש באפליקציה!" : "התקבל אירוע חדש באפליקציה!";
            String titl = string.equals("T4") || string.equals("T6") ? "הודעה חדשה באפליקציה!" : "אירוע חדש באפליקציה!";
            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context, CnlWin).setContentTitle(titl)
                    .setContentText("לחץ כאן בכדי להכתיב טפסים.").setSmallIcon(R.drawable.ic_siren)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setVibrate(lng).setSound(null)
                    .setContentIntent(pendingIntent).setAutoCancel(true).addAction(R.drawable.ic_edit, "הכתב", pendingIntent)
                    .setTicker(tick).setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            if (testAp)
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("בדיקת אפליקציה!")).addAction(R.drawable.ic_info, "פרטים", testPI);
            else if (madasApp != null) {
                builder.addAction(R.drawable.ic_info, "פרטים", madasApp);
            }
            if (!win) {
                if (!statVib || silentVib) {
                    OreoVib = true;
                    if (vib)
                        builder.setVibrate(lng2);
                    else {
                        OreoVib = false;
                        builder.setVibrate(lng);
                    }
                }
                if (sou && (!stat || !silent)) {
                    OreoSou = true;
                    final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
                    if (app) {
                        builder.setSound(uri, AudioManager.STREAM_ALARM);
                    }
                    if (!app) {
                        builder.setSound(url, AudioManager.STREAM_ALARM);
                    }
                }else {
                    OreoSou = false;
                }
                if (ShabbatM) {
                    int finishTime = Integer.parseInt(sp.getString("timeK", "6")); //6 secs
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (sp.getBoolean("ride", false)) {
                                notifManager.cancelAll();
                                builder.setVibrate(lng).setSound(null).setChannelId(CnlWin);
                                Notification mNotification = builder.build();
                                notifManager.notify(0, mNotification);
                            } else {
                                notifManager.cancel(NOTIF_ID);
                                builder.setVibrate(lng).setSound(null).setChannelId(CnlWin);
                                Notification mNotification = builder.build();
                                notifManager.notify(++NOTIF_ID, mNotification);
                            }
                            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                            //int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                            //Toast.makeText(getApplicationContext(),"maxVol: " + a + "\noldVol: " + currentVolume,Toast.LENGTH_LONG).show();
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                            mMediaPlayer = new MediaPlayer();
                            try {
                                mMediaPlayer.setVolume(vo, vo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, finishTime * 1000);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String cnlId = "generated" + Calendar.getInstance().getTimeInMillis();
                    AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setLegacyStreamType(AudioManager.STREAM_ALARM).build();
                    NotificationChannel serviceChannel = new NotificationChannel(cnlId, "ערוץ זמני מותאם אישית", NotificationManager.IMPORTANCE_HIGH);
                    serviceChannel.enableVibration(OreoVib);
                    serviceChannel.setBypassDnd(true);
                    serviceChannel.setDescription("התראות עבור אירועי חירום");
                    serviceChannel.enableLights(true);
                    serviceChannel.setShowBadge(true);
                    serviceChannel.setLightColor(Color.RED);
                    if (OreoSou) {
                        if (app)
                            serviceChannel.setSound(uri, attributes);
                        else
                            serviceChannel.setSound(url, attributes);
                    }else
                        serviceChannel.setSound(null, attributes);
                    serviceChannel.setGroup(Grp3);
                    manager.createNotificationChannel(serviceChannel);
                    builder.setChannelId(cnlId);
                }
            }
            Notification mNotification = builder.build();
            if (sp.getBoolean("loop", false)) {
                mNotification.flags |= Notification.FLAG_INSISTENT;
            }
            if (sp.getBoolean("ride", false)) {
                notifManager.cancelAll();
                notifManager.notify(notNum, mNotification);
            } else {
                notNum = ++NOTIF_ID;
                notifManager.notify(NOTIF_ID, mNotification);
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
}
