package atlow.chemi.mymada.recieversAndServices;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import javax.annotation.Nullable;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.UtillitiesAndTools;
import atlow.chemi.mymada.classes.Metronome;
import atlow.chemi.mymada.helpers.Event;
import atlow.chemi.mymada.helpers.MetronomeEventCallback;

public class MetroService extends Service {
    public Event<MetronomeEventCallback> MetronomeStartEvent = new Event<>();
    public Event<MetronomeEventCallback> MetronomeStopEvent = new Event<>();
    public long timestamp;
    private AudioManager audioManager;
    private int currentVolume;
    public static float vo = 0;
    public static boolean running = false;
    private MetronomeAsyncTask metroTask;
    private IBinder serviceBinder = new MetroBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent callSNI = new Intent(MetroService.this, UtillitiesAndTools.class);
        callSNI.putExtra("running", true);
        callSNI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, callSNI, PendingIntent.FLAG_IMMUTABLE);
        Intent btn = new Intent(this, ActionReceiver.class);
        btn.putExtra("action", "stopM");
        PendingIntent pBtn = PendingIntent.getBroadcast(MetroService.this, 9, btn, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String action = MetroService.this.getResources().getString(R.string.MetroStop);

//        Crashlytics.log("Starting Metro Notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "runInBg")
                .setContentTitle(MetroService.this.getResources().getString(R.string.MetroServiceTtl))
                .setContentText(MetroService.this.getResources().getString(R.string.MetroServiceTxt))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_metronome_tile)
                .setShowWhen(false)
                .setColor(Color.argb(50, 255, 57, 50))
                .addAction(R.drawable.ic_close, action, pBtn)
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManagerCompat.IMPORTANCE_MIN);
        }
        Notification notification = builder.build();
//        Crashlytics.log("built Metro Notification.");
        startForeground(13, notification);
//        Crashlytics.log("started Metro service foreground.");

        timestamp = System.currentTimeMillis();
        for(MetronomeEventCallback listener : MetronomeStartEvent.listeners())
            listener.metronomeEventHandler(timestamp);


        metro(MetroService.this);
        running = true;
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public class MetroBinder extends Binder {
        public MetroService getService() {
            return MetroService.this;
        }
    }

    @Override
    public void onDestroy() {
        metroStop();
        super.onDestroy();
    }

    private void metro(Context context) {
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager != null) {
            int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
        }
        try {
            metroTask = new MetronomeAsyncTask();
            metroTask.execute();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void metroStop() {
        for(MetronomeEventCallback listener : MetronomeStopEvent.listeners())
            listener.metronomeEventHandler(0);
        running = false;
        try {
            metroTask.stop();
        }
        catch(NullPointerException e) {
//            Crashlytics.log("failed stopping metronome");
//            Crashlytics.logException(e);
        }
        if(audioManager != null)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
    }

    private static class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {
        Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
        }

        protected String doInBackground(Void... params) {
            try {
                metronome.play();
            } catch(Exception e) {
//                Crashlytics.logException(e);
                e.printStackTrace();
            }
            return null;
        }

        void stop() {
            metronome.stop();
            metronome = null;
        }
    }
}
