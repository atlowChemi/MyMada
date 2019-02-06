package atlow.chemi.mymada;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class contactActivity extends AppCompatActivity {
    private static final String BASE_URL = "https://us-central1-project-3918384647339420127.cloudfunctions.net/date";
    private OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
    private ProgressDialog pdialog;
    private EditText emailA, emailN, emailS, emailT;
    private Typeface myTypeface;
    private TextInputLayout emailATIL, emailNTIL, emailSTIL, emailTTIL;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private Button sendEM;
    private boolean isMove = false;
    private float oldX1, oldY1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pdialog = new ProgressDialog(contactActivity.this);
        pdialog.setIndeterminate(true);
        pdialog.setMessage(getApplicationContext().getResources().getString(R.string.load));
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setCancelable(false);
        emailA = findViewById(R.id.emailA);
        emailN = findViewById(R.id.emailN);
        emailS = findViewById(R.id.emailS);
        emailT = findViewById(R.id.emailT);
        emailATIL = findViewById(R.id.emailATIL);
        emailNTIL = findViewById(R.id.emailNTIL);
        emailSTIL = findViewById(R.id.emailSTIL);
        emailTTIL = findViewById(R.id.emailTTIL);
        emailA.addTextChangedListener(new MyTextWatcher(emailA));
        emailN.addTextChangedListener(new MyTextWatcher(emailN));
        emailS.addTextChangedListener(new MyTextWatcher(emailS));
        emailT.addTextChangedListener(new MyTextWatcher(emailT));

        sendEM = findViewById(R.id.sendEM);

        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        emailA.setTypeface(myTypeface);
        emailATIL.setTypeface(myTypeface);
        emailN.setTypeface(myTypeface);
        emailNTIL.setTypeface(myTypeface);
        emailT.setTypeface(myTypeface);
        emailTTIL.setTypeface(myTypeface);
        emailS.setTypeface(myTypeface);
        emailSTIL.setTypeface(myTypeface);
        Drawable img = getApplicationContext().getResources().getDrawable(R.drawable.ic_send);
        sendEM.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        sendEM.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX1 - event.getX() > 60 || event.getX() - oldX1 > 60;
                        boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                        if (x || y) {
                            isMove = true;
                            sendEM.setAlpha(1f);
                        }
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY1 = event.getY();
                        oldX1 = event.getX();
                        isMove = false;
                        sendEM.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendEM.setAlpha(1f);
                        if (!isMove)
                            sendEM.performClick();
                        return true;
                }
                return false;
            }
        });
        sendEM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailA.getText().toString().trim();
                String name = emailN.getText().toString().trim();
                String subject = emailS.getText().toString().trim();
                String message = emailT.getText().toString().trim();
                if (email.length() < 1)
                    emailATIL.setError(getResources().getString(R.string.emailNone));
                else if (!isEmailValid(email))
                    emailATIL.setError(getResources().getString(R.string.emailInvalid));
                else if (isEmailValid(email))
                    emailATIL.setErrorEnabled(false);
                if (name.length() < 1)
                    emailNTIL.setError(getResources().getString(R.string.emailNameNone));
                else
                    emailNTIL.setErrorEnabled(false);
                if (subject.length() < 1)
                    emailSTIL.setError(getResources().getString(R.string.emailSubjNone));
                else
                    emailSTIL.setErrorEnabled(false);
                if (message.length() < 1)
                    emailTTIL.setError(getResources().getString(R.string.emailMsgNone));
                else
                    emailTTIL.setErrorEnabled(false);
                if (!email.isEmpty() && isEmailValid(email) && !name.isEmpty() && !subject.isEmpty() && !message.isEmpty()) {
                    sms(email, name, subject, message);
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

    public void sendMail(String emailA, String emailN, String emailS, String emailT) {
        String mPhoneNumber = "unknown";
        TelephonyManager tMgr = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                mPhoneNumber = tMgr != null ? tMgr.getLine1Number() : "err";
            }
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sent from phone: " + mPhoneNumber);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sent from: " + emailA);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Email");
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        RequestBody body = new FormBody.Builder().add("email", emailA).add("name",emailN).add("subject", emailS).add("message",emailT).build();
        Request request = new Request.Builder().url(BASE_URL).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                contactActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pdialog.cancel();
                        sendEM.setEnabled(true);
                        di(getResources().getString(R.string.emailBad));
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String resp = response.body().string();
                    if (response.isSuccessful()) {
                        contactActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendEM.setEnabled(true);
                                pdialog.cancel();
                                //gooddi('"' + resp + '"');
                                if (resp.trim().equals("true"))
                                    gooddi(getResources().getString(R.string.emailGood));
                                else
                                    di(getResources().getString(R.string.emailBad));
                            }
                        });
                    } else {
                        contactActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendEM.setEnabled(true);
                                pdialog.cancel();
                                di(getResources().getString(R.string.emailBad));
                            }
                        });
                    }
                } catch (IOException e) {
                    contactActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendEM.setEnabled(true);
                            pdialog.cancel();
                            di(getResources().getString(R.string.emailBad));
                        }
                    });
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
                case R.id.emailA:
                    if (emailA.getText().toString().isEmpty())
                        emailATIL.setError(getApplicationContext().getResources().getString(R.string.emailNone));
                    else if(!isEmailValid(emailA.getText().toString()))
                        emailATIL.setError(getApplicationContext().getResources().getString(R.string.emailInvalid));
                    else if(isEmailValid(emailA.getText().toString()))
                        emailATIL.setErrorEnabled(false);
                    break;
                case R.id.emailN:
                    if(emailN.getText().toString().isEmpty())
                        emailNTIL.setError(getApplicationContext().getResources().getString(R.string.emailNameNone));
                    else
                        emailNTIL.setErrorEnabled(false);
                    break;
                case R.id.emailS:
                    if (emailS.getText().toString().isEmpty())
                        emailSTIL.setError(getApplicationContext().getResources().getString(R.string.emailSubjNone));
                    else
                        emailSTIL.setErrorEnabled(false);
                    break;
                case R.id.emailT:
                    if(emailT.getText().toString().isEmpty())
                        emailTTIL.setError(getApplicationContext().getResources().getString(R.string.emailMsgNone));
                    else
                        emailTTIL.setErrorEnabled(false);
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
    private void sms(final String emailA, final String emailN, final String emailS, final String emailT)   {
        String alert = contactActivity.this.getResources().getString(R.string.emailAlert) + "\n\n";
        String sender = alert + contactActivity.this.getResources().getString(R.string.sender) + emailN + " - " + emailA + "\n";
        String sub = sender + getResources().getString(R.string.subject) +  emailS;
        int sendI = getResources().getString(R.string.sender).length();
        int subI = getResources().getString(R.string.subject).length();
        int msgI = getResources().getString(R.string.msg).length();
        SpannableString subj = new SpannableString(sub + "\n" + getResources().getString(R.string.msg) + "\n" + emailT);
        subj.setSpan(new ForegroundColorSpan(Color.RED), 0, alert.length(), 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), alert.length(), alert.length()+sendI, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sender.length(), sender.length()+subI, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sub.length(), sub.length()+msgI, 0);
        subj.setSpan(myTypeface, 0, subj.length(), 0);
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle).setTitle(getResources().getString(R.string.suc)).setMessage(subj)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendEM.setEnabled(false);
                        pdialog.show();
                        sendMail(emailA, emailN, emailS, emailT);
                        dialog.cancel();
                    }
                }).create();
        TextView textView = builder.findViewById(android.R.id.message);
        if (textView!= null)
            textView.setTypeface(myTypeface);
        textView = (TextView) builder.findViewById(android.R.id.title);
        if (textView!= null)
            textView.setTypeface(myTypeface);
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setTypeface(myTypeface);
                canc.setTypeface(myTypeface);
                canc.setTextColor(contactActivity.this.getResources().getColor(R.color.OldPrimary));
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

                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.OldPrimary));
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
                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.OldPrimary));
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
                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.OldPrimary));
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
}
