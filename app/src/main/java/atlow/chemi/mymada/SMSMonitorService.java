package atlow.chemi.mymada;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;
import java.util.TimeZone;


public class SMSMonitorService extends Service {
    private BroadcastReceiver callExplicitReceiver;
    protected static String txtS = "";
    protected String smsSender = "", string, smsBody = "", Grp2 = "grpOthers", Grp3 = "grpTog", CnlBg = "runInBg", CnlWin = "notWithWin";
    public NotificationManager manager;
    final protected int NOTIF_ID = 1;
    protected static int numMessages = 0;
    protected PendingIntent piR;
    public static Boolean testB = false;
    public long[] lng = {0, 0, 0, 0, 0, 0, 0, 0, 0}, lng2 = {0, 900L, 200, 900, 200, 900, 200, 900L, 200, 900, 200};
    public Context context;
    public static final String SMS_BUNDLE = "pdus";
    private MediaPlayer mMediaPlayer;
    int currentVolume, callT = 1;
    protected boolean callType, cut, vib, sou, app, timeLegal = true, OreoVib, OreoSou;
    public static float vo = 0;
    private Uri uri, url;
    final SmsManager sms = SmsManager.getDefault();


    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static void testR()   {
        testB = true;
        }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            Intent callSNI = new Intent(this, HomePage.class);
            callSNI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, callSNI, 0);
            Crashlytics.log("sending SmsMonitor notif");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                setNotifChannels();
            NotificationCompat.Builder buil = new NotificationCompat.Builder(this, CnlBg)
                    .setContentTitle(getApplicationContext().getResources().getString(R.string.BGServiceTtl))
                    .setTicker(getApplicationContext().getResources().getString(R.string.BGServiceTicker))
                    .setContentText(getApplicationContext().getResources().getString(R.string.BGServiceTxt))
                    .setContentIntent(pendingIntent)
                    .setGroup("BG")
                    .setPriority(NotificationManagerCompat.IMPORTANCE_MIN)
                    //.setColorized(true)
                    .setShowWhen(false)
                    .setColor(Color.argb(50, 76, 119, 247))
                    .setSound(null)
                    .setVibrate(null)
                    .setOngoing(true);
            try {
                buil.setSmallIcon(R.drawable.ic_noti);
            }catch (Exception e) {
                Crashlytics.log("Error SmsMonitor Notificaion!");
                Crashlytics.logException(e);
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            buil.setPriority(NotificationManager.IMPORTANCE_MIN);
        }
        Notification notification = buil.build();
        startForeground(78, notification);


        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(9999);
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.callExplicitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                String act = intent.getAction();
                if (act != null && act.equals("android.provider.Telephony.SMS_RECEIVED")) {
                    context = ctx;
                    Bundle intentExtras = intent.getExtras();
                    if (intentExtras != null) {
                        Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                        assert sms != null;
                        smsBody = "";
                        for (Object sm : sms) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);
                            smsBody += smsMessage.getMessageBody();
                            smsSender = smsMessage.getOriginatingAddress();
                        }
                    }
                    url = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);
                    final SharedPreferences sp = context.getSharedPreferences("Settings", 0);
                    SharedPreferences notSP = context.getSharedPreferences("notifications", 0);
                    Log.v("number is:", smsSender);
                    if (smsSender.equals("MADA") || smsSender.equals("101") || smsSender.equals("Mada")) {
                        Log.v("succ number:", smsSender);
                        if ((smsBody.contains("אירוע חדש") || smsBody.contains("הודעת קבוצת") || smsBody.contains("הודעת צוות"))) {
                            if (smsBody.contains("שירות") || smsBody.contains("כלכלה") || smsBody.contains("תפילה")) {
                                //sheirut
                                callT = 6;
                                callType = sp.getBoolean("callT6", true);
                                string = "T6";
                            } else if (smsBody.contains("מנהלתי") || smsBody.contains("מינהלתי") || smsBody.contains("משמרת בנה")) {
                                //minhala
                                callT = 4;
                                callType = sp.getBoolean("callT4", true);
                                string = "T4";
                            } else if (smsBody.contains("אירוע חדש")) {
                                //conan
                                callT = 1;
                                callType = sp.getBoolean("callT1", true);
                                string = "T1";
                            } else if (smsBody.contains("הודעת קבוצת")) {
                                //kvutza
                                callT = 2;
                                callType = sp.getBoolean("callT2", true);
                                string = "T2";
                            } else if (smsBody.contains("הודעת צוות")) {
                                //shift
                                callT = 3;
                                callType = sp.getBoolean("callT3", true);
                                string = "T3";
                            } else {
                                callT = 1;
                                callType = sp.getBoolean("callT1", true);
                                string = "T1";
                            }
                        } else {
                            if (smsBody.contains("שירות") || smsBody.contains("כלכלה") || smsBody.contains("תפילה")) {
                                //sheirut
                                callT = 6;
                                callType = sp.getBoolean("callT6", true);
                                string = "T6";
                            } else if (smsBody.contains("על מידע זה חל חיסיון רפואי")) {
                                //Old SMS nusach
                                callT = 5;
                                callType = sp.getBoolean("callT3", true);
                                string = "T3";
                            } else {
                                //conan
                                callT = 1;
                                callType = sp.getBoolean("callT1", true);
                                string = "T1";
                            }
                        }
                    } else {
                        smsBody = "";
                        return;
                    }
                    cut = sp.getBoolean("cut", false);
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
                    if ((hour <= nightEnd || hour >= nightStart) || (hour <= nightEnd && hour >= nightStart)) {
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
                    //this will update the UI with message
                    if (smsSender.equals("MADA") || smsSender.equals("101") || smsSender.equals("Mada") && callType) {
                        Intent intent2 = new Intent(Intent.ACTION_SENDTO);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intent2.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
                        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent2, 0);
                        if (!testB) {
                            Intent intentReal = new Intent(Intent.ACTION_SENDTO);
                            intentReal.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            intentReal.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
                            intentReal.putExtra("address", smsSender);
                            piR = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentReal, PendingIntent.FLAG_UPDATE_CURRENT);
                            smsRes.newC(smsBody);
                            smsRes.alerT(piR);
                        } else {
                            Intent intentFake = new Intent(Intent.ACTION_SENDTO);
                            intentFake.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            intentFake.setData(Uri.parse("smsto:"));  // This ensures only SMS apps respond
                            intentFake.putExtra("address", "MADA");
                            piR = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentFake, 0);
                        }
                        Intent hachtava = new Intent(context, MokedPage.class);
                        hachtava.putExtra("msg", true);
                        hachtava.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        hachtava.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), hachtava, 0);
                        Boolean win = sp.getBoolean("win", true);
                        Boolean not = sp.getBoolean("not", true);
                        Boolean lop = sp.getBoolean("loop", false);
                        Boolean ShabbatM = sp.getBoolean("killS", false);
                        boolean stat = sp.getBoolean("stat", false);
                        boolean statVib = sp.getBoolean("statVib", false);
                        boolean silent = true;
                        boolean silentVib = true;
                        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
                            silent = false;
                        if (am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
                            silentVib = false;
                        if (not && callType) {
                            final NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            PendingIntent intentA = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity1.class), 0);
                            final NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context, CnlWin).setContentTitle("סמס חדש מהמוקד!")
                                    .setContentText("לחץ כאן בכדי להכתיב את הטפסים.").setSmallIcon(R.drawable.ic_siren)
                                    .setLights(Color.rgb(255,0,0), 40, 50)
                                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                                    .setContentIntent(intentA).setAutoCancel(true).addAction(R.drawable.ic_edit, "הכתב", pendingIntent)
                                    .addAction(R.drawable.ic_info, "פרטים", piR).setTicker("התקבל אירוע חדש מהמוקד!")
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(smsBody));
                            if (testB)
                                builder.setStyle(new NotificationCompat.BigTextStyle().bigText("בדיקת סמס!"));
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
                                                notifManager.cancel(numMessages);
                                                builder.setVibrate(lng).setSound(null).setChannelId(CnlWin);
                                                Notification mNotification = builder.build();
                                                notifManager.notify(++numMessages, mNotification);
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
                                    serviceChannel.setVibrationPattern(lng2);
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
                                    manager = getSystemService(NotificationManager.class);
                                    if (manager != null) {
                                        manager.createNotificationChannel(serviceChannel);
                                    }
                                    builder.setChannelId(cnlId);
                                }
                            }
                            builder.setGroup("Calls");
                            Notification mNotification = builder.build();
                            if (lop) {
                                mNotification.flags |= Notification.FLAG_INSISTENT;
                            }
                            if (sp.getBoolean("ride", false)) {
                                notifManager.cancelAll();
                                notifManager.notify(0, mNotification);
                            } else
                                notifManager.notify(++numMessages, mNotification);
                        }
                        /**/
                        if (win && callType) {
                            Intent i = new Intent(context, smsRes.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            i.putExtra("callT", callT);
                            context.startActivity(i);
                        }
                        testB = false;
                    }
                }
            }
        };
        registerReceiver(callExplicitReceiver, intentFilter);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(callExplicitReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("SMSMonitorService", e.getMessage());
            Crashlytics.log(e.getMessage());
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setNotifChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");
            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);
            manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannelGroup(group2);
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
