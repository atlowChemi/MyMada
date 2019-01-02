package atlow.chemi.mymada;

import android.os.Message;
import android.util.Log;

class Metronome {

    private double bpm = 100;
    private int beat = 30;
    private int noteValue = 10;
    private int silence;
    private double beatSound = 9440;
    private double sound = 9350;
    private final int tick = 1000; // samples of tick
    private boolean play = true;
    private AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray;
    private double[] soundTockArray;
    private double[] silenceSoundArray;
    private Message msg;
    private int currentBeat = 1;

    Metronome() {
        audioGenerator.createPlayer();
    }
    void calcSilence() {
        silence = (int)(((60/bpm)*8000)-tick);
        soundTickArray = new double[this.tick];
        soundTockArray = new double[this.tick];
        silenceSoundArray = new double[this.silence];

        Log.e("Silence Met", ""+currentBeat);

        double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
        double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
        for(int i=0;i<this.tick;i++) {
            soundTickArray[i] = tick[i];
            soundTockArray[i] = tock[i];
        }
        for(int i=0;i<silence;i++)
            silenceSoundArray[i] = 0;
    }
    void play() {
        calcSilence();
        do {
            Log.e("Metronome", ""+currentBeat);
            if(currentBeat == 1)
                try {
                    audioGenerator.writeSound(soundTockArray);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            else
                try {
                    audioGenerator.writeSound(soundTickArray);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            try {
                audioGenerator.writeSound(silenceSoundArray);
            }catch (Exception e) {
                e.printStackTrace();
            }
            currentBeat++;
            if(currentBeat > beat)
                currentBeat = 1;
        } while(play);
    }
    void stop() {
        play = false;
        audioGenerator.destroyAudioTrack();
    }
    public double getBpm() {
        return bpm;
    }
    void setBpm(int bpm) {
        this.bpm = bpm;
    }
    public int getNoteValue() {
        return noteValue;
    }
    void setNoteValue(int bpmetre) {
        this.noteValue = bpmetre;
    }
    public int getBeat() {
        return beat;
    }
    void setBeat(int beat) {
        this.beat = beat;
    }
    public double getBeatSound() {
        return beatSound;
    }
    void setBeatSound(double sound1) {
        this.beatSound = sound1;
    }
    public double getSound() {
        return sound;
    }
    void setSound(double sound2) {
        this.sound = sound2;
    }
}