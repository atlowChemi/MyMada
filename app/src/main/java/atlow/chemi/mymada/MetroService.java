package atlow.chemi.mymada;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.animation.Animation;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import static atlow.chemi.mymada.Applicatio.CnlBg;
import static atlow.chemi.mymada.MetroActivity.MetroRun;
import static atlow.chemi.mymada.MetroActivity.startAnim;
import static atlow.chemi.mymada.MetroActivity.stopAnim;

public class MetroService extends Service {
    private AudioManager audioManager;
    private int currentVolume;
    public static float vo = 0;
    private MetronomeAsyncTask metroTask;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent callSNI = new Intent(MetroService.this, UtillTools.class);
        callSNI.putExtra("running", true);
        callSNI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 7, callSNI, 0);
        Intent btn = new Intent(this, ActionReciver.class);
        PendingIntent pBtn = PendingIntent.getBroadcast(MetroService.this, 9, btn, PendingIntent.FLAG_UPDATE_CURRENT);
        String action = MetroService.this.getResources().getString(R.string.MetroStop);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CnlBg)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.MetroServiceTtl))
                .setContentText(getApplicationContext().getResources().getString(R.string.MetroServiceTxt))
                .setContentIntent(pendingIntent)
                .setColorized(true)
                .setSmallIcon(R.drawable.ic_noti)
                .setShowWhen(false)
                .setColor(Color.argb(50, 255, 57, 50))
                .addAction(R.drawable.ic_close, action, pBtn)
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPriority(NotificationManager.IMPORTANCE_MIN);
        }
        Notification notification = builder.build();
        startForeground(13, notification);
        metro(MetroService.this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy()  {
        metroStop();
        super.onDestroy();
    }

    private void metro(Context context) {
        startAnim();
        MetroRun = true;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
        }
        try {
            metroTask = new MetronomeAsyncTask();
            metroTask.execute();
        }catch (Exception e)    {
            e.printStackTrace();
        }

    }
    private void metroStop() {
        stopAnim();
        MetroRun = false;
        metroTask.stop();
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
    }
    private static class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
        }
        protected String doInBackground(Void... params) {
            //metronome.setBeat(beats);
            //metronome.setNoteValue(noteValue);
            //metronome.setBpm(bpm);
            //metronome.setBeatSound(beatSound);
            //metronome.setSound(sound);
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
