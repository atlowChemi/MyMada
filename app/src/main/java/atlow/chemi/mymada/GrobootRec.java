package atlow.chemi.mymada;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static atlow.chemi.mymada.Applicatio.Cnl101;
import static atlow.chemi.mymada.Applicatio.CnlConan;
import static atlow.chemi.mymada.Applicatio.CnlGroup;
import static atlow.chemi.mymada.Applicatio.CnlMinhala;
import static atlow.chemi.mymada.Applicatio.CnlMishmeret;

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
    protected boolean callType, cut, vib, sou, app;
    private NotificationCompat.Builder builder;
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
            text = intent.getCharSequenceExtra("text").toString();
            callT = intent.getIntExtra("callT", 1);
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
                default:
                    callType = true;
                    string = "T1";
                    CNL = CnlConan + notSP.getInt(CnlConan, 0);
                    break;
            }
            smsRes.newC(text);
            smsRes.alerT(madasApp);
        }
        cut = sp.getBoolean("cut", false);
        if(testAp || !cut)  {
            callType = true;
        }
        Uri uri;
        if (string.equals("T4"))    {
            vib = sp.getBoolean("Nvib" + string, true);
            sou = sp.getBoolean("Nsou" + string, true);
            app = sp.getBoolean("Napp" + string, false);
            uri = Uri.parse(sp.getString("NuRi" + string, ""));
        }   else if (cut) {
            vib = sp.getBoolean("Nvib" + string, true);
            sou = sp.getBoolean("Nsou" + string, true);
            app = sp.getBoolean("Napp" + string, false);
            uri = Uri.parse(sp.getString("NuRi" + string, ""));
        }   else {
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
            String tick = string.equals("T4") ? "התקבלה הודעה חדש באפליקציה!" : "התקבל אירוע חדש באפליקציה!";
            String titl = string.equals("T4") ? "הודעה חדשה באפליקציה!" : "אירוע חדש באפליקציה!";
            notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context, CNL).setContentTitle(titl)
                    .setContentText("לחץ כאן בכדי להכתיב טפסים.").setSmallIcon(R.drawable.ic_siren)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setVibrate(lng).setSound(null)
                    .setContentIntent(pendingIntent).setAutoCancel(true).addAction(R.drawable.ic_edit, "הכתב", pendingIntent)
                    .setTicker(tick).setStyle(new NotificationCompat.BigTextStyle().bigText(text));
            if(testAp)
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("בדיקת אפליקציה!")).addAction(R.drawable.ic_info, "פרטים", testPI);
            else if (madasApp != null)   {
                builder.addAction(R.drawable.ic_info, "פרטים", madasApp);
            }
            if (!win) {
                if (!statVib || silentVib) {
                    if (vib)
                        builder.setVibrate(lng2);
                    else
                        builder.setVibrate(lng);
                }
                if (sou && (!stat || !silent)) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                        vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
                        if (app) {
                            builder.setSound(uri, AudioManager.STREAM_ALARM);
                        }
                        if (!app) {
                            builder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert), AudioManager.STREAM_ALARM);
                        }
                    }
                }
                if(ShabbatM)    {
                    int finishTime = Integer.parseInt(sp.getString("timeK", "6")); //6 secs
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if(sp.getBoolean("ride",false)) {
                                notifManager.cancelAll();
                                builder.setVibrate(lng).setSound(null).setChannelId(CNL);
                                Notification mNotification = builder.build();
                                notifManager.notify(0, mNotification);
                            }
                            else {
                                notifManager.cancel(NOTIF_ID);
                                builder.setVibrate(lng).setSound(null);
                                Notification mNotification = builder.build();
                                notifManager.notify(++NOTIF_ID, mNotification);
                            }
                            final AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                            //int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                            //Toast.makeText(getApplicationContext(),"maxVol: " + a + "\noldVol: " + currentVolume,Toast.LENGTH_LONG).show();
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                            try {
                                mMediaPlayer.setVolume(vo, vo);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, finishTime * 1000);
                }
            }
            Notification mNotification = builder.build();
            if(sp.getBoolean("loop",false)) {
                mNotification.flags |= Notification.FLAG_INSISTENT;
            }
            if(sp.getBoolean("ride",false)) {
                notifManager.cancelAll();
                notifManager.notify(notNum, mNotification);
            } else
                notNum = ++NOTIF_ID;
                notifManager.notify(NOTIF_ID, mNotification);
            }
            /**/
            if (win && (callType || testAp)) {
                Intent i = new Intent(context, smsRes.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                i.putExtra("notNum", notNum);
                i.putExtra("loop", sp.getBoolean("loop", false));
                //TODO: make smsRes shut off the notification (remove and regenerate);
                i.putExtra("callT", callT);

                context.startActivity(i);
            }
        testAp = false;
        }
}
