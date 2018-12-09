package atlow.chemi.mymada;

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
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.List;
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
    private FloatingActionButton sendButton;
    private EditText emailA, emailN, emailS, emailT;
    private Typeface myTypeface;
    private TextInputLayout emailATIL, emailNTIL, emailSTIL, emailTTIL;
    private static final int REQUEST_PERMISSION_SETTING = 101;
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
        pdialog.setMessage("טוען. אנא המתן...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setCancelable(false);
        emailA = (EditText)findViewById(R.id.emailA);
        emailN = (EditText)findViewById(R.id.emailN);
        emailS = (EditText)findViewById(R.id.emailS);
        emailT = (EditText)findViewById(R.id.emailT);
        emailATIL = (TextInputLayout) findViewById(R.id.emailATIL);
        emailNTIL = (TextInputLayout)findViewById(R.id.emailNTIL);
        emailSTIL = (TextInputLayout)findViewById(R.id.emailSTIL);
        emailTTIL = (TextInputLayout)findViewById(R.id.emailTTIL);
        emailA.addTextChangedListener(new MyTextWatcher(emailA));
        emailN.addTextChangedListener(new MyTextWatcher(emailN));
        emailS.addTextChangedListener(new MyTextWatcher(emailS));
        emailT.addTextChangedListener(new MyTextWatcher(emailT));
        sendButton = (FloatingActionButton)findViewById(R.id.sendE);

        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        emailA.setTypeface(myTypeface);
        emailATIL.setTypeface(myTypeface);
        emailN.setTypeface(myTypeface);
        emailNTIL.setTypeface(myTypeface);
        emailT.setTypeface(myTypeface);
        emailTTIL.setTypeface(myTypeface);
        emailS.setTypeface(myTypeface);
        emailSTIL.setTypeface(myTypeface);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) sendButton.getLayoutParams();
        if(SettingsActivity.isRTL())
            sendButton.setRotationY(180);
        if (!SettingsActivity.isRTL()) {
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            sendButton.setLayoutParams(lp);
        }
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailA.getText().toString().trim();
                String name = emailN.getText().toString().trim();
                String subject = emailS.getText().toString().trim();
                String message = emailT.getText().toString().trim();
                if (email.length() < 1) {
                    emailATIL.setError("לא רשמת כתובת מייל!");
                }else if(!isEmailValid(email))  {
                    emailATIL.setError("כתובת המייל שהזנת אינה תקינה!");
                }else if(isEmailValid(email)) {
                    emailATIL.setErrorEnabled(false);
                }if (name.length() < 1) {
                    emailNTIL.setError("לא רשמת שם!");
                }else if(name.length() >= 1)    {
                    emailNTIL.setErrorEnabled(false);
                }
                if (subject.length() < 1) {
                    emailSTIL.setError("לא רשמת נושא להודעה!");
                }else if(subject.length() >= 1) {
                    emailSTIL.setErrorEnabled(false);
                }if(message.length() < 1)  {
                    emailTTIL.setError("לא רשמת תוכן בהודעה!");
                }else if(message.length() >= 1)  {
                    emailTTIL.setErrorEnabled(false);
                }
                if(!email.isEmpty() && isEmailValid(email) && !name.isEmpty() && !subject.isEmpty() && !message.isEmpty()) {
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
                        sendButton.setEnabled(true);
                        di("שליחה הופסקה בשל תקלה. נסה שוב!");
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
                                sendButton.setEnabled(true);
                                pdialog.cancel();
                                //gooddi('"' + resp + '"');
                                if (resp.trim().equals("true"))
                                    gooddi(getResources().getString(R.string.emailGood));
                                else
                                    di("שליחה נכשלה. נסה שוב!");
                            }
                        });
                    } else {
                        contactActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendButton.setEnabled(true);
                                pdialog.cancel();
                                di("שליחה הופסקה בשל תקלה. נסה שוב!");
                            }
                        });
                    }
                } catch (IOException e) {
                    contactActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendButton.setEnabled(true);
                            pdialog.cancel();
                            di("שליחה הופסקה בשל תקלה. נסה שוב!");
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
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.emailA:
                    if (emailA.getText().toString().isEmpty())
                        emailATIL.setError("לא רשמת כתובת אימייל שלך!");
                    else
                        emailATIL.setErrorEnabled(false);
                    break;
                case R.id.emailN:
                    if(emailN.getText().toString().isEmpty())
                        emailNTIL.setError("לא רשמת מה שמך!");
                    else
                        emailNTIL.setErrorEnabled(false);
                    break;
                case R.id.emailS:
                    if (emailS.getText().toString().isEmpty())
                        emailSTIL.setError("לא רשמת מה נושא האימייל!");
                    else
                        emailSTIL.setErrorEnabled(false);
                    break;
                case R.id.emailT:
                    if(emailT.getText().toString().isEmpty())
                        emailTTIL.setError("לא רשמת את תוכן ההודעה");
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
        String alert = getApplicationContext().getResources().getString(R.string.emailAlert) + "\n\n";
        String sender = alert + getApplicationContext().getResources().getString(R.string.sender) + emailN + " - " + emailA + "\n";
        String sub = sender + getApplicationContext().getResources().getString(R.string.subject) +  emailS;
        int sendI = getApplicationContext().getResources().getString(R.string.sender).length();
        int subI = getApplicationContext().getResources().getString(R.string.subject).length();
        int msgI = getApplicationContext().getResources().getString(R.string.msg).length();
        SpannableString subj = new SpannableString(sub + "\n" + getApplicationContext().getResources().getString(R.string.msg) + "\n" + emailT);
        subj.setSpan(new ForegroundColorSpan(Color.RED), 0, alert.length(), 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), alert.length(), alert.length()+sendI, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sender.length(), sender.length()+subI, 0);
        subj.setSpan(new ForegroundColorSpan(Color.rgb(35, 139, 173)), sub.length(), sub.length()+msgI, 0);
        subj.setSpan(myTypeface, 0, subj.length(), 0);
        final AlertDialog builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle).setTitle(getApplicationContext().getResources().getString(R.string.suc)).setMessage(subj)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendButton.setEnabled(false);
                        pdialog.show();
                        sendMail(emailA, emailN, emailS, emailT);
                        dialog.cancel();
                    }
                }).create();
        TextView textView = (TextView) builder.findViewById(android.R.id.message);
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
