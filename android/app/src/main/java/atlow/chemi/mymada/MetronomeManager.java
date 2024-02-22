package atlow.chemi.mymada;

import static androidx.core.content.ContextCompat.RECEIVER_EXPORTED;
import static androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED;
import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_START;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.METRONOME_NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.METRONOME_START_TIME;
import static atlow.chemi.mymada.Constants.METRONOME_STATE;
import static atlow.chemi.mymada.Constants.METRONOME_TOGGLED;
import static atlow.chemi.mymada.Constants.NOTIFICATION_CONFIG;
import static atlow.chemi.mymada.Constants.RUN_IN_BG_NOTIFICATION_CHANNEL;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;

import atlow.chemi.mymada.classes.AudioGenerator;
import atlow.chemi.mymada.classes.JSException;
import atlow.chemi.mymada.helpers.EventListener;
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

    private void startBackgroundService(Context context, ReadableMap notificationConfig) throws JSException {
        if (context == null) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: context is missing");
        }
        if (notificationConfig == null) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: Notification config is invalid");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationConfig.hasKey("channelId")) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: channelId is required");
        }
        if (!notificationConfig.hasKey("id")) {
            throw new JSException(ERROR_INVALID_CONFIG , "MetronomeManager: id is required");
        }
        if (!notificationConfig.hasKey("icon")) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: icon is required");
        }
        if (!notificationConfig.hasKey("title")) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: title is required");
        }
        if (!notificationConfig.hasKey("text")) {
            throw new JSException(ERROR_INVALID_CONFIG, "MetronomeManager: text is required");
        }

        var intent = new Intent(context, ForegroundService.class);
        intent.setAction(ACTION_FOREGROUND_SERVICE_START);
        intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
        ComponentName componentName = null;
        try {
            ContextCompat.startForegroundService(context, intent);
        } catch (IllegalStateException e) {
            throw new JSException(ERROR_SERVICE_ERROR, "MetronomeManager: Foreground service is not started");
        }
        var filter = new IntentFilter();
        filter.addAction(METRONOME_NOTIFICATION_BUTTON_PRESSED);
        ContextCompat.registerReceiver(context, buttonPressReceiver, filter, RECEIVER_EXPORTED);
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
                        // Crashlytics
                    }
                    if (++currentBeat > beat) {
                        currentBeat = 0;
                    }
                }
            }).thenRun(cleanup);
        } catch (Exception e) {
            cleanup.run();
            // Crashlytics
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
            startBackgroundService(context, notificationConfig);
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
        var intent = new Intent(context, ForegroundService.class);
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

    public void toggleMetronome(Context context) {
        try {
            if (isActive()) {
                stopMetronome(context, null);
                return;
            }
            var notificationConfig = Arguments.createMap();
            notificationConfig.putInt("id", 13);
            notificationConfig.putString("color", "red");
            notificationConfig.putString("icon", "ic_metronome_tile");
            notificationConfig.putInt("priority", Notification.PRIORITY_MIN);
            notificationConfig.putString("channelId", RUN_IN_BG_NOTIFICATION_CHANNEL);
            notificationConfig.putString("text", context.getString(R.string.MetroServiceTxt));
            notificationConfig.putString("button", context.getString(R.string.MetroStop));
            notificationConfig.putString("title", context.getString(R.string.MetroServiceTtl));
            notificationConfig.putDouble("startTime", System.currentTimeMillis());
            notificationConfig.putBoolean("navigateToTools", true);
            notificationConfig.putBoolean("isOnGoing", true);
            notificationConfig.putString("target", "MetronomeActivity");

            startMetronome(context, notificationConfig, null);
        } catch (Exception e) {
            Log.e("MetronomeManager", "Toggle poop", e);
            // Crashlytics
        }
    }
}
