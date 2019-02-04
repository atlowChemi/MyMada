package atlow.chemi.mymada;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class smsRes extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    int currentVolume;
    private String string;
    private static PendingIntent pi;
    private Vibrator vibe;
    private static String Stxt = "מקרה חדש";
    public Context context;
    public static float vo = 0;
    private float oldX1, oldY1;
    private boolean isMove1 = false;
    @SuppressLint("ClickableViewAccessibility")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar()!=null)
            getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(smsRes.this, R.color.OldPrimary));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_res);
        //findViewById(R.id.home).setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        Crashlytics.log("SmsRes Has Been Opened!");
        Bundle extras = getIntent().getExtras();
        int callT = extras.getInt("callT");
        switch (callT)  {
            case 1:
                string = "T1";
                break;
            case 2:
                string = "T2";
                break;
            case 3:
                string = "T3";
                break;
            case 4:
                string = "T4";
                break;
            case 5:
                string = "T3";
                break;
            case 6:
                string = "T6";
                break;
            default:
                string = "T1";
                break;
        }
        mMediaPlayer = new MediaPlayer();
        SharedPreferences SP = getSharedPreferences("Settings",0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        Uri url = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.alert);
        long[] length = {0, 900L, 200, 900, 200, 900, 200, 900L, 200, 900, 200};
        vibe = (Vibrator) smsRes.this.getSystemService(Context.VIBRATOR_SERVICE);
        boolean not = SP.getBoolean("not", true);
        boolean cut = SP.getBoolean("cut", false);
        boolean vib;
        boolean soun;
        boolean app;
        Uri uri;
        if (string.equals("T6") || string.equals("T4"))    {
            vib = SP.getBoolean("NvibT4", true);
            soun = SP.getBoolean("NsouT4", true);
            app = SP.getBoolean("NappT4",false);
            uri = Uri.parse(SP.getString("NuRiT4", ""));
        }   else if (cut) {
            vib = SP.getBoolean("Nvib" + string, true);
            soun = SP.getBoolean("Nsou" + string, true);
            app = SP.getBoolean("Napp" + string,false);
            uri = Uri.parse(SP.getString("NuRi" + string, ""));
        }   else {
            vib = SP.getBoolean("Nvib", true);
            soun = SP.getBoolean("Nsou", true);
            app = SP.getBoolean("Napp", false);
            uri = Uri.parse(SP.getString("NuRi", ""));
        }
        Calendar now = Calendar.getInstance(TimeZone.getDefault());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int sleep = SP.getInt("sleep", 0);
        int nightStart = SP.getInt("nightStart", 23);
        int nightEnd = SP.getInt("nightEnd", 7);
        if ((hour <= nightEnd || hour >= nightStart) || (hour <= nightEnd && hour >= nightStart)) {
            //Toast.makeText(getApplicationContext(), "night!!", Toast.LENGTH_SHORT).show();
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
        else/* if (hour > nightEnd || hour < nightStart)*/  {
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
        boolean loop = SP.getBoolean("loop",false);
        boolean ShabbatM = SP.getBoolean("killS",false);
        boolean stat = SP.getBoolean("stat",false);
        boolean statVib = SP.getBoolean("statVib",false);
        boolean silent = true;
        boolean silentVib = true;
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)
            silent = false;
        if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
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
            handler.postDelayed(new Runnable() {
                public void run() {
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
                }
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
            case 1:
                m.setText("כונן");
                break;
            case 2:
                m.setText("קבוצה");
                break;
            case 3:
                m.setText("משמרת");
                break;
            case 4:
                m.setText("הודעה מנהלתית");
                break;
            case 5:
                m.setText("משמרת");
                break;
            case 6:
                m.setText("שירות");
                break;
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
                Log.e("smsRes OldSMS", e.getMessage());
                Crashlytics.logException(e);
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
        } else if (!Stxt.contains("אירוע חדש") && !Stxt.contains("הודעת קבוצת התראה צוות") && !Stxt.contains("הודעת צוות"))    {
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
        } else {
            try {
                Address = getBetweenStrings(Stxt, "כתובת: ", " קוד");
            } catch (Exception e) {
                try {
                    Address = getBetweenStrings(Stxt, "כתובת: ", ", קוד");
                } catch (Exception ew) {
                    Address = "";
                }
            }
            try {
                KodH = getBetweenStrings(Stxt, "קוד הזנקה: ", ". קוד רפואי");
            } catch (Exception e) {
                try {
                    KodH = getBetweenStrings(Stxt, "קוד הזנקה: ", ", קוד רפואי");
                } catch (Exception ew) {
                    KodH = "";
                }
            }

            try {
                KodR = getBetweenStrings(Stxt, "קוד רפואי: ", ".");
            } catch (Exception e) {
                try {
                    KodR = getEndStrings(Stxt, "קוד רפואי: ");
                } catch (Exception ew) {
                    KodR = "";
                }
            }
            try {
                time = getBetweenStrings(Stxt, ". ", "מונה:");
            } catch (Exception e) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                time = sdf.format(now.getTime());
            }
            try {
                mikre = getStartStrings(Stxt, "קוד הזנקה:");
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
            String add = Address.equals("") ? mikre : Address;
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
        //wakeLock.acquire();
        Button closeBtn = findViewById(R.id.button);
        Button infBtn = findViewById(R.id.buttonI);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        infBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pi != null) {
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }
        });

        infBtn.setOnTouchListener(new MyTouchListener());
        closeBtn.setOnTouchListener(new MyTouchListener());
    }
    public static String getBetweenStrings(String text, String textFrom, String textTo) {
        String result = "";
        // Cut the beginning of the text to not occasionally meet a
        // 'textTo' value in it:
        result = text.substring(text.indexOf(textFrom) + textFrom.length(), text.length());
        // Cut the excessive ending of the text:
        result = result.substring(0,result.indexOf(textTo));
        return result;
    }
    public static String getStartStrings(String text, String textTo) {
        String result = "";
        // Cut the excessive ending of the text:
        result = text.substring(0,text.indexOf(textTo));
        return result;
    }
    public static String getEndStrings(String text, String textFrom) {
        String result = "";
        // Cut the beginning of the text to not occasionally meet a
        // 'textTo' value in it:
        result = text.substring(text.indexOf(textFrom) + textFrom.length(), text.length());
        // Cut the excessive ending of the text:
        /*Not needed because that it is at the end of the text*/
        //result = result.substring(0,result.length());
        return result;
    }
    private void playSound(Context context, Uri alert, Boolean loop) {
        try {
            final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int vol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            vo = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);
            mMediaPlayer.setVolume(vol, vol);
            mMediaPlayer.setDataSource(context, alert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if(loop) {
                mMediaPlayer.setLooping(true);
            }
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }
    public static void newC(String C)  {
        Stxt = C;
    }
    public static void alerT(PendingIntent pendingIntent)  {
            pi = pendingIntent;
    }
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus)  {
        /*try {
            if(Build.VERSION_CODES.JELLY_BEAN <= Build.VERSION.SDK_INT) {
                if (getSupportActionBar()!=null)
                    getSupportActionBar().hide();
                View decorView = getWindow().getDecorView();
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions | View.SYSTEM_UI_FLAG_FULLSCREEN);
                ///getWindow().addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }catch (OutOfMemoryError e) {
            e.printStackTrace();
        }*/
    }
    @Override
    public void onDestroy() {
        SharedPreferences SP = getSharedPreferences("settings",0);
        final Boolean sou = SP.getBoolean("Nsou" + string, true);
        vibe = (Vibrator) smsRes.this.getSystemService(Context.VIBRATOR_SERVICE);
        vibe.cancel();
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }*/
        try {
            boolean isp = mMediaPlayer.isPlaying();
            if (sou && isp) {
                final AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                //int a = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                //Toast.makeText(getApplicationContext(),"maxVol: " + a + "\noldVol: " + currentVolume,Toast.LENGTH_LONG).show();
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, currentVolume, 0);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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