package atlow.chemi.mymada;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

public class AudioGeneratorModule extends ReactContextBaseJavaModule {
    private final Map<UUID, AudioTrack> trackMap = new HashMap<>();
    private final Map<UUID, double[]> waveMap = new HashMap<>();
    private final ReactApplicationContext mContext;
    private final int sampleRate = 8000;
    private final AudioManager audioManager;

    AudioGeneratorModule(ReactApplicationContext context) {
        super(context);
        mContext = context;
        audioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public String getName() {
        return "AudioGeneratorModule";
    }

    @ReactMethod
    public void createPlayer(Promise promise) {
        UUID uuid = UUID.randomUUID();
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_ALARM, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, sampleRate, AudioTrack.MODE_STREAM);
        audioTrack.play();
        trackMap.put(uuid, audioTrack);
        promise.resolve(uuid.toString());
    }

    @ReactMethod
    public void destroyAudioTrack(String id, Promise promise) {
        UUID uuid = UUID.fromString(id);
        if (!trackMap.containsKey(uuid)) {
            promise.reject(new IndexOutOfBoundsException("No player with given ID"));
        }
        AudioTrack audioTrack = trackMap.get(uuid);
        trackMap.remove(uuid);
        assert audioTrack != null;
        audioTrack.stop();
        audioTrack.release();
        promise.resolve(null);
    }

    @ReactMethod
    public void writeSound(String trackId, String waveId, Promise promise) {
        UUID trackUUID = UUID.fromString(trackId);
        UUID waveUUID = UUID.fromString(waveId);
        if (!trackMap.containsKey(trackUUID)) {
            promise.reject(new IndexOutOfBoundsException("No player with given ID"));
        }
        if (!waveMap.containsKey(waveUUID)) {
            promise.reject(new IndexOutOfBoundsException("No wave with given ID"));
        }
        AudioTrack audioTrack = trackMap.get(trackUUID);
        double[] samples = waveMap.get(waveUUID);
        assert samples != null;
        assert audioTrack != null;
        byte[] generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        promise.resolve(null);
    }

    @ReactMethod
    public void destroyWave(String id) {
        UUID uuid = UUID.fromString(id);
        trackMap.remove(uuid);
    }

    @ReactMethod
    public void getSineWave(int samples, double frequencyOfTone, Promise promise){
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
        }
        UUID uuid = UUID.randomUUID();
        waveMap.put(uuid, sample);
        promise.resolve(uuid.toString());
    }

    @ReactMethod
    public void maximizeVolume(Promise promise) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        setVolume((int)(maxVolume / 2), null);
        promise.resolve(currentVolume);
    }

    @ReactMethod
    public void setVolume(int volume, @Nullable Promise promise) {
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0);
        if (promise != null) {
            promise.resolve(null);
        }
    }

    private byte[] get16BitPcm(double[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples) {
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }
}
