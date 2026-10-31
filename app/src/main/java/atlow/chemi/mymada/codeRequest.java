package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class codeRequest extends AppCompatActivity {
    private ProgressDialog pdialog;
    private EditText codeA, codeN, codeT;
    private TextInputLayout codeATIL, codeNTIL, codeTTIL;
    private String token;
    private DatabaseReference mFirebaseDatabase;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean isMove = false, isConnected = true;
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
        builder.setTitle(getResources().getString(R.string.plsNote)).setMessage(getResources().getString(R.string.plsNoteT)).setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
        MyFirebaseMessagingService.getToken(getApplicationContext());
        token = getSharedPreferences("Settings", 0).getString("fcm", "empty");
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
        Drawable img = VectorDrawableCompat.create(getResources(), R.drawable.ic_send, null);
        sendCR.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        sendCR.setOnTouchListener((v, event) -> {
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
        });
        sendCR.setOnClickListener(view -> {
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
//            Crashlytics.log(e.getMessage());
        }
    }
    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public void sendMailNew(String codeA, String codeN, String codeT) {
        String randomLetters = "0123456789";
        StringBuilder res = new StringBuilder();
        for (int n = 0; n < 4; n++)
            res.append(randomLetters.charAt(new Random().nextInt(randomLetters.length())));
        long tsLong = System.currentTimeMillis()/1000;
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
            codeReq.put("code", res.toString());
            codeReq.put("token", token);
            codeReq.put("allowed", false);
            codeReq.put("used", false);
            codeReq.put("time", ""+ tsLong);
            mFirebaseDatabase = mFirebaseDatabase.child("codes");
            mFirebaseDatabase = mFirebaseDatabase.push();
            mFirebaseDatabase.setValue(codeReq).addOnSuccessListener((aVoid) -> {
                    try {
                        gooddi(getResources().getString(R.string.requestGood));
                    } catch (WindowManager.BadTokenException e) {
//                        Crashlytics.log("gooddi called after window closed.");
//                        Crashlytics.logException(e);
                    }
                    pdialog.cancel();
                    SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("firebaseGenratedKey", mFirebaseDatabase.getKey());
                    editor.apply();
            }).addOnFailureListener(e -> {
                sendCR.setEnabled(false);
                pdialog.cancel();
                di(getResources().getString(R.string.emailBad));
            });
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
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false).setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void gooddi(String good)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(good).setCancelable(false).setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
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
                .setNegativeButton(getResources().getString(R.string.ret), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getResources().getString(R.string.ok), (dialog, id) -> {
                    sendCR.setEnabled(false);
                    pdialog = new ProgressDialog(codeRequest.this);
                    pdialog.setIndeterminate(true);
                    pdialog.setMessage(getResources().getString(R.string.load));
                    pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pdialog.setCancelable(false);
                    pdialog.show();
                    sendMailNew(codeA, codeN, codeT);
                    dialog.cancel();
                }).create();
        TextView textView = builder.findViewById(android.R.id.message);
        final Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        if (textView!= null)
            textView.setTypeface(myTypeface);
        builder.setOnShowListener(dialogInterface -> {
            Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
            canc.setTypeface(myTypeface);
            ok.setTypeface(myTypeface);
            canc.setTextColor(codeRequest.this.getResources().getColor(R.color.OldPrimary));
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
                atlow.chemi.mymada.settings.DetectionControls.toggle(this, item);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsPage.class));
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
