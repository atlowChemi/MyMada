package atlow.chemi.mymada;

import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.METRONOME_NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.METRONOME_START_TIME;
import static atlow.chemi.mymada.Constants.METRONOME_STATE;
import static atlow.chemi.mymada.Constants.METRONOME_TOGGLED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;

import atlow.chemi.mymada.classes.AudioGenerator;
import atlow.chemi.mymada.classes.JSException;
import atlow.chemi.mymada.helpers.EventListener;
import atlow.chemi.mymada.helpers.NotificationHelper;
import atlow.chemi.mymada.recieversAndServices.MetronomeForegroundService;
import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;
import java9.util.concurrent.CompletableFuture;

public class MetronomeManager extends EventListener<HashMap<String, Object>> {
    private static MetronomeManager instance = null;
    private String startTime = "0";
    private final int beat = 30;
    private final AudioGenerator audioGenerator = new AudioGenerator(8000);
    private final double[] tickWave = audioGenerator.getSineWave(1000, 8000, 9440);
    private final double[] tockWave = audioGenerator.getSineWave(1000, 8000, 9350);
    private final double[] emptyWave = new double[3800];
    private class ButtonPressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                stopMetronome(context, null);
            } catch (JSException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private final ButtonPressReceiver buttonPressReceiver = new ButtonPressReceiver();

    private MetronomeManager() {}

    public static synchronized MetronomeManager getInstance() {
        if (instance == null) {
            instance = new MetronomeManager();
        }
        return instance;
    }

    private int maximizeVolume(Context context) {
        var audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        var maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0);
        return currentVolume;
    }
    private void playMetronomeTrack(Context context) {
        audioGenerator.createPlayer();
        var originalVolume = maximizeVolume(context);
        Runnable cleanup = () -> {
            audioGenerator.destroyAudioTrack();
            var audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalVolume, 0);
        };
        try {
            CompletableFuture.runAsync(() -> {
                var currentBeat = 0;
                while (isActive()) {
                    var sound = currentBeat == 0 ? tockWave : tickWave;
                    try {
                        audioGenerator.writeSound(sound);
                        audioGenerator.writeSound(emptyWave);
                    } catch (Exception e) {
                        ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                    }
                    if (++currentBeat > beat) {
                        currentBeat = 0;
                    }
                }
            }).thenRun(cleanup);
        } catch (Exception e) {
            cleanup.run();
            ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
        }
    }

    public boolean isActive() {
        return !startTime.equals("0");
    }

    public double getStartTime() {
        return Long.parseLong(startTime);
    }

    public void startMetronome(@NonNull Context context, ReadableMap notificationConfig, @Nullable Promise promise) throws JSException {
        try {
            NotificationHelper.from(context).startForegroundService(new Intent(context, MetronomeForegroundService.class), notificationConfig, buttonPressReceiver, METRONOME_NOTIFICATION_BUTTON_PRESSED);
            var startTimeDouble = notificationConfig.getDouble("startTime");
            startTime = String.valueOf((long)startTimeDouble);
            playMetronomeTrack(context);
            var eventData = Arguments.createMap();
            eventData.putBoolean(METRONOME_STATE, true);
            eventData.putDouble(METRONOME_START_TIME, startTimeDouble);
            emit(METRONOME_TOGGLED, eventData.toHashMap());
            if (promise != null) {
                promise.resolve(startTimeDouble);
            }
        } catch (JSException e) {
            e.rejectPromiseForException(promise);
        }
    }

    public void stopMetronome(@NonNull Context context, @Nullable Promise promise) throws JSException {
        var intent = new Intent(context, MetronomeForegroundService.class);
        intent.setAction(Constants.ACTION_FOREGROUND_SERVICE_STOP);
        var stopped = context.stopService(intent);
        if (stopped) {
            var eventData = Arguments.createMap();
            eventData.putBoolean(METRONOME_STATE, false);
            eventData.putDouble(METRONOME_START_TIME, 0);
            emit(METRONOME_TOGGLED, eventData.toHashMap());
            startTime = "0";
            if (promise != null) {
                promise.resolve(0);
            }
        } else {
            new JSException(ERROR_SERVICE_ERROR, "MetronomeManager: Foreground service failed to stop").rejectPromiseForException(promise);
        }
    }
}
