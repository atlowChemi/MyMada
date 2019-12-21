package atlow.chemi.mymada.classes;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

public class Metronome {
    private boolean play = true;
    private static AudioGenerator audioGenerator = new AudioGenerator(8000);
    private static double[] soundTickArray = audioGenerator.getSineWave(1000, 8000, 9440);
    private static double[] soundTockArray = audioGenerator.getSineWave(1000, 8000, 9350);
    private static double[] silenceSoundArray = new double[3800];
    private int currentBeat = 1;

    public Metronome() {
        audioGenerator.createPlayer();
    }
    public void play() {
        int beat = 30;
        do {
            Log.e("Metronome", ""+currentBeat);
            if(currentBeat == 1)
                try {
                    audioGenerator.writeSound(soundTockArray);
                }catch (Exception e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            else
                try {
                    audioGenerator.writeSound(soundTickArray);
                }catch (Exception e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                }
            try {
                audioGenerator.writeSound(silenceSoundArray);
            }catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
            currentBeat++;
            if(currentBeat > beat)
                currentBeat = 1;
        } while(play);
    }
    public void stop() {
        play = false;
        audioGenerator.destroyAudioTrack();
    }
}