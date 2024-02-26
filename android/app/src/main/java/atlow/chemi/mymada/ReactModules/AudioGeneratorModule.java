package atlow.chemi.mymada.ReactModules;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

@ReactModule(name = AudioGeneratorModule.NAME)
public class AudioGeneratorModule extends ReactContextBaseJavaModule {
    public static final String NAME = "AudioGeneratorModule";
    private final Map<UUID, AudioTrack> trackMap = new HashMap<>();
    private final Map<UUID, double[]> waveMap = new HashMap<>();
    private final int sampleRate = 8000;
    private final AudioManager audioManager;

    public AudioGeneratorModule(ReactApplicationContext context) {
        super(context);
        audioManager = (AudioManager) context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public String createPlayer(@Nullable Promise promise) {
        var uuid = UUID.randomUUID();
        var audioTrack = new AudioTrack(AudioManager.STREAM_ALARM, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, sampleRate, AudioTrack.MODE_STREAM);
        audioTrack.play();
        trackMap.put(uuid, audioTrack);
        var uuidString = uuid.toString();
        if (promise != null) {
            promise.resolve(uuidString);
        }
        return uuidString;
    }

    @ReactMethod
    public void destroyAudioTrack(String id, @Nullable Promise promise) {
        var uuid = UUID.fromString(id);
        if (!trackMap.containsKey(uuid)) {
            var err = new IndexOutOfBoundsException("No player with given ID");
            if (promise != null) {
                promise.reject(err);
                return;
            }
            throw err;
        }
        var audioTrack = trackMap.get(uuid);
        trackMap.remove(uuid);
        if (audioTrack == null) {
            if (promise != null) {
                promise.reject(new Exception("Invalid audioTrack"));
            }
            return;
        }
        audioTrack.stop();
        audioTrack.release();
        if (promise != null) {
            promise.resolve(null);
        }
    }

    @ReactMethod
    public void writeSound(String trackId, String waveId, @Nullable Promise promise) throws Exception {
        var trackUUID = UUID.fromString(trackId);
        var waveUUID = UUID.fromString(waveId);
        if (!trackMap.containsKey(trackUUID)) {
            var err = new IndexOutOfBoundsException("No player with given ID");
            if (promise != null) {
                promise.reject(err);
                return;
            }
            throw err;
        }
        if (!waveMap.containsKey(waveUUID)) {
            var err = new IndexOutOfBoundsException("No wave with given ID");
            if (promise != null) {
                promise.reject(err);
                return;
            }
            throw err;
        }
        var audioTrack = trackMap.get(trackUUID);
        double[] samples = waveMap.get(waveUUID);
        if (samples == null || audioTrack == null) {
            var err = new Exception("Invalid sample or audioTrack");
            if (promise != null) {
                promise.reject(err);
                return;
            }
            throw err;
        }
        var generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        if (promise != null) {
            promise.resolve(null);
        }
    }

    @ReactMethod
    public void destroyWave(String id) {
        var uuid = UUID.fromString(id);
        trackMap.remove(uuid);
    }

    @ReactMethod
    public String getEmptySineWave(int samples, @Nullable Promise promise){
        var sample = new double[samples];
        var uuid = UUID.randomUUID();
        waveMap.put(uuid, sample);
        var uuidString = uuid.toString();
        if (promise != null) {
            promise.resolve(uuidString);
        }
        return uuidString;
    }
    @ReactMethod
    public String getSineWave(int samples, double frequencyOfTone, @Nullable Promise promise){
        var sample = new double[samples];
        for (var i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
        }
        var uuid = UUID.randomUUID();
        waveMap.put(uuid, sample);
        var uuidString = uuid.toString();
        if (promise != null) {
            promise.resolve(uuidString);
        }
        return uuidString;
    }

    @ReactMethod
    public int maximizeVolume(@Nullable Promise promise) {
        var maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        var currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        setVolume(maxVolume, null);
        if (promise != null) {
            promise.resolve(currentVolume);
        }
        return currentVolume;
    }

    @ReactMethod
    public void setVolume(int volume, @Nullable Promise promise) {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
        if (promise != null) {
            promise.resolve(null);
        }
    }

    @ReactMethod
    public void getMaxVolumeForAlarmStream(Promise promise) {
        var maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        promise.resolve(maxVolume);
    }

    private byte[] get16BitPcm(double[] samples) {
        var generatedSound = new byte[2 * samples.length];
        var index = 0;
        for (var sample : samples) {
            // scale to maximum amplitude
            var maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wave PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }
}
