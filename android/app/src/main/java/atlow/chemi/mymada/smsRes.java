package atlow.chemi.mymada;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class smsRes extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    int currentVolume;
    private String string;
    private static PendingIntent pi;
    private Vibrator vibe;
    private static String Stxt = "מקרה חדש";
    public Context context;
    private AudioManager am;
    public static float vo = 0;
    private float oldX1, oldY1;
    private boolean isMove1 = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
            getWindow().setStatusBarColor(ContextCompat.getColor(smsRes.this, R.color.OldPrimary));
        }catch (OutOfMemoryError e) {
//            Crashlytics.log("error hiding action bar smsRes");
//            Crashlytics.logException(e);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_res);
//        Crashlytics.log("SmsRes Has Been Opened!");
        Bundle extras = getIntent().getExtras();
        int callT = extras != null ? extras.getInt("callT") : 1;
        switch (callT)  {
            case 2:
                string = "T2";
                break;
            case 3:
            case 5:
                string = "T3";
                break;
            case 4:
                string = "T4";
                break;
            case 6:
                string = "T6";
                break;
            case 1:
            default:
                string = "T1";
                break;
        }
        mMediaPlayer = new MediaPlayer();
        var SP = getSharedPreferences("Settings", MODE_PRIVATE);
        final var map = (Map<String, Object>) SP.getAll();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        Uri url = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.alert);
        long[] length = {0, 900L, 200, 900, 200, 900, 200, 900L, 200, 900, 200};
        vibe = (Vibrator) smsRes.this.getSystemService(Context.VIBRATOR_SERVICE);
        boolean cut = map.compute("cut", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
        boolean vib;
        boolean soun;
        boolean app;
        Uri uri;
        if (string.equals("T6") || string.equals("T4"))    {
            vib = map.compute("NvibT4", (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            soun = map.compute("NsouT4", (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            app = map.compute("NappT4", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
            uri = Uri.parse(SP.getString("NuRiT4", ""));
        }
        else if (cut) {
            vib = map.compute("Nvib" + string, (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            soun = map.compute("Nsou" + string, (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            app = map.compute("Napp" + string, (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
            uri = Uri.parse(SP.getString("NuRi" + string, ""));
        }
        else {
            vib = map.compute("Nvib", (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            soun = map.compute("Nsou", (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            app = map.compute("Napp", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
            uri = Uri.parse(SP.getString("NuRi", ""));
        }
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int sleep = Integer.parseInt((String) map.compute("sleep", (key, val) -> String.valueOf(val == null ? "0" : val)));
        int nightStart = Integer.parseInt((String) map.compute("nightStart", (key, val) -> String.valueOf(val == null ? "23" : val)));
        int nightEnd = Integer.parseInt((String) map.compute("nightEnd", (key, val) -> String.valueOf(val == null ? "7" : val)));
        if (hour <= nightEnd || hour >= nightStart) {
            switch (sleep)  {
                case 1:
                case 4:
                case 7:
                    soun = false;
                    break;
                case 2:
                case 5:
                case 8:
                    soun = false;
                    vib = false;
                    break;
            }
        }
        else {
            //Toast.makeText(getApplicationContext(), "day!!", Toast.LENGTH_SHORT).show();
            switch (sleep)  {
                case 3:
                case 4:
                case 5:
                    soun = false;
                    break;
                case 6:
                case 7:
                case 8:
                    soun = false;
                    vib = false;
                    break;
            }
        }
        final boolean sou = soun;
        boolean loop = map.compute("loop", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
        boolean ShabbatM = map.compute("killS", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
        boolean stat = map.compute("stat", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
        boolean statVib = map.compute("statVib", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true");
        boolean silent = true;
        boolean silentVib = true;
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            silent = false;
        if(am != null && am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
            silentVib = false;
        if (!statVib || silentVib) {
            if (vib && !loop) {
                vibe.vibrate(length, -1);
            } else if (vib) {
                vibe.vibrate(length, 0);
            }
        }
        if (!stat || !silent) {
            if (sou) {
                if (app) {
                    playSound(getApplicationContext(), uri, loop);
                } else {
                    playSound(getApplicationContext(), url, loop);
                }
            }
        }
        if(ShabbatM)    {
            int finishTime = Integer.parseInt(SP.getString("timeK", "6")); //6 secs
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                vibe.cancel();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                try {
                    if (sou && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.setVolume(vo, vo);
                        mMediaPlayer.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                smsRes.this.finish();
            }, finishTime * 1000);
        }
        TextView m = findViewById(R.id.msgType);
        TextView a = findViewById(R.id.Address);
        TextView k = findViewById(R.id.KodR);
        TextView t = findViewById(R.id.SMS);
        TextView one = findViewById(R.id.newC), two = findViewById(R.id.name);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        m.setTypeface(myTypeface);
        a.setTypeface(myTypeface);
        k.setTypeface(myTypeface);
        t.setTypeface(myTypeface);
        one.setTypeface(myTypeface);
        two.setTypeface(myTypeface);
        one.setText(string.equals("T4") || string.equals("T6") ? "הודעה חדשה!" : "אירוע חדש!");
        switch (callT)  {
            case 2:
                m.setText("קבוצה");
                break;
            case 3:
            case 5:
                m.setText("משמרת");
                break;
            case 4:
                m.setText("הודעה מנהלתית");
                break;
            case 6:
                m.setText("שירות");
                break;
            case 1:
            default:
                m.setText("כונן");
                break;
        }
        String time;
        String KodH;
        String KodR;
        String mikre;
        String Address;
        if (callT == 5) {
            try {
                Stxt = Stxt.substring(0, Stxt.indexOf("//"));
            } catch (StringIndexOutOfBoundsException e) {
                Log.getStackTraceString(e);
//                Crashlytics.logException(e);
            }
            try {
                    time = getBetweenStrings(Stxt, "בשעה ", " ");
            }catch (Exception e1) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                time = sdf.format(now.getTime());
            }
            String ti = getResources().getString(R.string.time, time);
            t.setText(ti);
            a.setText(Stxt);
            a.setMaxLines(5);
            a.setMovementMethod(new ScrollingMovementMethod());
        }
        else if (!Stxt.contains("אירוע חדש") && !Stxt.contains("הודעת קבוצת התראה צוות") && !Stxt.contains("הודעת צוות"))    {
            try {
                time = getBetweenStrings(Stxt, "", "מונה:");
            } catch (Exception e) {
                try {
                    time = getBetweenStrings(Stxt, "בשעה ", " ");
                }catch (Exception e1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    time = sdf.format(now.getTime());
                }
            }
            String ti = getResources().getString(R.string.time, time);
            t.setText(ti);
            a.setText(Stxt);
            a.setMaxLines(5);
            a.setMovementMethod(new ScrollingMovementMethod());
        }
        else {
            try {
                Address = getBetweenStrings(Stxt, "כתובת: ", " קוד");
            } catch (Exception e) {
                try {
                    Address = getBetweenStrings(Stxt, "Address: ", "Dispatch Code");
                } catch (Exception ew) {
                    Address = "";
                }
            }
            try {
                KodH = getBetweenStrings(Stxt, "קוד הזנקה: ", " קוד רפואי");
            } catch (Exception e) {
                try {
                    KodH = getBetweenStrings(Stxt, "Dispatch Code: ", "Medical Code");
                } catch (Exception ew) {
                    KodH = "";
                }
            }
            try {
                KodR = getEndStrings(Stxt, "קוד רפואי: ");
            } catch (Exception e) {
                try {
                    KodR = getEndStrings(Stxt, "Medical Code: ");
                } catch (Exception ew) {
                    KodR = "";
                }
            }
            try {
                String textTo = Stxt.contains("מונה") ? "מונה:" : "Incident number:";
                time = getBetweenStrings(Stxt, ". ", textTo);
            } catch (Exception e) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                time = sdf.format(now.getTime());
            }
            try {
                String textTo = Stxt.contains("קוד הזנקה") ? "קוד הזנקה:" : "Dispatch Code:";
                mikre = getStartStrings(Stxt, textTo);
            } catch (Exception e) {
                mikre = Stxt;
            }
            SpannableString kodimT = new SpannableString(KodH + "\n" + KodR);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            kodimT.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ColorPrimary)), KodH.length(), kodimT.length(), 0);
            kodimT.setSpan(myTypeface, 0, kodimT.length(), 0);
            kodimT.setSpan(boldSpan, KodH.length(), kodimT.length(), 0);
            k.setText(kodimT);
            String ti = getResources().getString(R.string.time, time);
            t.setText(ti);
            String add = Address.isEmpty() ? mikre : Address;
            a.setText(add);
            a.setMovementMethod(new ScrollingMovementMethod());
        }

        if (callT == 4) {
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(150); //You can manage the time of the blink with this parameter
            anim.setStartOffset(100);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            m.startAnimation(anim);
            ObjectAnimator colorAnim = ObjectAnimator.ofInt(m, "textColor", Color.RED, Color.BLUE);
            colorAnim.setDuration(350);
            colorAnim.setRepeatMode(ValueAnimator.REVERSE);
            colorAnim.setRepeatCount(ValueAnimator.INFINITE);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.start();
        }
        Button closeBtn = findViewById(R.id.button);
        Button infBtn = findViewById(R.id.buttonI);
        closeBtn.setOnClickListener(v -> finish());
        infBtn.setOnClickListener(v -> {
            if (pi != null) {
                try {
                    pi.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            finish();
        });

        infBtn.setOnTouchListener(new MyTouchListener());
        closeBtn.setOnTouchListener(new MyTouchListener());
    }
    public static String getBetweenStrings(String text, String textFrom, String textTo) {
        String result;
        //Cut the beginning of the text to meet a 'textFrom' value in it:
        result = text.substring(text.indexOf(textFrom) + textFrom.length());
        // Cut the excessive ending of the text:
        result = result.substring(0,result.indexOf(textTo));
        return result;
    }
    public static String getStartStrings(String text, String textTo) {
        String result;
        // Cut the excessive ending of the text:
        result = text.substring(0,text.indexOf(textTo));
        return result;
    }
    public static String getEndStrings(String text, String textFrom) {
        String result;
        // Cut the beginning of the text to find 'textFeom' value in it:
        result = text.substring(text.indexOf(textFrom) + textFrom.length());
        return result;
    }
    private void playSound(Context context, Uri alert, boolean loop) {
        try {
            int vol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            currentVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);
            vo = am.getStreamVolume(AudioManager.STREAM_ALARM);
            SharedPreferences SP = getSharedPreferences("Settings", MODE_PRIVATE);
            boolean volumeApp = ((Map<String, Object>) SP.getAll()).compute("volumeApp", (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
            if (volumeApp) {
                var volumeAppNum = Integer.parseInt((String)((Map<String, Object>) SP.getAll()).compute("volumeAppNum", (key, val) -> String.valueOf(val == null ? vol : val)));
                am.setStreamVolume(AudioManager.STREAM_ALARM, volumeAppNum, 0);
                mMediaPlayer.setVolume(volumeAppNum, volumeAppNum);
            }
            mMediaPlayer.setDataSource(context, alert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if(loop) {
                mMediaPlayer.setLooping(true);
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
//            Crashlytics.log("error with play number 1");
//            Crashlytics.logException(e);
        } catch (RuntimeException e) {
//            Crashlytics.log("error with play number 2");
//            Crashlytics.logException(e);
        }
    }
    public static void newC(String C)  {
        Stxt = C;
    }
    public static void alerT(PendingIntent pendingIntent)  {
            pi = pendingIntent;
    }
    @Override
    public void onDestroy() {
        SharedPreferences SP = getSharedPreferences("settings",0);
        final boolean sou = ((Map<String, Object>) SP.getAll()).compute("Nsou" + string, (key, val) -> String.valueOf(val == null ? "true" : val)).equals("true");
        vibe = (Vibrator) smsRes.this.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibe != null) {
            vibe.cancel();
        }
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            boolean isp = mMediaPlayer.isPlaying();
            if (sou && isp) {
                am.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
                mMediaPlayer.setVolume(vo, vo);
                mMediaPlayer.release();
            }
        }catch (IllegalStateException e)    {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    private class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    boolean x = oldX1 - event.getX() > 140 || event.getX() - oldX1 > 140;
                    boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                    if (x || y) {
                        isMove1 = true;
                        v.setAlpha(1f);
                    }
                    return true;
                case MotionEvent.ACTION_DOWN:
                    oldY1 = event.getY();
                    oldX1 = event.getX();
                    isMove1 = false;
                    v.setAlpha(0.4f);
                    return true;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1f);
                    if (!isMove1)
                        v.performClick();
                    return true;
            }
            return false;
        }
    }
}