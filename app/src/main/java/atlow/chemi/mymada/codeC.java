package atlow.chemi.mymada;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import atlow.chemi.mymada.helpers.LocalHelper;
import okhttp3.OkHttpClient;

public class codeC extends AppCompatActivity {
    private Button send;
    private DatabaseReference mFirebaseDatabase;
    private ProgressDialog pdialog;
    private static final String BASE_URL = "http://atlowapps.tzevethatzolah.org/codeC.php";
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_code_c);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //start
        SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
        Log.d("KEYERG", "the key is: " + preferences.getString("firebaseGenratedKey", ""));
        //end
        boolean toCode = getIntent().getBooleanExtra("toCode", false);
        final String code = getIntent().getStringExtra("code");
        String fireKey = getIntent().getStringExtra("fireKey");
        pdialog = new ProgressDialog(codeC.this);
        pdialog.setIndeterminate(true);
        pdialog.setMessage("טוען. אנא המתן...");
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setCancelable(false);
        final EditText code1 = (EditText)findViewById(R.id.code1);
        final EditText code2 = (EditText)findViewById(R.id.code2);
        final EditText code3 = (EditText)findViewById(R.id.code3);
        final EditText code4 = (EditText)findViewById(R.id.code4);
        send = (Button)findViewById(R.id.okBBB);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(code1.getText().length() == 0 || code2.getText().length() == 0 || code3.getText().length() == 0 || code4.getText().length() == 0)   {
                    di("לא הזנת את הקוד במלואו!");
                }else   {
                    SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
                    String MemoKey = preferences.getString("firebaseGenratedKey", "");
                    newSendCode(code1.getText().toString() + code2.getText().toString() + code3.getText().toString() + code4.getText().toString(), MemoKey);
                }
            }
        });
        if(toCode)  {
            newSendCode(code, fireKey);
        }
        code1.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    code1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    code2.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        code2.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    code2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    code3.requestFocus();
                }
                if (count == 0)
                    code1.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable arg0) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}
        });
        code3.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    code3.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    code4.requestFocus();
                }
                if (count == 0)
                    code2.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable arg0) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start,int count, int after) {}
        });
        code4.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0)
                    code3.requestFocus();
            }
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
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
    private void di(String err)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        TextView textView = alert.findViewById(android.R.id.message);
        final Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        if (textView!= null)
            textView.setTypeface(myTypeface);
        alert.setOnShowListener((dialogInterface) -> {
                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                canc.setTypeface(myTypeface);
        });
        alert.show();
    }
    private void gooddi(String good)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(good).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> {
                        Intent i2 = new Intent(getApplicationContext(), MokedPage.class);
                        i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(i2);
                        dialog.cancel();
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public void newSendCode(final String myCode, String key)  {
        if (key.length() <= 0) {
            SharedPreferences preferences = getApplication().getSharedPreferences("Settings", 0);
            key = preferences.getString("firebaseGenratedKey", "");
        }
        if (key.trim().length() == 0) {
            di(getString(R.string.codeNotExist));
        }
        else {
            pdialog.show();
            mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
            mFirebaseDatabase = mFirebaseDatabase.child("codes");
            mFirebaseDatabase = mFirebaseDatabase.child(key);
            mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String reason = dataSnapshot.child("reason").getValue(String.class);
                    String code = dataSnapshot.child("code").getValue(String.class);
                    String token = dataSnapshot.child("token").getValue(String.class);
                    boolean allowed = false;
                    boolean used = true;
                    try {
                        allowed = dataSnapshot.child("allowed").getValue(Boolean.class);
                    } catch (NullPointerException e) {
                        Crashlytics.log(e.getMessage());
                    }
                    try {
                        used = dataSnapshot.child("used").getValue(Boolean.class);
                    } catch (NullPointerException e) {
                        Crashlytics.log(e.getMessage());
                    }
                    if (myCode.equals(code)) {
                        if (used)
                            di(getString(R.string.codeUsed, myCode));
                        else if (!allowed)
                            di(getString(R.string.codeNotAllowed, myCode));
                        else {
                            mFirebaseDatabase.child("used").setValue(true);
                            gooddi(getResources().getString(R.string.codeGood));
                            SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
                            SharedPreferences.Editor sp1 = sharedPreferences.edit();
                            sp1.putBoolean("realM", true);
                            sp1.apply();
                        }
                    } else {
                        di(getString(R.string.codeBad));
                    }
                    pdialog.cancel();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pdialog.cancel();
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}