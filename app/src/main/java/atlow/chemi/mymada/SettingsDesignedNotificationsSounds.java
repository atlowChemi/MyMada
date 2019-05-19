package atlow.chemi.mymada;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

public class SettingsDesignedNotificationsSounds extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sp;
    //T
    private ConstraintLayout NappT_Pref, NrinT_Pref;
    private TextView NvibT_Desc, NsouT_Desc, NappT_Desc;
    private SwitchCompat NvibT, NsouT, NappT;
    //T1
    private ConstraintLayout NvibT1_Pref, NsouT1_Pref, NappT1_Pref, NrinT1_Pref;
    private TextView findT1_Desc, NvibT1_Desc, NsouT1_Desc, NappT1_Desc;
    private SwitchCompat findT1, NvibT1, NsouT1, NappT1;
    //T2
    private ConstraintLayout NvibT2_Pref, NsouT2_Pref, NappT2_Pref, NrinT2_Pref;
    private TextView findT2_Desc, NvibT2_Desc, NsouT2_Desc, NappT2_Desc;
    private SwitchCompat findT2, NvibT2, NsouT2, NappT2;
    //T3
    private ConstraintLayout NvibT3_Pref, NsouT3_Pref, NappT3_Pref, NrinT3_Pref;
    private TextView findT3_Desc, NvibT3_Desc, NsouT3_Desc, NappT3_Desc;
    private SwitchCompat findT3, NvibT3, NsouT3, NappT3;
    //T4
    private ConstraintLayout NappT4_Pref, NrinT4_Pref;
    private TextView NvibT4_Desc, NsouT4_Desc, NappT4_Desc;
    private SwitchCompat NvibT4, NsouT4, NappT4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_designed_notifications_sounds);
        resetTitle();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set view of T's we Might hide soon.
        ConstraintLayout trueCut = findViewById(R.id.true_cut);
        ConstraintLayout falseCut = findViewById(R.id.false_cut);
        ConstraintLayout nonCritical = findViewById(R.id.non_critical);

        //get Shared Preferences and decide what to show and hide
        sp = getSharedPreferences("Settings", 0);
        boolean cutAB = sp.getBoolean("cut", false);
        boolean t6B = sp.getBoolean("callT6", true);
        boolean findT4B = sp.getBoolean("callT4", true);
        if (cutAB) {
            trueCut.setVisibility(View.VISIBLE);
            falseCut.setVisibility(View.GONE);
            //set all views relevant
            /*T1*/
            ConstraintLayout findT1_Pref = findViewById(R.id.findT1_pref);
            NvibT1_Pref = findViewById(R.id.NvibT1_pref);
            NsouT1_Pref = findViewById(R.id.NsouT1_pref);
            NappT1_Pref = findViewById(R.id.NappT1_pref);
            NrinT1_Pref = findViewById(R.id.NrinT1_pref);
            findT1_Desc = findViewById(R.id.findT1_Desc);
            NvibT1_Desc = findViewById(R.id.NvibT1_Desc);
            NsouT1_Desc = findViewById(R.id.NsouT1_Desc);
            NappT1_Desc = findViewById(R.id.NappT1_Desc);
            findT1 = findViewById(R.id.findT1);
            NvibT1 = findViewById(R.id.NvibT1);
            NsouT1 = findViewById(R.id.NsouT1);
            NappT1 = findViewById(R.id.NappT1);
            /*T2*/
            ConstraintLayout findT2_Pref = findViewById(R.id.findT2_pref);
            NvibT2_Pref = findViewById(R.id.NvibT2_pref);
            NsouT2_Pref = findViewById(R.id.NsouT2_pref);
            NappT2_Pref = findViewById(R.id.NappT2_pref);
            NrinT2_Pref = findViewById(R.id.NrinT2_pref);
            findT2_Desc = findViewById(R.id.findT2_Desc);
            NvibT2_Desc = findViewById(R.id.NvibT2_Desc);
            NsouT2_Desc = findViewById(R.id.NsouT2_Desc);
            NappT2_Desc = findViewById(R.id.NappT2_Desc);
            findT2 = findViewById(R.id.findT2);
            NvibT2 = findViewById(R.id.NvibT2);
            NsouT2 = findViewById(R.id.NsouT2);
            NappT2 = findViewById(R.id.NappT2);
            /*T3*/
            ConstraintLayout findT3_Pref = findViewById(R.id.findT3_pref);
            NvibT3_Pref = findViewById(R.id.NvibT3_pref);
            NsouT3_Pref = findViewById(R.id.NsouT3_pref);
            NappT3_Pref = findViewById(R.id.NappT3_pref);
            NrinT3_Pref = findViewById(R.id.NrinT3_pref);
            findT3_Desc = findViewById(R.id.findT3_Desc);
            NvibT3_Desc = findViewById(R.id.NvibT3_Desc);
            NsouT3_Desc = findViewById(R.id.NsouT3_Desc);
            NappT3_Desc = findViewById(R.id.NappT3_Desc);
            findT3 = findViewById(R.id.findT3);
            NvibT3 = findViewById(R.id.NvibT3);
            NsouT3 = findViewById(R.id.NsouT3);
            NappT3 = findViewById(R.id.NappT3);
            //set click to work
            /*T1*/
            findT1_Pref.setOnClickListener(this);
            NvibT1_Pref.setOnClickListener(this);
            NsouT1_Pref.setOnClickListener(this);
            NappT1_Pref.setOnClickListener(this);
            NrinT1_Pref.setOnClickListener(this);
            /*T2*/
            findT2_Pref.setOnClickListener(this);
            NvibT2_Pref.setOnClickListener(this);
            NsouT2_Pref.setOnClickListener(this);
            NappT2_Pref.setOnClickListener(this);
            NrinT2_Pref.setOnClickListener(this);
            /*T3*/
            findT3_Pref.setOnClickListener(this);
            NvibT3_Pref.setOnClickListener(this);
            NsouT3_Pref.setOnClickListener(this);
            NappT3_Pref.setOnClickListener(this);
            NrinT3_Pref.setOnClickListener(this);
            //get shared preferences
            /*T1*/
            boolean findT1B = sp.getBoolean("callT1", false);
            boolean NvibT1B = sp.getBoolean("NvibT1", false);
            boolean NsouT1B = sp.getBoolean("NsouT1", false);
            boolean NappT1B = sp.getBoolean("NappT1", false);
            /*T2*/
            boolean findT2B = sp.getBoolean("callT2", false);
            boolean NvibT2B = sp.getBoolean("NvibT2", false);
            boolean NsouT2B = sp.getBoolean("NsouT2", false);
            boolean NappT2B = sp.getBoolean("NappT2", false);
            /*T2*/
            boolean findT3B = sp.getBoolean("callT3", false);
            boolean NvibT3B = sp.getBoolean("NvibT3", false);
            boolean NsouT3B = sp.getBoolean("NsouT3", false);
            boolean NappT3B = sp.getBoolean("NappT3", false);
            //set the switches
            /*T1*/
            setTrueOrFalse(findT1B, findT1_Pref.getId());
            setTrueOrFalse(NvibT1B, NvibT1_Pref.getId());
            setTrueOrFalse(NsouT1B, NsouT1_Pref.getId());
            setTrueOrFalse(NappT1B, NappT1_Pref.getId());
            /*T2*/
            setTrueOrFalse(findT2B, findT2_Pref.getId());
            setTrueOrFalse(NvibT2B, NvibT2_Pref.getId());
            setTrueOrFalse(NsouT2B, NsouT2_Pref.getId());
            setTrueOrFalse(NappT2B, NappT2_Pref.getId());
            /*T2*/
            setTrueOrFalse(findT3B, findT3_Pref.getId());
            setTrueOrFalse(NvibT3B, NvibT3_Pref.getId());
            setTrueOrFalse(NsouT3B, NsouT3_Pref.getId());
            setTrueOrFalse(NappT3B, NappT3_Pref.getId());
        } else {
            trueCut.setVisibility(View.GONE);
            falseCut.setVisibility(View.VISIBLE);
            //set all views relevant
            ConstraintLayout NvibT_Pref = findViewById(R.id.NvibT_pref);
            ConstraintLayout NsouT_Pref = findViewById(R.id.NsouT_pref);
            NappT_Pref = findViewById(R.id.NappT_pref);
            NrinT_Pref = findViewById(R.id.NrinT_pref);
            NvibT_Desc = findViewById(R.id.NvibT_Desc);
            NsouT_Desc = findViewById(R.id.NsouT_Desc);
            NappT_Desc = findViewById(R.id.NappT_Desc);
            NvibT = findViewById(R.id.NvibT);
            NsouT = findViewById(R.id.NsouT);
            NappT = findViewById(R.id.NappT);
            //set click to work
            NvibT_Pref.setOnClickListener(this);
            NsouT_Pref.setOnClickListener(this);
            NappT_Pref.setOnClickListener(this);
            NrinT_Pref.setOnClickListener(this);
            //get shared preferences
            boolean NvibTB = sp.getBoolean("Nvib", false);
            boolean NsouTB = sp.getBoolean("Nsou", false);
            boolean NappTB = sp.getBoolean("Napp", false);
            //set the switches
            setTrueOrFalse(NvibTB, NvibT_Pref.getId());
            setTrueOrFalse(NsouTB, NsouT_Pref.getId());
            setTrueOrFalse(NappTB, NappT_Pref.getId());
        }
        if (!t6B && !findT4B) {
            nonCritical.setVisibility(View.GONE);
        } else {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nonCritical.getLayoutParams();
            params.topToBottom = cutAB ? trueCut.getId() : falseCut.getId();
            nonCritical.requestLayout();
            //set all views relevant
            ConstraintLayout NvibT4_Pref = findViewById(R.id.NvibT4_pref);
            ConstraintLayout NsouT4_Pref = findViewById(R.id.NsouT4_pref);
            NappT4_Pref = findViewById(R.id.NappT4_pref);
            NrinT4_Pref = findViewById(R.id.NrinT4_pref);
            NvibT4_Desc = findViewById(R.id.NvibT4_Desc);
            NsouT4_Desc = findViewById(R.id.NsouT4_Desc);
            NappT4_Desc = findViewById(R.id.NappT4_Desc);
            NvibT4 = findViewById(R.id.NvibT4);
            NsouT4 = findViewById(R.id.NsouT4);
            NappT4 = findViewById(R.id.NappT4);
            //set click to work
            NvibT4_Pref.setOnClickListener(this);
            NsouT4_Pref.setOnClickListener(this);
            NappT4_Pref.setOnClickListener(this);
            NrinT4_Pref.setOnClickListener(this);
            //get shared preferences
            boolean NvibT4B = sp.getBoolean("NvibT4", false);
            boolean NsouT4B = sp.getBoolean("NsouT4", false);
            boolean NappT4B = sp.getBoolean("NappT4", false);
            //set the switches
            setTrueOrFalse(NvibT4B, NvibT4_Pref.getId());
            setTrueOrFalse(NsouT4B, NsouT4_Pref.getId());
            setTrueOrFalse(NappT4B, NappT4_Pref.getId());
        }
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View view) {
        SwitchCompat sc = null;
        String desc = null;
        TextView tv = null;
        boolean newB = false;
        SharedPreferences.Editor editor = sp.edit();
        switch (view.getId()) {
            /*T***/
            case R.id.NvibT_pref:
                sc = NvibT;
                desc = NvibT.isChecked() ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("Nvib", !sc.isChecked());
                tv = NvibT_Desc;
                break;
            case R.id.NsouT_pref:
                sc = NsouT;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("Nsou", newB);
                NrinT_Pref.setClickable(newB && NappT.isChecked());
                NrinT_Pref.setAlpha(newB && NappT.isChecked() ? 1 : 0.4f);
                NappT_Pref.setClickable(newB);
                NappT_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NsouT_Desc;
                break;
            case R.id.NappT_pref:
                sc = NappT;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("Napp", newB);
                NrinT_Pref.setClickable(newB);
                NrinT_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NappT_Desc;
                break;
            case R.id.NrinT_pref:
                sc = null;
                ringtoner(0);
                break;
            /*T1***/
            case R.id.findT1_pref:
                sc = findT1;
                newB = !sc.isChecked();
                desc = findT1.isChecked() ? getResources().getString(R.string.findT1_off) : getResources().getString(R.string.findT1_on);
                editor.putBoolean("callT1", !sc.isChecked());
                NvibT1_Pref.setClickable(newB);
                NvibT1_Pref.setAlpha(newB ? 1 : 0.4f);
                NsouT1_Pref.setClickable(newB);
                NsouT1_Pref.setAlpha(newB ? 1 : 0.4f);
                NappT1_Pref.setClickable(newB && NsouT1.isChecked());
                NappT1_Pref.setAlpha(newB && NsouT1.isChecked() ? 1 : 0.4f);
                NrinT1_Pref.setClickable(newB && NappT1.isChecked());
                NrinT1_Pref.setAlpha(newB && NappT1.isChecked() ? 1 : 0.4f);
                tv = findT1_Desc;
                break;
            case R.id.NvibT1_pref:
                sc = NvibT1;
                desc = NvibT1.isChecked() ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT1", !sc.isChecked());
                tv = NvibT1_Desc;
                break;
            case R.id.NsouT1_pref:
                sc = NsouT1;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT1", newB);
                NrinT1_Pref.setClickable(newB && NappT1.isChecked());
                NrinT1_Pref.setAlpha(newB && NappT1.isChecked() ? 1 : 0.4f);
                NappT1_Pref.setClickable(newB);
                NappT1_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NsouT1_Desc;
                break;
            case R.id.NappT1_pref:
                sc = NappT1;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT1", newB);
                NrinT1_Pref.setClickable(newB);
                NrinT1_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NappT1_Desc;
                break;
            case R.id.NrinT1_pref:
                sc = null;
                ringtoner(1);
                break;
            /*T2***/
            case R.id.findT2_pref:
                sc = findT2;
                newB = !sc.isChecked();
                desc = findT2.isChecked() ? getResources().getString(R.string.findT2_off) : getResources().getString(R.string.findT2_on);
                editor.putBoolean("callT2", !sc.isChecked());
                NvibT2_Pref.setClickable(newB);
                NvibT2_Pref.setAlpha(newB ? 1 : 0.4f);
                NsouT2_Pref.setClickable(newB);
                NsouT2_Pref.setAlpha(newB ? 1 : 0.4f);
                NappT2_Pref.setClickable(newB && NsouT2.isChecked());
                NappT2_Pref.setAlpha(newB && NsouT2.isChecked() ? 1 : 0.4f);
                NrinT2_Pref.setClickable(newB && NappT2.isChecked());
                NrinT2_Pref.setAlpha(newB && NappT2.isChecked() ? 1 : 0.4f);
                tv = findT2_Desc;
                break;
            case R.id.NvibT2_pref:
                sc = NvibT2;
                desc = NvibT2.isChecked() ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT2", !sc.isChecked());
                tv = NvibT2_Desc;
                break;
            case R.id.NsouT2_pref:
                sc = NsouT2;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT2", newB);
                NrinT2_Pref.setClickable(newB && NappT2.isChecked());
                NrinT2_Pref.setAlpha(newB && NappT2.isChecked() ? 1 : 0.4f);
                NappT2_Pref.setClickable(newB);
                NappT2_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NsouT2_Desc;
                break;
            case R.id.NappT2_pref:
                sc = NappT2;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT2", newB);
                NrinT2_Pref.setClickable(newB);
                NrinT2_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NappT2_Desc;
                break;
            case R.id.NrinT2_pref:
                sc = null;
                ringtoner(2);
                break;
            /*T3***/
            case R.id.findT3_pref:
                sc = findT3;
                newB = !sc.isChecked();
                desc = findT3.isChecked() ? getResources().getString(R.string.findT3_off) : getResources().getString(R.string.findT3_on);
                editor.putBoolean("callT3", !sc.isChecked());
                NvibT3_Pref.setClickable(newB);
                NvibT3_Pref.setAlpha(newB ? 1 : 0.4f);
                NsouT3_Pref.setClickable(newB);
                NsouT3_Pref.setAlpha(newB ? 1 : 0.4f);
                NappT3_Pref.setClickable(newB && NsouT3.isChecked());
                NappT3_Pref.setAlpha(newB && NsouT3.isChecked() ? 1 : 0.4f);
                NrinT3_Pref.setClickable(newB && NappT3.isChecked());
                NrinT3_Pref.setAlpha(newB && NappT3.isChecked() ? 1 : 0.4f);
                tv = findT3_Desc;
                break;
            case R.id.NvibT3_pref:
                sc = NvibT3;
                desc = NvibT3.isChecked() ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT3", !sc.isChecked());
                tv = NvibT3_Desc;
                break;
            case R.id.NsouT3_pref:
                sc = NsouT3;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT3", newB);
                NrinT3_Pref.setClickable(newB && NappT3.isChecked());
                NrinT3_Pref.setAlpha(newB && NappT3.isChecked() ? 1 : 0.4f);
                NappT3_Pref.setClickable(newB);
                NappT3_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NsouT3_Desc;
                break;
            case R.id.NappT3_pref:
                sc = NappT3;
                newB = !sc.isChecked();
                desc = !newB ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT3", newB);
                NrinT3_Pref.setClickable(newB);
                NrinT3_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NappT3_Desc;
                break;
            case R.id.NrinT3_pref:
                sc = null;
                ringtoner(3);
                break;
            /*T4***/
            case R.id.NvibT4_pref:
                sc = NvibT4;
                desc = NvibT4.isChecked() ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT4", !sc.isChecked());
                tv = NvibT4_Desc;
                break;
            case R.id.NsouT4_pref:
                sc = NsouT4;
                newB = !sc.isChecked();
                desc = NsouT4.isChecked() ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT4", newB);
                NrinT4_Pref.setClickable(newB && NappT4.isChecked());
                NrinT4_Pref.setAlpha(newB && NappT4.isChecked() ? 1 : 0.4f);
                NappT4_Pref.setClickable(newB);
                NappT4_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NsouT4_Desc;
                break;
            case R.id.NappT4_pref:
                sc = NappT4;
                newB = !sc.isChecked();
                desc = NappT4.isChecked() ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT4", newB);
                NrinT4_Pref.setClickable(newB);
                NrinT4_Pref.setAlpha(newB ? 1 : 0.4f);
                tv = NappT4_Desc;
                break;
            case R.id.NrinT4_pref:
                sc = null;
                ringtoner(4);
                break;
        }
        editor.apply();
        if (sc != null && tv != null && !desc.isEmpty()) {
            sc.setChecked(!sc.isChecked());
            tv.setText(desc);
        }
    }
    private void setTrueOrFalse(boolean set, int id) {
        SwitchCompat sc = null;
        String desc = null;
        TextView tv = null;
        SharedPreferences.Editor editor = sp.edit();
        switch (id) {
            /*T***/
            case R.id.NvibT_pref:
                sc = NvibT;
                desc = !set ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("Nvib", set);
                tv = NvibT_Desc;
                break;
            case R.id.NsouT_pref:
                sc = NsouT;
                desc = !set ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("Nsou", set);
                NrinT_Pref.setClickable(set && NappT.isChecked());
                NrinT_Pref.setAlpha(set && NappT.isChecked() ? 1 : 0.4f);
                NappT_Pref.setClickable(set);
                NappT_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NsouT_Desc;
                break;
            case R.id.NappT_pref:
                sc = NappT;
                desc = !set ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("Napp", set);
                NrinT_Pref.setClickable(set);
                NrinT_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NappT_Desc;
                break;
            /*T1***/
            case R.id.findT1_pref:
                sc = findT1;
                desc = !set ? getResources().getString(R.string.findT1_off) : getResources().getString(R.string.findT1_on);
                editor.putBoolean("callT1", set);
                tv = findT1_Desc;
                NvibT1_Pref.setClickable(set);
                NvibT1_Pref.setAlpha(set ? 1 : 0.4f);
                NsouT1_Pref.setClickable(set);
                NsouT1_Pref.setAlpha(set ? 1 : 0.4f);
                NappT1_Pref.setClickable(set);
                NappT1_Pref.setAlpha(set ? 1 : 0.4f);
                NrinT1_Pref.setClickable(set && NappT1.isChecked());
                NrinT1_Pref.setAlpha(set && NappT1.isChecked() ? 1 : 0.4f);
                break;
            case R.id.NvibT1_pref:
                sc = NvibT1;
                desc = !set ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT1", set);
                tv = NvibT1_Desc;
                break;
            case R.id.NsouT1_pref:
                sc = NsouT1;
                desc = !set ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT1", set);
                NrinT1_Pref.setClickable(set && NappT1.isChecked());
                NrinT1_Pref.setAlpha(set && NappT1.isChecked() ? 1 : 0.4f);
                NappT1_Pref.setClickable(set && findT1.isChecked());
                NappT1_Pref.setAlpha(set && findT1.isChecked() ? 1 : 0.4f);
                tv = NsouT1_Desc;
                break;
            case R.id.NappT1_pref:
                sc = NappT1;
                desc = !set ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT1", set);
                NrinT1_Pref.setClickable(set);
                NrinT1_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NappT1_Desc;
                break;
            /*T2***/
            case R.id.findT2_pref:
                sc = findT2;
                desc = !set ? getResources().getString(R.string.findT2_off) : getResources().getString(R.string.findT2_on);
                editor.putBoolean("callT2", set);
                tv = findT2_Desc;
                NvibT2_Pref.setClickable(set);
                NvibT2_Pref.setAlpha(set ? 1 : 0.4f);
                NsouT2_Pref.setClickable(set);
                NsouT2_Pref.setAlpha(set ? 1 : 0.4f);
                NappT2_Pref.setClickable(set);
                NappT2_Pref.setAlpha(set ? 1 : 0.4f);
                NrinT2_Pref.setClickable(set && NappT2.isChecked());
                NrinT2_Pref.setAlpha(set && NappT2.isChecked() ? 1 : 0.4f);
                break;
            case R.id.NvibT2_pref:
                sc = NvibT2;
                desc = !set ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT2", set);
                tv = NvibT2_Desc;
                break;
            case R.id.NsouT2_pref:
                sc = NsouT2;
                desc = !set ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT2", set);
                NrinT2_Pref.setClickable(set && NappT2.isChecked());
                NrinT2_Pref.setAlpha(set && NappT2.isChecked() ? 1 : 0.4f);
                NappT2_Pref.setClickable(set && findT2.isChecked());
                NappT2_Pref.setAlpha(set && findT2.isChecked() ? 1 : 0.4f);
                tv = NsouT2_Desc;
                break;
            case R.id.NappT2_pref:
                sc = NappT2;
                desc = !set ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT2", set);
                NrinT2_Pref.setClickable(set);
                NrinT2_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NappT2_Desc;
                break;
            /*T3***/
            case R.id.findT3_pref:
                sc = findT3;
                desc = !set ? getResources().getString(R.string.findT3_off) : getResources().getString(R.string.findT3_on);
                editor.putBoolean("callT3", set);
                tv = findT3_Desc;
                NvibT3_Pref.setClickable(set);
                NvibT3_Pref.setAlpha(set ? 1 : 0.4f);
                NsouT3_Pref.setClickable(set);
                NsouT3_Pref.setAlpha(set ? 1 : 0.4f);
                NappT3_Pref.setClickable(set);
                NappT3_Pref.setAlpha(set ? 1 : 0.4f);
                NrinT3_Pref.setClickable(set && NappT3.isChecked());
                NrinT3_Pref.setAlpha(set && NappT3.isChecked() ? 1 : 0.4f);
                break;
            case R.id.NvibT3_pref:
                sc = NvibT3;
                desc = !set ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT3", set);
                tv = NvibT3_Desc;
                break;
            case R.id.NsouT3_pref:
                sc = NsouT3;
                desc = !set ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT3", set);
                NrinT3_Pref.setClickable(set && NappT3.isChecked());
                NrinT3_Pref.setAlpha(set && NappT3.isChecked() ? 1 : 0.4f);
                NappT3_Pref.setClickable(set && findT3.isChecked());
                NappT3_Pref.setAlpha(set && findT3.isChecked() ? 1 : 0.4f);
                tv = NsouT3_Desc;
                break;
            case R.id.NappT3_pref:
                sc = NappT3;
                desc = !set ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT3", set);
                NrinT3_Pref.setClickable(set);
                NrinT3_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NappT3_Desc;
                break;
            /*T4***/
            case R.id.NvibT4_pref:
                sc = NvibT4;
                desc = !set ? getResources().getString(R.string.pref_description_vibrate_off) : getResources().getString(R.string.pref_description_vibrate_on);
                editor.putBoolean("NvibT4", set);
                tv = NvibT4_Desc;
                break;
            case R.id.NsouT4_pref:
                sc = NsouT4;
                desc = !set ? getResources().getString(R.string.pref_description_ringtone_off) : getResources().getString(R.string.pref_description_ringtone_on);
                editor.putBoolean("NsouT4", set);
                NrinT4_Pref.setClickable(set && NappT4.isChecked());
                NrinT4_Pref.setAlpha(set && NappT4.isChecked() ? 1 : 0.4f);
                NappT4_Pref.setClickable(set);
                NappT4_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NsouT4_Desc;
                break;
            case R.id.NappT4_pref:
                sc = NappT4;
                desc = !set ? getResources().getString(R.string.pref_description_app_off) : getResources().getString(R.string.pref_description_app_on);
                editor.putBoolean("NappT4", set);
                NrinT4_Pref.setClickable(set);
                NrinT4_Pref.setAlpha(set ? 1 : 0.4f);
                tv = NappT4_Desc;
                break;
        }
        editor.apply();
        if (sc != null && tv != null && !desc.isEmpty()) {
            sc.setChecked(set);
            tv.setText(desc);
        }
    }
    private void ringtoner(int T) {
        String uri = T > 0 ? "NuRiT" + T : "NuRi";
        Toast.makeText(this, uri, Toast.LENGTH_SHORT).show();
        Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pick_sound));
        picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        picker.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.parse(sp.getString(uri, "")));
        SettingsDesignedNotificationsSounds.this.startActivityForResult(picker, T);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri ringUri = null;
        String f;
        if (resultCode == RESULT_OK) {
            ringUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            f = ringUri.toString();
            switch (requestCode) {
                case 0:
                    Toast.makeText(this, f, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(this, f, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(this, f, Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this, f, Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(this, f, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}