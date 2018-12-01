package atlow.chemi.mymada;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.*;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.Locale;

public class HomePage extends AppCompatActivity {
    private Boolean isSMST = false;
    private Context context;
    private Typeface myTypeface;
    private Button btn1, btn2, btn3, btn4, btn100;
    private boolean isMove1 = false, isMove2 = false, isMove3 = false, isMove4 = false, isMove100 = false;
    private float oldX1, oldY1, oldX2, oldY2, oldX3, oldY3, oldX4, oldY4, oldX100, oldY100;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE};
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        final SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        Boolean realM = sharedPreferences.getBoolean("realM", false);
        AdView mAdView;
        FirebaseAnalytics mFirebaseAnalytics;
        try {
            Log.e("Chemi ", FirebaseInstanceId.getInstance().getToken());
        }catch (Exception e)    {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_home_page);
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7600269766434003/5732229370");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        btn1 = (Button) findViewById(R.id.b1);
        btn2 = (Button) findViewById(R.id.b3);
        btn3 = (Button) findViewById(R.id.b4);
        btn4 = (Button) findViewById(R.id.b5);
        btn100 = (Button) findViewById(R.id.b100);
        btn1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX1 - event.getX() > 30 || event.getX() - oldX1 > 30;
                        boolean y = oldY1 - event.getY() > 30 || event.getY() - oldY1 > 30;
                        if (x || y)
                            isMove1 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY1 = event.getY();
                        oldX1 = event.getX();
                        isMove1 = false;
                        btn1.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn1.setAlpha(1f);
                        if (!isMove1)
                            btn1.performClick();
                        return true;
                }
                return false;
            }
        });
        btn100.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX100 - event.getX() > 30 || event.getX() - oldX100 > 30;
                        boolean y = oldY100 - event.getY() > 30 || event.getY() - oldY100 > 30;
                        if (x || y)
                            isMove100 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY100 = event.getY();
                        oldX100 = event.getX();
                        isMove100 = false;
                        btn100.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn100.setAlpha(1f);
                        if (!isMove100)
                            btn100.performClick();
                        return true;
                }
                return false;
            }
        });
        btn2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX2 - event.getX() > 30 || event.getX() - oldX2 > 30;
                        boolean y = oldY2 - event.getY() > 30 || event.getY() - oldY2 > 30;
                        if (x || y)
                            isMove2 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX2 = event.getX();
                        oldY2 = event.getY();
                        isMove2 = false;
                        btn2.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn2.setAlpha(1f);
                        if (!isMove2)
                            btn2.performClick();
                        return true;
                }
                return false;
            }
        });
        btn3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX3 - event.getX() > 30 || event.getX() - oldX3 > 30;
                        boolean y = oldY3 - event.getY() > 30 || event.getY() - oldY3 > 30;
                        if (x || y)
                            isMove3 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX3 = event.getX();
                        oldY3 = event.getY();
                        isMove3 = false;
                        btn3.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn3.setAlpha(1f);
                        if (!isMove3)
                            btn3.performClick();
                        return true;
                }
                return false;
            }
        });
        btn4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX4 - event.getX() > 30 || event.getX() - oldX4 > 30;
                        boolean y = oldY4 - event.getY() > 30 || event.getY() - oldY4 > 30;
                        if (x || y)
                            isMove4 = true;
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldX4 = event.getX();
                        oldY4 = event.getY();
                        isMove4 = false;
                        btn4.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        btn4.setAlpha(1f);
                        if (!isMove4)
                            btn4.performClick();
                        return true;
                }
                return false;
            }
        });
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        if(ActivityCompat.checkSelfPermission(HomePage.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(HomePage.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(HomePage.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED)    {
            if(ActivityCompat.shouldShowRequestPermissionRationale(HomePage.this,permissionsRequired[0]) || ActivityCompat.shouldShowRequestPermissionRationale(HomePage.this,permissionsRequired[1]) || ActivityCompat.shouldShowRequestPermissionRationale(HomePage.this,permissionsRequired[2])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this, R.style.AlertDialogStyle);
                builder.setTitle("נדרשות לאפלקציה הרשאות מסוימות");
                builder.setMessage("האפליקציה זקוקה להרשאות לקריאת סמסים כדי לזהות מקרים שיגיעו, וכדי לאמת שאתה מתנדב מד\"א.");
                builder.setPositiveButton("אישור הרשאות", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(HomePage.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("דחה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0],false) || permissionStatus.getBoolean(permissionsRequired[1],false) || permissionStatus.getBoolean(permissionsRequired[2],false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(HomePage.this, R.style.AlertDialogStyle);
                builder.setTitle("נדרשות לאפלקציה הרשאות מסוימות");
                builder.setMessage("האפליקציה זקוקה להרשאות לקריאת סמסים כדי לזהות מקרים שיגיעו, וכדי לאמת שאתה מתנדב מד\"א.");
                builder.setPositiveButton("אישור הרשאות", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("דחה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(HomePage.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.putBoolean(permissionsRequired[1],true);
            editor.putBoolean(permissionsRequired[2],true);
            editor.apply();
        } else {
            smsGot();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        Boolean Privacy = sharedPreferences.getBoolean("privacy", false);
        Boolean oldU = sharedPreferences.getBoolean("oldU", false);
        if (!Privacy) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.privacyMsg)).setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.title_activity_privacy), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = (TextView) alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    ok.setTypeface(myTypeface);
                    canc.setTypeface(myTypeface);
                    canc.setTextColor(getResources().getColor(R.color.ColorPrimary));
                }
            });
            alert.show();
        }
        if (!oldU) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.newSettings)).setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.title_activity_settings), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = (TextView) alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    ok.setTypeface(myTypeface);
                    canc.setTypeface(myTypeface);
                    canc.setTextColor(getResources().getColor(R.color.ColorPrimary));
                }
            });
            alert.show();
        }
    }
    public void mok(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        Boolean realM = sharedPreferences.getBoolean("realM", false), isMADA = false;
        if (!realM) {
            isMADA = isPI(getApplicationContext(), "com.groboot.mdaemergency");
            try {
                smsGot();
            } catch (Exception e) {
                isSMST = false;
            }
        }
        Intent intent;
        if (realM || (isMADA && isSMST)) {
            intent = new Intent(getApplicationContext(), MokedPage.class);
            SharedPreferences.Editor sp1 = sharedPreferences.edit();
            sp1.putBoolean("realM", true);
            sp1.apply();
        }else
            intent = new Intent(getApplicationContext(), lockPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void con(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        Boolean realM = sharedPreferences.getBoolean("realM", false), isMADA = false;
        if (!realM) {
            isMADA = isPI(getApplicationContext(), "com.groboot.mdaemergency");
            try {
                smsGot();
            } catch (Exception e) {
                isSMST = false;
            }
        }
        if (realM || (isMADA && isSMST)) {
            Intent intent = new Intent(getApplicationContext(), contactActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else   {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setTitle("שים לב!").setMessage("האפליקציה לא מזהה אותך כמתנדב. האם אתה מעוניין בקוד הפעלה או ביצירת קשר רגילה?").setCancelable(true)
                    .setNegativeButton("אני רוצה קוד הפעלה", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getApplicationContext(), codeRequest.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).setPositiveButton("יצירת קשר", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(getApplicationContext(), contactActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            builder.show();
        }
        //Toast.makeText(getApplicationContext(),"עובדים על זה...",Toast.LENGTH_SHORT).show();
    }
    public void tol(View view) {
        Intent intent = new Intent(getApplicationContext(), UtillTools.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void inf(View view) {
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    public void tes(View view) {
        final SharedPreferences sp = getSharedPreferences("Settings", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(getResources().getString(R.string.TestCallMsg)).setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.TestCallSMS), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if ((sp.getBoolean("not", true) || sp.getBoolean("win", true)) && sp.getBoolean("Mapp", false)) {
                            if (sp.getBoolean("cut", false) && !sp.getBoolean("callT3", true)) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.MISHM_reco_off), Toast.LENGTH_SHORT).show();
                            } else {
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
                                PendingIntent testPI;
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    testPI = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), launchIntent, 0);
                                } else {
                                    testPI = null;
                                }
                                Intent intent = new Intent("atlow.chemi.MyMada.MADA_APP");
                                intent.setAction("atlow.chemi.MyMada.MADA_APP");
                                intent.putExtra("callT", 3);
                                intent.putExtra("text", getResources().getString(R.string.TEAMTest));
                                GrobootRec.madasApp(testPI);
                                sendBroadcast(intent);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.TestCallApp), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if ((sp.getBoolean("not", true) || sp.getBoolean("win", true)) && sp.getBoolean("Mapp", false)) {
                            if (sp.getBoolean("cut", false) && !sp.getBoolean("callT1", true)) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.CONAN_reco_off), Toast.LENGTH_SHORT).show();
                            } else {
                                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.groboot.mdaemergency");
                                PendingIntent testPI;
                                if (launchIntent != null) {
                                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    testPI = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), launchIntent, 0);
                                } else {
                                    testPI = null;
                                }
                                Intent intent = new Intent("atlow.chemi.MyMada.MADA_APP");
                                intent.setAction("atlow.chemi.MyMada.MADA_APP");
                                intent.putExtra("callT", 1);
                                intent.putExtra("text", getResources().getString(R.string.CONANTest));
                                GrobootRec.madasApp(testPI);
                                sendBroadcast(intent);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.APP_reco_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        final AlertDialog alert = builder.create();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        if (textView != null)
            textView.setTypeface(myTypeface);
        textView = (TextView) alert.findViewById(android.R.id.title);
        if (textView != null)
            textView.setTypeface(myTypeface);
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setTypeface(myTypeface);
                canc.setTypeface(myTypeface);
                canc.setTextColor(getResources().getColor(R.color.ColorPrimary));
            }
        });
        alert.show();
    }
    public static boolean isPI(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    private void smsGot() {
        try {
            Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
            Cursor cursor1 = getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type"}, null, null, null);
            startManagingCursor(cursor1);
            String[] columns = new String[]{"address", "person", "date", "body", "type"};
            if (cursor1 != null && cursor1.getCount() > 0) {
                while (cursor1.moveToNext()) {
                    String address = cursor1.getString(cursor1.getColumnIndex(columns[0]));
                    if (address.equalsIgnoreCase("MADA")) {
                        String body = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                        if (body.contains("רישום נקלט בהצלחה"))
                            isSMST = true;
                    }
                }
            }
        }catch (Exception e)    {
            isSMST = false;
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        boolean newLang = sp.getBoolean("WasLangChanged", false);
        if (newLang)
            recreate();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("WasLangChanged", false);
        editor.apply();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(HomePage.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                SharedPreferences sp = getSharedPreferences("Settings", 0);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
            }
        }
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(HomePage.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                SharedPreferences sp = getSharedPreferences("Settings", 0);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        SpannableString reco = new SpannableString(getResources().getString(R.string.pref_title_enable));
        reco.setSpan(new ForegroundColorSpan(Color.BLACK), 0, reco.length(), 0);
        SpannableString sett = new SpannableString(getResources().getString(R.string.action_settings));
        sett.setSpan(new ForegroundColorSpan(Color.BLACK), 0, sett.length(), 0);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setTitle(reco);
        menu.findItem(R.id.action_settings).setTitle(sett);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        ComponentName component = new ComponentName(getBaseContext(), SMSReceiver.class);
        int status = getPackageManager().getComponentEnabledSetting(component);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                SharedPreferences.Editor s = sp.edit();
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                String enabledNotificationListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
                String packageName = getApplicationContext().getPackageName();
                if (!item.isChecked()) {
                    item.setChecked(true);
                    /*new*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                        ContextCompat.startForegroundService(this, serviceIntent);
                    }   else
                        getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    /*end new*/
                    Intent test = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    boolean activityExists = isCallable(test);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (activityExists) {
                            try {
                                if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            } catch (ActivityNotFoundException e) {
                                Intent intent = new Intent(this, NotificationService.class);
                                Intent old = new Intent(this, NotificationServiceOld.class);
                                stopService(intent);
                                startService(old);
                                if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            }
                        } else {
                            Intent intent = new Intent(this, NotificationService.class);
                            Intent old = new Intent(this, NotificationServiceOld.class);
                            stopService(intent);
                            startService(old);
                            if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                final AlertDialog alert = builder.create();
                                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                    }
                                });
                                alert.show();
                            }
                            s.putBoolean("Mapp", true);
                        }
                    } else {
                        s.putBoolean("Mapp", true);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                        stopService(serviceIntent);
                    }else
                        getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    item.setChecked(false);
                    s.putBoolean("Mapp", false);
                }
                s.apply();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isCallable(Intent intent) {
        try {
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
