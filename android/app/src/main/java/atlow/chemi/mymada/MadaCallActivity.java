package atlow.chemi.mymada;

import static atlow.chemi.mymada.Constants.VIBRATION_PATTERN;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowManager;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import java.io.IOException;
import java.util.Map;

import atlow.chemi.mymada.classes.ActivityWithPropsDelegate;
import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;

public class MadaCallActivity extends ReactActivity implements DefaultHardwareBackBtnHandler {
    private final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private int currentVolume;
    private AudioManager audioManager;
    @Override
    protected String getMainComponentName() {
        return "MadaCallPage";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        final var extras = getIntent().getExtras();

        if (extras == null) {
            return;
        }

        startVibrations(extras.getBoolean("vibrate", false), extras.getBoolean("infiniteAlert", false));
        playSound(
                extras.getBoolean("sound", true),
                extras.getBoolean("overrideVolume", false),
                extras.getInt("volume", audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)),
                Uri.parse(extras.getString("uri", "android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.alert)),
                extras.getBoolean("infiniteAlert", false)
        );
        if (!extras.getBoolean("autoStopAlert", false))    {
            return;
        }
        final var finishTime = Integer.parseInt(extras.getString("autoStopAlertDuration", "6"));
        final var handler = new Handler();
        handler.postDelayed(() -> {
            mediaCleanup();
            finish();
        }, finishTime * 1000L);

    }

    @Override
    public void onDestroy() {
        mediaCleanup();
        super.onDestroy();
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ActivityWithPropsDelegate(this, getMainComponentName(), DefaultNewArchitectureEntryPoint.getFabricEnabled());
    }

    private void startVibrations(boolean vibrate, boolean infiniteAlert) {
        if (!vibrate) {
            return;
        }
        var repeat = infiniteAlert ? 0 : -1;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            var effect = VibrationEffect.createWaveform(VIBRATION_PATTERN, repeat);
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(VIBRATION_PATTERN, repeat);
        }
    }

    private void playSound(boolean sound, boolean overrideVolume, int overrideVolumeValue, Uri uri, boolean infiniteAlert) {
        if (!sound) {
            return;
        }
        ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: attempt playing sound");
        try {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            if (overrideVolume) {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, overrideVolumeValue, 0);
                mediaPlayer.setVolume(overrideVolumeValue, overrideVolumeValue);
            }
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if (infiniteAlert) {
                mediaPlayer.setLooping(true);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException | RuntimeException e) {
            ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: an exception while playing sound");
            ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
        }
    }

    private void mediaCleanup() {
        if (vibrator != null) vibrator.cancel();
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                mediaPlayer.setVolume(currentVolume, currentVolume);
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                ReactNativeFirebaseCrashlyticsNativeHelper.log("FloatingWidgetService: Failed to stop media player for");
                ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
            }
        }
    }
}