package atlow.chemi.mymada.classes;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioGenerator {

    private final int sampleRate;
    private AudioTrack audioTrack;

    public AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }
    public double[] getSineWave(int samples,int sampleRate,double frequencyOfTone){
        var sample = new double[samples];
        for (var i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
        }
        return sample;
    }
    private byte[] get16BitPcm(double[] samples) {
        var generatedSound = new byte[2 * samples.length];
        var index = 0;
        for (var sample : samples) {
            // scale to maximum amplitude
            var maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }
    public void createPlayer(){
        audioTrack = new AudioTrack(AudioManager.STREAM_ALARM, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, sampleRate, AudioTrack.MODE_STREAM);
        audioTrack.play();
    }
    public void writeSound(double[] samples) {
        var generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }
    public void destroyAudioTrack() {
        audioTrack.stop();
        audioTrack.release();
    }
}