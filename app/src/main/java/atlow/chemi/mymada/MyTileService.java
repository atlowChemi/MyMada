package atlow.chemi.mymada;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import static atlow.chemi.mymada.Applicatio.CnlBg;

@TargetApi(Build.VERSION_CODES.N)
public class MyTileService extends TileService {
    private MetronomeAsyncTask2 metroTask;
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String)msg.obj;
            }
        };
    };
    AudioManager audioManager;
    private int currentVolume;
    public static float vo = 0;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        setCurrentState(Tile.STATE_INACTIVE);
    }

    @Override
    public void onTileRemoved() {
        setCurrentState(Tile.STATE_INACTIVE);
        super.onTileRemoved();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        // switch from active to passive based on tiles current state
        Handler handler = new Handler();
        switch (tile.getState()) {
            case Tile.STATE_ACTIVE:
                stopForeground(STOP_FOREGROUND_REMOVE);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            audioManager = (AudioManager) MyTileService.this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                            metroTask.stop();
                            metroTask = new MetronomeAsyncTask2();
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                        }catch (Exception e)    {
                            e.printStackTrace();
                        }
                    }
                });

                /*****/

                setCurrentState(Tile.STATE_INACTIVE);
                break;
            case Tile.STATE_INACTIVE:

                Intent callServiceNotificationIntent = new Intent(MyTileService.this, UtillTools.class);
                callServiceNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, callServiceNotificationIntent, 0);
                NotificationCompat.Builder buil = new NotificationCompat.Builder(this, CnlBg)
                        .setContentTitle("מטרונום פועל ברקע")
                        .setTicker("מטרונום פועל עכשיו ברקע")
                        .setContentText("התראה זו מונעת כיבוי המטרונום באופן אוטומטי במטרת חיסכון בסוללה.")
                        .setContentIntent(pendingIntent)
                        .setColorized(true)
                        .setSmallIcon(R.drawable.ic_noti)
                        .setColor(Color.argb(50, 255, 57, 50))
                        .setSound(null)
                        .setVibrate(null)
                        .setOngoing(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    buil.setPriority(NotificationManager.IMPORTANCE_MIN);
                }
                Notification notification = buil.build();
                startForeground(20, notification);
                //Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            audioManager = (AudioManager) MyTileService.this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(MyTileService.this.getApplicationContext());
                            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                            vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                            try {
                                metroTask = new MetronomeAsyncTask2();
                                metroTask.execute();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "metro");
                            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "metronomeBG7");
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR");
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                        }catch (Exception e)    {
                            e.printStackTrace();
                        }
                    }
                });

                /*****/

                setCurrentState(Tile.STATE_ACTIVE);
                break;
            case Tile.STATE_UNAVAILABLE:
                break;
        }
    }

    private void setCurrentState(int state){
        Tile tile = getQsTile();
        try {
            tile.setState(state);
        }catch (Exception e)    {
            e.printStackTrace();
        }
        switch (state){
            case Tile.STATE_ACTIVE:
                tile.setIcon(tile.getIcon().setTint(Color.WHITE));
                break;
            case Tile.STATE_INACTIVE:
                tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                break;
            case Tile.STATE_UNAVAILABLE:
                tile.setIcon(tile.getIcon().setTint(Color.GRAY));
                break;
        }

        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }
    private class MetronomeAsyncTask2 extends AsyncTask<Void,Void,String> {
        Metronome metronome;
        short bpm = 100;
        Handler handler;
        short noteValue = 10;
        short beats = 30;
        double beatSound = 9440;
        double sound = 9350;

        MetronomeAsyncTask2() {
            handler = getHandler();
            metronome = new Metronome(handler);
        }
        protected String doInBackground(Void... params) {
            try {
                metronome.play();
            }catch (Exception e)    {
                e.printStackTrace();
            }
            return null;
        }
        void stop() {
            metronome.stop();
            metronome = null;
        }
        void setBpm(short bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }
    }
}