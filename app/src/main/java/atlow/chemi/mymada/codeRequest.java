package atlow.chemi.mymada;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class codeRequest extends AppCompatActivity {
    private ProgressDialog pdialog;
    private FloatingActionButton sendButton;
    private EditText codeA, codeN, codeT;
    private TextInputLayout codeATIL, codeNTIL, codeTTIL;
    private String token;
    private DatabaseReference mFirebaseDatabase;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE};
    private boolean sentToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_request);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(codeRequest.this, R.style.AlertDialogStyle);
        builder.setTitle("שים לב!").setMessage("קוד הפעלה מאפשר שליחת הודעות למוקד מד\"א.\nבמידה ואינך מתנדב פעיל במד\"א, אין לך צורך בקוד הפעלה!").setCancelable(false);
        builder.setPositiveButton("הבנתי", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
        token = FirebaseInstanceId.getInstance().getToken();
        pdialog = new ProgressDialog(codeRequest.this);
        pdialog.setIndeterminate(true);
        pdialog.setMessage("טוען. אנא המתן...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setCancelable(false);
        final Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        TextView dd = (TextView) findViewById(R.id.declare);
        try {
            TextView d = (TextView) pdialog.findViewById(android.R.id.message);
            if (d != null)
                d.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dd.setTypeface(myTypeface);
        codeA = (EditText) findViewById(R.id.codeA);
        codeN = (EditText) findViewById(R.id.codeN);
        codeT = (EditText) findViewById(R.id.codeT);
        codeATIL = (TextInputLayout) findViewById(R.id.codeATIL);
        codeNTIL = (TextInputLayout) findViewById(R.id.codeNTIL);
        codeTTIL = (TextInputLayout) findViewById(R.id.codeTTIL);
        codeA.addTextChangedListener(new MyTextWatcher(codeA));
        codeN.addTextChangedListener(new MyTextWatcher(codeN));
        codeT.addTextChangedListener(new MyTextWatcher(codeT));
        codeA.setTypeface(myTypeface);
        codeN.setTypeface(myTypeface);
        codeT.setTypeface(myTypeface);
        codeATIL.setTypeface(myTypeface);
        codeNTIL.setTypeface(myTypeface);
        codeTTIL.setTypeface(myTypeface);
        sendButton = (FloatingActionButton) findViewById(R.id.sendE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) sendButton.getLayoutParams();
        if (SettingsActivity.isRTL())
            sendButton.setRotationY(180);
        if (!SettingsActivity.isRTL()) {
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            sendButton.setLayoutParams(lp);
        }
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = codeA.getText().toString().trim();
                String name = codeN.getText().toString().trim();
                String message = codeT.getText().toString().trim();
                if (email.length() < 1) {
                    codeATIL.setError("לא רשמת כתובת מייל!");
                } else if (!isEmailValid(email)) {
                    codeATIL.setError("כתובת המייל שהזנת אינה תקינה!");
                } else if (isEmailValid(email)) {
                    codeATIL.setErrorEnabled(false);
                }
                if (name.length() < 1) {
                    codeNTIL.setError("לא רשמת שם!");
                } else if (name.length() >= 1) {
                    codeNTIL.setErrorEnabled(false);
                }
                if (message.length() < 1) {
                    codeTTIL.setError("לא רשמת תוכן בהודעה!");
                } else if (message.length() >= 1) {
                    codeTTIL.setErrorEnabled(false);
                }
                if (!email.isEmpty() && isEmailValid(email) && !name.isEmpty() && !message.isEmpty()) {
                    sms(email, name, message);
                }
            }
        });
    }

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void sendMailNew(String codeA, String codeN, String codeT) {
        String randomLetters = "0123456789";
        String res = "", mPhoneNumber = "";
        for (int n = 0; n < 4; n++)
            res += randomLetters.charAt(new Random().nextInt(randomLetters.length()));
        TelephonyManager tMgr = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        SharedPreferences permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(codeRequest.this,permissionsRequired[0]) || ActivityCompat.shouldShowRequestPermissionRationale(codeRequest.this,permissionsRequired[1])){
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(codeRequest.this, R.style.AlertDialogStyle);
                    builder.setTitle("נדרשות לאפלקציה הרשאות מסוימות");
                    builder.setMessage("האפליקציה זקוקה להרשאות מסוימות כדי שנאמת שאתה רשאי להשתמש באפליקציה.");
                    builder.setPositiveButton("אישור הרשאות", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(codeRequest.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton("דחה", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(permissionsRequired[0],false) || permissionStatus.getBoolean(permissionsRequired[1],false)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(codeRequest.this, R.style.AlertDialogStyle);
                    builder.setTitle("נדרשות לאפלקציה הרשאות מסוימות");
                    builder.setMessage("האפליקציה זקוקה להרשאות מסוימות כדי שנאמת שאתה רשאי להשתמש באפליקציה.");
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
                    ActivityCompat.requestPermissions(codeRequest.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                }
            return;
        }
        mPhoneNumber = tMgr.getLine1Number();
        Long tsLong = System.currentTimeMillis()/1000;
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> codeReq = new HashMap<>();
        codeReq.put("email", codeA);
        codeReq.put("name", codeN);
        codeReq.put("reason", codeT);
        codeReq.put("code", res);
        codeReq.put("token", token);
        codeReq.put("allowed", false);
        codeReq.put("used", false);
        codeReq.put("Pnum", mPhoneNumber);
        codeReq.put("time", tsLong.toString());
        mFirebaseDatabase = mFirebaseDatabase.child("codes");
        mFirebaseDatabase = mFirebaseDatabase.push();
        mFirebaseDatabase.setValue(codeReq);
        pdialog.cancel();
        gooddi("הבקשה נרשמה אצלינו והמנהלים יבדקו אותה בהקדם!");
        SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("firebaseGenratedKey", mFirebaseDatabase.getKey());
        editor.apply();
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.codeA:
                    if (codeA.getText().toString().trim().isEmpty())
                        codeATIL.setError("לא רשמת כתובת אימייל שלך!");
                    else if(!isEmailValid(codeA.getText().toString().trim()))
                        codeATIL.setError("כתובת המייל שרשמת אינה תקינה!");
                    else
                        codeATIL.setErrorEnabled(false);
                    break;
                case R.id.codeN:
                    if(codeN.getText().toString().isEmpty())
                        codeNTIL.setError("לא רשמת מה שמך!");
                    else
                        codeNTIL.setErrorEnabled(false);
                    break;
                case R.id.codeT:
                    if(codeT.getText().toString().isEmpty())
                        codeTTIL.setError("לא רשמת את תוכן ההודעה");
                    else
                        codeTTIL.setErrorEnabled(false);
                    break;
            }
        }
    }

    private void di(String err)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void gooddi(String good)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(good).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void sms(final String codeA, final String codeN, final String codeT)   {
        String alert = getApplicationContext().getResources().getString(R.string.emailAlert) + "\n\n";
        String sender = alert + getApplicationContext().getResources().getString(R.string.sender) + codeN + " - " + codeA + "\n";
        SpannableString subj = new SpannableString(sender + "\n" + getApplicationContext().getResources().getString(R.string.msg) + "\n" + codeT);
        subj.setSpan(new ForegroundColorSpan(Color.RED), 0, alert.length(), 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), alert.length(), alert.length()+5, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sender.length(), sender.length()+7, 0);
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle).setTitle(getApplicationContext().getResources().getString(R.string.suc)).setMessage(subj)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendButton.setEnabled(false);
                        pdialog = new ProgressDialog(codeRequest.this);
                        pdialog.setIndeterminate(true);
                        pdialog.setMessage("טוען. אנא המתן...");
                        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pdialog.setCancelable(false);
                        pdialog.show();
                        //sendMail(codeA, codeN, codeT);
                        sendMailNew(codeA, codeN, codeT);
                        dialog.cancel();
                    }
                }).create();
        TextView textView = (TextView) builder.findViewById(android.R.id.message);
        final Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        if (textView!= null)
            textView.setTypeface(myTypeface);
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                canc.setTypeface(myTypeface);
                ok.setTypeface(myTypeface);
                canc.setTextColor(codeRequest.this.getResources().getColor(R.color.ColorPrimary));
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getDelegate().getMenuInflater().inflate(R.menu.popup_menu, menu);
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
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    ContextCompat.startForegroundService(this, serviceIntent);
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
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    stopService(serviceIntent);
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
        }catch (Exception e)    {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(codeRequest.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(codeRequest.this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                SharedPreferences sp = getSharedPreferences("Settings", 0);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(codeRequest.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(codeRequest.this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                SharedPreferences sp = getSharedPreferences("Settings", 0);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
            }
        }
    }
}
