package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import atlow.chemi.mymada.recieversAndServices.MetroService;
import atlow.chemi.mymada.R;

public class MetroActivity extends Fragment {
    private static Animation anim, big;
    public static boolean MetroRun = false;
    @SuppressLint("StaticFieldLeak")
    private static View v;
    public static long timestamp = 0;
    @SuppressLint("StaticFieldLeak")
    private static TextView clock;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar pb;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static StopWatch stopWatch = new StopWatch();
    private static final int START_TIMER = 0, STOP_TIMER = 1;
    private static Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            final int UPDATE_TIMER = 2, REFRESH_RATE = 500;
            switch (msg.what) {
                case START_TIMER:
                    stopWatch.start();
                    mHandler.sendEmptyMessage(UPDATE_TIMER);
                    break;
                case STOP_TIMER:
                    mHandler.removeMessages(UPDATE_TIMER);
                    stopWatch.stop();
                    try {
                        clock.setText(stopWatch.toString());
                        pb.setProgress(0);
                    }catch (Exception e) {
                        Crashlytics.log("Text box does not exist");
                        Crashlytics.logException(e);
                    }
                    break;
                case UPDATE_TIMER:
                    try {
                        String timeToDisplay = stopWatch.toString();
                        if (stopWatch.toString().contains("\n")) {
                            int start = timeToDisplay.indexOf("\n");
                            SpannableString string = new SpannableString(timeToDisplay);
                            string.setSpan(new RelativeSizeSpan(0.6f), start, timeToDisplay.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            clock.setText(string);
                        } else
                            clock.setText(stopWatch.toString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            pb.setProgress(stopWatch.getPercentOfTwoMinute(), true);
                        else
                            pb.setProgress(stopWatch.getPercentOfTwoMinute());
                    }catch (Exception e) {
                        Crashlytics.log("Text box does not exist");
                        Crashlytics.logException(e);
                    }
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIMER, REFRESH_RATE);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
            getActivity().setTitle(R.string.button_d);
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        return inflater.inflate(R.layout.activity_metro, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean start = false;
        boolean running = false;
        Typeface myTypeface;
        if (getActivity() != null) {
            start = getActivity().getIntent().getBooleanExtra("start", false);
            running = getActivity().getIntent().getBooleanExtra("running", false);
            myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
            final TextView f = view.findViewById(R.id.metroExp);
            f.setTypeface(myTypeface);
        }
        ConstraintLayout bp = view.findViewById(R.id.bpmW);
        clock = view.findViewById(R.id.metro_Clock);
        pb = view.findViewById(R.id.metro_progressBar);
        v = view.findViewById(R.id.startstop);
        if (running || MetroRun) {
            clocketise(START_TIMER);
            startAnim();
        }
        bp.setOnClickListener(view1 -> {
            if(!MetroRun) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "metro");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "metronome");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                MetroRun = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
                    if (manager != null && manager.getNotificationChannel("runInBg") == null) {
                        NotificationChannelGroup group2 = new NotificationChannelGroup("grpOthers", "התראות שונות");
                        NotificationChannel serviceChannel = new NotificationChannel("runInBg", "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
                        serviceChannel.enableVibration(false);
                        serviceChannel.enableLights(false);
                        serviceChannel.setShowBadge(false);
                        serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
                        serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
                        serviceChannel.setGroup("grpOthers");
                        manager.createNotificationChannelGroup(group2);
                        manager.createNotificationChannel(serviceChannel);
                    }
                }
                Intent serviceIntent = new Intent(getActivity(), MetroService.class);
                try {
                    ContextCompat.startForegroundService(getActivity(), serviceIntent);
                }
                catch (Exception e) {
                    Crashlytics.log("failed starting metronome");
                    Crashlytics.logException(e);
                }
                clocketise(START_TIMER);
                startAnim();
            }
            else {
                clocketise(STOP_TIMER);
                MetroRun = false;
                Intent serviceIntent = new Intent(getActivity(), MetroService.class);
                getActivity().stopService(serviceIntent);
                stopAnim();
            }
        });
        if (start)
            bp.performClick();
    }
    public static void clocketise(int what) {
        if (timestamp == 0 || System.currentTimeMillis() < timestamp)
            timestamp = System.currentTimeMillis();
        stopWatch.setStartTime(timestamp);
        mHandler.sendEmptyMessage(what);
    }
    public static void startAnim() {
        if (v != null) {
            try {
                scaleView(v);
                anim.setDuration(650);
                big.setDuration(350);
            } catch (Exception e) {
                Crashlytics.log(e.getMessage());
            }
        }
    }
    public static void stopAnim() {
        if (v != null && anim != null && big != null) {
            try {
                v.getAnimation().cancel();
                v.clearAnimation();
                anim.setAnimationListener(null);
                big.setAnimationListener(null);
                v.setAnimation(null);
            } catch (Exception e) {
                Crashlytics.log(e.getMessage());
            }
        }
    }
    private static void scaleView(final View v) {
        anim = new ScaleAnimation((float) 1.05, (float) 1.0, (float) 1.05, (float) 1.0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        big = new ScaleAnimation((float) 1.0, (float) 1.05, (float) 1.0, (float) 1.05, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(650);
        big.setDuration(350);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(big);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        big.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                v.startAnimation(anim);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        v.startAnimation(anim);
    }
}
class StopWatch {
    private long startTime = 0;
    private boolean isOn = false;
    void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public void start() {
        if (this.startTime == 0)
            this.startTime = System.currentTimeMillis();
        this.isOn = true;
    }
    public void stop() {
        this.startTime = 0;
        this.isOn = false;
    }
    int getPercentOfTwoMinute() {
        return (int)(((System.currentTimeMillis() - this.startTime) / 1000) % 120);
    }
    private String getElapsedTimeSeconds() {
        long elapsed = 0;
        if (isOn)
            elapsed = ((System.currentTimeMillis() - this.startTime) / 1000) % 60;
        return elapsed <= 9 ? "0" + elapsed : elapsed + "";
    }
    private String getElapsedTimeMinutes() {
        long elapsed = 0;
        if (isOn)
            elapsed = (((System.currentTimeMillis() - this.startTime) / 1000) / 60) % 60;
        return elapsed <= 9 ? "0" + elapsed : elapsed + "";
    }
    private String getElapsedTimeHours() {
        long elapsed = 0;
        if (isOn)
            elapsed = ((((System.currentTimeMillis() - this.startTime) / 1000) / 60) / 60);
        return elapsed <= 9 ? "0" + elapsed : elapsed + "";
    }
    @NonNull
    @Override
    public String toString() {
        if (getElapsedTimeHours().equals("00"))
            return getElapsedTimeMinutes() + ":" + getElapsedTimeSeconds();
        return getElapsedTimeHours() + "\n" + getElapsedTimeMinutes() + ":" + getElapsedTimeSeconds();
    }
}