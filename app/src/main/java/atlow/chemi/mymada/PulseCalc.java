package atlow.chemi.mymada;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

public class PulseCalc extends Fragment {
    private boolean running = false;
    private EditText pulse, fullPulse;
    private TextInputLayout pulseTIL;
    private int currentVolume;
    public static float vo = 0;
    public PulseCalc()  {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_pulse_calc, container, false);
        if (SettingsDesigned.isRTL())
            v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pulse = view.findViewById(R.id.fifteenPulseA);
        fullPulse = view.findViewById(R.id.fullPulseA);
        pulseTIL = view.findViewById(R.id.shortPulseATIL);
        TextInputLayout fullPulseTIL =view.findViewById(R.id.fullPulseATIL);
        fullPulse.setInputType(InputType.TYPE_NULL);
        pulse.addTextChangedListener(new MyTextWatcher(pulse));
        final TextView seconds = view.findViewById(R.id.txtTimerSecond);
        TextView one = view.findViewById(R.id.txt_TimerSecond), two = view.findViewById(R.id.txt_nothing);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        pulseTIL.setTypeface(myTypeface);
        fullPulseTIL.setTypeface(myTypeface);
        fullPulse.setTypeface(myTypeface);
        pulse.setTypeface(myTypeface);
        seconds.setTypeface(myTypeface);
        one.setTypeface(myTypeface);
        two.setTypeface(myTypeface);
            final CountDownTimer aCounter = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished)    {
                String t = (millisUntilFinished/1000) + "";
                seconds.setText(t);
            }
            public void onFinish()  {
                try {
                    seconds.setText("0");
                    AudioGenerator audio = new AudioGenerator(8000);
                    double[] silence = audio.getSineWave(3000, 8000, 0);
                    double[] doNote = audio.getSineWave(3000, 8000, 9000.25);
                    final AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
                    int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (vol / 2), 0);
                    audio.createPlayer();
                    audio.writeSound(doNote);
                    audio.writeSound(silence);
                    /*audio.writeSound(doNote);
                    audio.writeSound(silence);
                    audio.writeSound(doNote);
                    audio.writeSound(silence);*/
                    audio.destroyAudioTrack();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                    running = false;
                }catch (Exception e)    {
                    e.printStackTrace();
                }
            }
        };
        RelativeLayout rl = view.findViewById(R.id.clickableTimer);
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    aCounter.start();
                    running = true;
                }
                else    {
                    aCounter.cancel();
                    seconds.setText("15");
                    running = false;
                }
            }
        });
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.fifteenPulseA:
                    if (pulse.getText().toString().trim().isEmpty() || !pulse.getText().toString().trim().matches("[0-9]+"))
                        pulseTIL.setError(getString(R.string.err_pulse));
                    else {
                        pulseTIL.setErrorEnabled(false);
                        String shortPulse = pulse.getText().toString().trim();
                        int pulse2c = Integer.parseInt(shortPulse.replaceAll("[\\D]",""));
                        try {
                            String toShow = "" + pulse2c*4;
                            fullPulse.setText(toShow);
                        } catch (IndexOutOfBoundsException e) {
                            Crashlytics.log("error with pulse calculation");
                            Crashlytics.logException(e);
                        }
                    }
                    break;
            }
        }
    }
}