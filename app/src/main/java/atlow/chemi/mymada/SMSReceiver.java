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
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SMSReceiver extends BroadcastReceiver {
    protected static String txtS = "";
    protected String smsSender = "", string, smsBody = "";
    final protected int NOTIF_ID = 1;
    protected static int numMessages = 0;
    protected PendingIntent piR;
    public static Boolean testB = false;
    public long[] lng = {0,0,0,0,0,0,0,0,0}, lng2 = {0, 900L, 200, 900, 200, 900, 200, 900L, 200, 900, 200};
    public Context context;
    public static final String SMS_BUNDLE = "pdus";
    private MediaPlayer mMediaPlayer;
    int currentVolume, callT = 1;
    protected boolean callType, cut, vib, sou, app, timeLegal = true;
    public static float vo = 0;
    private Uri uri;
    final SmsManager sms = SmsManager.getDefault();
    public void onReceive(final Context context,Intent intent) {
        this.context = context;
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            assert sms != null;
            for (Object sm : sms) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);
                smsBody += smsMessage.getMessageBody();
                smsSender = smsMessage.getOriginatingAddress();
            }
        }
        Toast.makeText(context, "NEW SMS", Toast.LENGTH_SHORT).show();
        final SharedPreferences sp = context.getSharedPreferences("Settings", 0);
        if (smsSender.equals("MADA") || smsSender.equals("101") || smsSender.equals("Mada") || smsSender.contains("7931329")) {
            if ((smsBody.contains("אירוע חדש") || smsBody.contains("הודעת קבוצת") || smsBody.contains("הודעת צוות"))) {
                if (smsBody.contains("אירוע חדש")) {
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
                //conan
                callT = 1;
                callType = sp.getBoolean("callT1", true);
                string = "T1";
            }
        } else {
            return;
        }
        cut = sp.getBoolean("cut", false);
        if (cut) {
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
                String NOTIFICATION_CHANNEL_ID = "CallRec";
                final NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "זיהוי מקרים", NotificationManager.IMPORTANCE_HIGH);
                    // Configure the notification channel.
                    notificationChannel.setDescription("התראות בזיהוי של מקרים");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    if (!statVib || silentVib) {
                        if (vib)
                            notificationChannel.setVibrationPattern(lng2);
                        else
                            notificationChannel.setVibrationPattern(lng);
                    }
                    notificationChannel.enableVibration(true);
                    notifManager.createNotificationChannel(notificationChannel);
                }
                PendingIntent intentA = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity1.class), 0);
                final NotificationCompat.Builder builder = (Builder) new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setContentTitle("סמס חדש מהמוקד!")
                        .setContentText("לחץ כאן בכדי להכתיב את הטפסים.").setSmallIcon(R.drawable.ic_siren)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(intentA).setAutoCancel(true).addAction(R.drawable.ic_edit, "הכתב", pendingIntent)
                        .addAction(R.drawable.ic_info, "פרטים", piR).setTicker("התקבל אירוע חדש מהמוקד!")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(smsBody));
                if (testB)
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText("בדיקת סמס!"));
                if (!win) {
                    if (!statVib || silentVib) {
                        if (vib)
                            builder.setVibrate(lng2);
                        else
                            builder.setVibrate(lng);
                    }
                    if (sou && (!stat || !silent)) {
                        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                        vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
                        if (app) {
                            builder.setSound(uri, AudioManager.STREAM_ALARM);
                        }
                        if (!app) {
                            Uri url = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert);
                            builder.setSound(url, AudioManager.STREAM_ALARM);
                        }
                    }
                    if (ShabbatM) {
                        int finishTime = Integer.parseInt(sp.getString("timeK", "6")); //6 secs
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                if (sp.getBoolean("ride", false)) {
                                    notifManager.cancelAll();
                                    builder.setVibrate(lng).setSound(null);
                                    Notification mNotification = builder.build();
                                    notifManager.notify(0, mNotification);
                                } else {
                                    notifManager.cancel(numMessages);
                                    builder.setVibrate(lng).setSound(null);
                                    Notification mNotification = builder.build();
                                    notifManager.notify(++numMessages, mNotification);
                                }
                                final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                //int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                                //Toast.makeText(getApplicationContext(),"maxVol: " + a + "\noldVol: " + currentVolume,Toast.LENGTH_LONG).show();
                                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                                try {
                                    mMediaPlayer.setVolume(vo, vo);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, finishTime * 1000);
                    }
                }
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
    public static void testR()   {
        testB = true;
    }
}