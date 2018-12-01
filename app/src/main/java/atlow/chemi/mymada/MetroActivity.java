package atlow.chemi.mymada;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MetroActivity extends Fragment {
    private TextView startOrStop;
    private AudioManager audioManager;
    private Animation anim, big;
    private int currentVolume;
    public static float vo = 0;
    private MetronomeAsyncTask metroTask;
    private String a;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String message = (String)msg.obj;
                if(message.equals("1"))
                    a = message;
                else
                    a = message;
                //currentBeat.setText(message);
            }
        };
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_metro, container, false);
        v.setRotationY(180);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean start = getActivity().getIntent().getBooleanExtra("start", false);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        TextView f = (TextView) view.findViewById(R.id.metroExp);
        f.setTypeface(myTypeface);
        startOrStop = (TextView) view.findViewById(R.id.startstopT);
        RelativeLayout bp = (RelativeLayout) view.findViewById(R.id.bpmW);
        bp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = startOrStop.getText().toString();
                View v = view.findViewById(R.id.startstop);
                if(txt.equalsIgnoreCase("הפעלה")) {
                    int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
                    startOrStop.setText(R.string.stop);
                    try {
                        metroTask = new MetronomeAsyncTask();
                        metroTask.execute();
                    }catch (Exception e)    {
                        e.printStackTrace();
                    }
                    scaleView(v, 1f, .95f, 1f, .95f);
                    anim.setDuration(230);
                    big.setDuration(230);
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "metro");
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "metronome");
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                }
                else {
                    startOrStop.setText(R.string.start);
                    metroTask.stop();
                    v.getAnimation().cancel();
                    v.clearAnimation();
                    anim.setAnimationListener(null);
                    big.setAnimationListener(null);
                    v.setAnimation(null);
                    metroTask = new MetronomeAsyncTask();
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                }
            }
        });
        if (start)
            bp.performClick();
    }
    public void scaleView(final View v, float startScale, float endScale, float XstartScale, float XendScale) {
        anim = new ScaleAnimation(XstartScale, XendScale, startScale, endScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        big = new ScaleAnimation(XendScale, XstartScale, endScale, startScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(250);
        big.setDuration(250);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(big);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        big.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(anim);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        v.startAnimation(anim);
    }

    @Override
    public void onDestroy()  {
        super.onDestroy();
        if (metroTask != null)
            metroTask.stop();
        metroTask = null;
    }
    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        Metronome metronome;
        short bpm = 100;
        Handler handler;
        short noteValue = 10;
        short beats = 30;
        double beatSound = 9440;
        double sound = 9350;

        MetronomeAsyncTask() {
            handler = getHandler();
            metronome = new Metronome(handler);
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