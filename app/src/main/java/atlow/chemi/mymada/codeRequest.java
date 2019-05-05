package atlow.chemi.mymada;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
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
    private EditText codeA, codeN, codeT;
    private TextInputLayout codeATIL, codeNTIL, codeTTIL;
    private String token;
    private DatabaseReference mFirebaseDatabase;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{android.Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE};
    private boolean sentToSettings = false, isMove = false, isConnected = true;
    private float oldX1, oldY1;
    private Button sendCR;
    private MenuItem Detect;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_code_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(codeRequest.this, R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.plsNote))
                .setMessage(getResources().getString(R.string.plsNoteT)).setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
        token = FirebaseInstanceId.getInstance().getToken();
        pdialog = new ProgressDialog(codeRequest.this);
        pdialog.setIndeterminate(true);
        pdialog.setMessage(getResources().getString(R.string.load));
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setCancelable(false);
        final Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        TextView dd = findViewById(R.id.declare);
        try {
            TextView d = pdialog.findViewById(android.R.id.message);
            if (d != null)
                d.setTypeface(myTypeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dd.setTypeface(myTypeface);
        codeA = findViewById(R.id.codeA);
        codeN = findViewById(R.id.codeN);
        codeT = findViewById(R.id.codeT);
        codeATIL = findViewById(R.id.codeATIL);
        codeNTIL = findViewById(R.id.codeNTIL);
        codeTTIL = findViewById(R.id.codeTTIL);
        codeA.addTextChangedListener(new MyTextWatcher(codeA));
        codeN.addTextChangedListener(new MyTextWatcher(codeN));
        codeT.addTextChangedListener(new MyTextWatcher(codeT));
        codeA.setTypeface(myTypeface);
        codeN.setTypeface(myTypeface);
        codeT.setTypeface(myTypeface);
        codeATIL.setTypeface(myTypeface);
        codeNTIL.setTypeface(myTypeface);
        codeTTIL.setTypeface(myTypeface);
        sendCR = findViewById(R.id.sendCR);
        Drawable img = getApplicationContext().getResources().getDrawable(R.drawable.ic_send);
        sendCR.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        sendCR.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX1 - event.getX() > 10 || event.getX() - oldX1 > 10;
                        boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                        if (x || y) {
                            isMove = true;
                            sendCR.setAlpha(1f);
                        }
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY1 = event.getY();
                        oldX1 = event.getX();
                        isMove = false;
                        sendCR.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendCR.setAlpha(1f);
                        if (!isMove)
                            sendCR.performClick();
                        return true;
                }
                return false;
            }
        });

        sendCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = codeA.getText().toString().trim();
                String name = codeN.getText().toString().trim();
                String message = codeT.getText().toString().trim();
                if (email.length() < 1)
                    codeATIL.setError(getResources().getString(R.string.emailNone));
                else if(!isEmailValid(email))
                    codeATIL.setError(getResources().getString(R.string.emailInvalid));
                else if(isEmailValid(email))
                    codeATIL.setErrorEnabled(false);
                if (name.length() < 1)
                    codeNTIL.setError(getResources().getString(R.string.emailNameNone));
                else
                    codeNTIL.setErrorEnabled(false);
                if(message.length() < 1)
                    codeTTIL.setError(getResources().getString(R.string.emailMsgNone));
                else
                    codeTTIL.setErrorEnabled(false);
                if (!email.isEmpty() && isEmailValid(email) && !name.isEmpty() && !message.isEmpty()) {
                    sms(email, name, message);
                }
            }
        });
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).labelRes;
            if (label != 0)
                setTitle(label);
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.log(e.getMessage());
        }
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
                    builder.setTitle(getResources().getString(R.string.permTitle));
                    builder.setMessage(getResources().getString(R.string.permDes));
                    builder.setPositiveButton(getResources().getString(R.string.permYes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(codeRequest.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else if (permissionStatus.getBoolean(permissionsRequired[0],false) || permissionStatus.getBoolean(permissionsRequired[1],false)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(codeRequest.this, R.style.AlertDialogStyle);
                    builder.setTitle(getResources().getString(R.string.permTitle));
                    builder.setMessage(getResources().getString(R.string.permDes));
                    builder.setPositiveButton(getResources().getString(R.string.permYes), new DialogInterface.OnClickListener() {
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
                    builder.setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
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
        try {
            mPhoneNumber = tMgr != null ? tMgr.getLine1Number() : "";
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
        Long tsLong = System.currentTimeMillis()/1000;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info == null || !info.isConnectedOrConnecting())
                isConnected = false;
        }
        if (isConnected) {
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
            mFirebaseDatabase.setValue(codeReq).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    try {
                        gooddi(getResources().getString(R.string.requestGood));
                    } catch (WindowManager.BadTokenException e) {
                        Crashlytics.log("gooddi called after window closed.");
                        Crashlytics.logException(e);
                    }
                    SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("firebaseGenratedKey", mFirebaseDatabase.getKey());
                    editor.apply();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendCR.setEnabled(false);
                    di(getResources().getString(R.string.emailBad));
                }
            });
            pdialog.cancel();

        } else {
            pdialog.cancel();
            sendCR.setEnabled(false);
            di(getResources().getString(R.string.emailBad));
        }
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
                case R.id.codeA:
                    if (codeA.getText().toString().isEmpty())
                    codeATIL.setError(getResources().getString(R.string.emailNone));
                else if(!isEmailValid(codeA.getText().toString()))
                    codeATIL.setError(getResources().getString(R.string.emailInvalid));
                else if(isEmailValid(codeA.getText().toString()))
                    codeATIL.setErrorEnabled(false);
                    break;
                case R.id.codeN:
                    if(codeN.getText().toString().isEmpty())
                        codeNTIL.setError(getResources().getString(R.string.emailNameNone));
                    else
                        codeNTIL.setErrorEnabled(false);
                    break;
                case R.id.codeT:
                    if(codeT.getText().toString().isEmpty())
                        codeTTIL.setError(getResources().getString(R.string.emailMsgNone));
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
        String alert = getResources().getString(R.string.emailAlert) + "\n\n";
        String sender = alert + getResources().getString(R.string.sender) + codeN + " - " + codeA + "\n";
        SpannableString subj = new SpannableString(sender + "\n" + getResources().getString(R.string.msg) + "\n" + codeT);
        subj.setSpan(new ForegroundColorSpan(Color.RED), 0, alert.length(), 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), alert.length(), alert.length()+5, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sender.length(), sender.length()+7, 0);
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle).setTitle(getResources().getString(R.string.suc)).setMessage(subj)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendCR.setEnabled(false);
                        pdialog = new ProgressDialog(codeRequest.this);
                        pdialog.setIndeterminate(true);
                        pdialog.setMessage(getResources().getString(R.string.load));
                        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pdialog.setCancelable(false);
                        pdialog.show();
                        //sendMail(codeA, codeN, codeT);
                        sendMailNew(codeA, codeN, codeT);
                        dialog.cancel();
                    }
                }).create();
        TextView textView = builder.findViewById(android.R.id.message);
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
                canc.setTextColor(codeRequest.this.getResources().getColor(R.color.OldPrimary));
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
        Detect = menu.findItem(R.id.one);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setIcon(icon);
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
                    item.setIcon(R.drawable.ic_detect_on);
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
                    item.setIcon(R.drawable.ic_detect_off);
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