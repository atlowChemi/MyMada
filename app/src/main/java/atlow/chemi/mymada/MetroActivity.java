package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MetroActivity extends Fragment {
    private static Animation anim;
    private static Animation big;
    static boolean MetroRun = false;
    @SuppressLint("StaticFieldLeak")
    private static View v;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_metro, container, false);
        if (SettingsDesigned.isRTL())
            v.setRotationY(180);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean start = getActivity().getIntent().getBooleanExtra("start", false);
        boolean running = getActivity().getIntent().getBooleanExtra("running", false);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        final TextView f = view.findViewById(R.id.metroExp);
        f.setTypeface(myTypeface);
        RelativeLayout bp = view.findViewById(R.id.bpmW);
        v = view.findViewById(R.id.startstop);
        if (running || MetroRun) {
            scaleView(v, 1f, .95f, 1f, .95f);
            anim.setDuration(230);
            big.setDuration(230);
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "metro");
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "metronome");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        bp.setOnClickListener(view1 -> {
            if(!MetroRun) {
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
                    Intent serviceIntent = new Intent(getActivity(), MetroService.class);
                    try {
                        ContextCompat.startForegroundService(getActivity(), serviceIntent);
                    } catch (Exception e) {
                        Crashlytics.log("failed starting metronome tile");
                        Crashlytics.logException(e);
                    }
                }
                scaleView(v, 1f, .95f, 1f, .95f);
                anim.setDuration(230);
                big.setDuration(230);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "metro");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "metronome");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CPR");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            } else {
                MetroRun = false;
                Intent serviceIntent = new Intent(getActivity(), MetroService.class);
                getActivity().stopService(serviceIntent);
                try {
                    v.getAnimation().cancel();
                    v.clearAnimation();
                    anim.setAnimationListener(null);
                    big.setAnimationListener(null);
                    v.setAnimation(null);
                }catch (Exception e) {
                    Crashlytics.log(e.getMessage());
                }
            }
        });
        if (start)
            bp.performClick();
    }
    static void startAnim() {
        try {
            scaleView(v, 1f, .95f, 1f, .95f);
            anim.setDuration(230);
            big.setDuration(230);
        }catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
    }
    static void stopAnim() {
        try {
            v.getAnimation().cancel();
            v.clearAnimation();
            anim.setAnimationListener(null);
            big.setAnimationListener(null);
            v.setAnimation(null);
        }catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
    }
    public static void scaleView(final View v, float startScale, float endScale, float XstartScale, float XendScale) {
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
}