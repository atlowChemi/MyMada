package atlow.chemi.mymada.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;

import atlow.chemi.mymada.R;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;

public class SettingsNotificationsSounds extends Fragment implements View.OnClickListener {
    private SharedPreferences sp, permissionStatus;
    private static final int PERMISSION_T = 100, PERMISSION_T1 = 101, PERMISSION_T2 = 102, PERMISSION_T3 = 103, PERMISSION_T4 = 104;
    private String[] permissionsRequired = {android.Manifest.permission.READ_EXTERNAL_STORAGE};
    private int retPer = PERMISSION_T;
    private boolean sentToSettings = false;
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

    public SettingsNotificationsSounds() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.pref_header_sound_and_vib);
            permissionStatus = getActivity().getSharedPreferences("permissionStatus", MODE_PRIVATE);
        }
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_notifications_sounds, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set view of T's we Might hide soon.
        ConstraintLayout trueCut = view.findViewById(R.id.true_cut);
        ConstraintLayout falseCut = view.findViewById(R.id.false_cut);
        ConstraintLayout nonCritical = view.findViewById(R.id.non_critical);

        //get Shared Preferences and decide what to show and hide
        sp = getActivity().getSharedPreferences("Settings", 0);
        boolean cutAB = sp.getBoolean("cut", false);
        boolean t6B = sp.getBoolean("callT6", true);
        boolean findT4B = sp.getBoolean("callT4", true);
        if (cutAB) {
            trueCut.setVisibility(View.VISIBLE);
            falseCut.setVisibility(View.GONE);
            //set all views relevant
            /*T1*/
            ConstraintLayout findT1_Pref = view.findViewById(R.id.findT1_pref);
            NvibT1_Pref = view.findViewById(R.id.NvibT1_pref);
            NsouT1_Pref = view.findViewById(R.id.NsouT1_pref);
            NappT1_Pref = view.findViewById(R.id.NappT1_pref);
            NrinT1_Pref = view.findViewById(R.id.NrinT1_pref);
            findT1_Desc = view.findViewById(R.id.findT1_Desc);
            NvibT1_Desc = view.findViewById(R.id.NvibT1_Desc);
            NsouT1_Desc = view.findViewById(R.id.NsouT1_Desc);
            NappT1_Desc = view.findViewById(R.id.NappT1_Desc);
            findT1 = view.findViewById(R.id.findT1);
            NvibT1 = view.findViewById(R.id.NvibT1);
            NsouT1 = view.findViewById(R.id.NsouT1);
            NappT1 = view.findViewById(R.id.NappT1);
            /*T2*/
            ConstraintLayout findT2_Pref = view.findViewById(R.id.findT2_pref);
            NvibT2_Pref = view.findViewById(R.id.NvibT2_pref);
            NsouT2_Pref = view.findViewById(R.id.NsouT2_pref);
            NappT2_Pref = view.findViewById(R.id.NappT2_pref);
            NrinT2_Pref = view.findViewById(R.id.NrinT2_pref);
            findT2_Desc = view.findViewById(R.id.findT2_Desc);
            NvibT2_Desc = view.findViewById(R.id.NvibT2_Desc);
            NsouT2_Desc = view.findViewById(R.id.NsouT2_Desc);
            NappT2_Desc = view.findViewById(R.id.NappT2_Desc);
            findT2 = view.findViewById(R.id.findT2);
            NvibT2 = view.findViewById(R.id.NvibT2);
            NsouT2 = view.findViewById(R.id.NsouT2);
            NappT2 = view.findViewById(R.id.NappT2);
            /*T3*/
            ConstraintLayout findT3_Pref = view.findViewById(R.id.findT3_pref);
            NvibT3_Pref = view.findViewById(R.id.NvibT3_pref);
            NsouT3_Pref = view.findViewById(R.id.NsouT3_pref);
            NappT3_Pref = view.findViewById(R.id.NappT3_pref);
            NrinT3_Pref = view.findViewById(R.id.NrinT3_pref);
            findT3_Desc = view.findViewById(R.id.findT3_Desc);
            NvibT3_Desc = view.findViewById(R.id.NvibT3_Desc);
            NsouT3_Desc = view.findViewById(R.id.NsouT3_Desc);
            NappT3_Desc = view.findViewById(R.id.NappT3_Desc);
            findT3 = view.findViewById(R.id.findT3);
            NvibT3 = view.findViewById(R.id.NvibT3);
            NsouT3 = view.findViewById(R.id.NsouT3);
            NappT3 = view.findViewById(R.id.NappT3);
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
            boolean findT1B = sp.getBoolean("callT1", true);
            boolean NvibT1B = sp.getBoolean("NvibT1", true);
            boolean NsouT1B = sp.getBoolean("NsouT1", true);
            boolean NappT1B = sp.getBoolean("NappT1", false);
            /*T2*/
            boolean findT2B = sp.getBoolean("callT2", true);
            boolean NvibT2B = sp.getBoolean("NvibT2", true);
            boolean NsouT2B = sp.getBoolean("NsouT2", true);
            boolean NappT2B = sp.getBoolean("NappT2", false);
            /*T2*/
            boolean findT3B = sp.getBoolean("callT3", true);
            boolean NvibT3B = sp.getBoolean("NvibT3", true);
            boolean NsouT3B = sp.getBoolean("NsouT3", true);
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
        }
        else {
            trueCut.setVisibility(View.GONE);
            falseCut.setVisibility(View.VISIBLE);
            //set all views relevant
            ConstraintLayout NvibT_Pref = view.findViewById(R.id.NvibT_pref);
            ConstraintLayout NsouT_Pref = view.findViewById(R.id.NsouT_pref);
            NappT_Pref = view.findViewById(R.id.NappT_pref);
            NrinT_Pref = view.findViewById(R.id.NrinT_pref);
            NvibT_Desc = view.findViewById(R.id.NvibT_Desc);
            NsouT_Desc = view.findViewById(R.id.NsouT_Desc);
            NappT_Desc = view.findViewById(R.id.NappT_Desc);
            NvibT = view.findViewById(R.id.NvibT);
            NsouT = view.findViewById(R.id.NsouT);
            NappT = view.findViewById(R.id.NappT);
            //set click to work
            NvibT_Pref.setOnClickListener(this);
            NsouT_Pref.setOnClickListener(this);
            NappT_Pref.setOnClickListener(this);
            NrinT_Pref.setOnClickListener(this);
            //get shared preferences
            boolean NvibTB = sp.getBoolean("Nvib", true);
            boolean NsouTB = sp.getBoolean("Nsou", true);
            boolean NappTB = sp.getBoolean("Napp", false);
            //set the switches
            setTrueOrFalse(NvibTB, NvibT_Pref.getId());
            setTrueOrFalse(NsouTB, NsouT_Pref.getId());
            setTrueOrFalse(NappTB, NappT_Pref.getId());
        }
        if (!t6B && !findT4B) {
            nonCritical.setVisibility(View.GONE);
        }
        else {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) nonCritical.getLayoutParams();
            params.topToBottom = cutAB ? trueCut.getId() : falseCut.getId();
            nonCritical.requestLayout();
            //set all views relevant
            ConstraintLayout NvibT4_Pref = view.findViewById(R.id.NvibT4_pref);
            ConstraintLayout NsouT4_Pref = view.findViewById(R.id.NsouT4_pref);
            NappT4_Pref = view.findViewById(R.id.NappT4_pref);
            NrinT4_Pref = view.findViewById(R.id.NrinT4_pref);
            NvibT4_Desc = view.findViewById(R.id.NvibT4_Desc);
            NsouT4_Desc = view.findViewById(R.id.NsouT4_Desc);
            NappT4_Desc = view.findViewById(R.id.NappT4_Desc);
            NvibT4 = view.findViewById(R.id.NvibT4);
            NsouT4 = view.findViewById(R.id.NsouT4);
            NappT4 = view.findViewById(R.id.NappT4);
            //set click to work
            NvibT4_Pref.setOnClickListener(this);
            NsouT4_Pref.setOnClickListener(this);
            NappT4_Pref.setOnClickListener(this);
            NrinT4_Pref.setOnClickListener(this);
            //get shared preferences
            boolean NvibT4B = sp.getBoolean("NvibT4", true);
            boolean NsouT4B = sp.getBoolean("NsouT4", true);
            boolean NappT4B = sp.getBoolean("NappT4", false);
            //set the switches
            setTrueOrFalse(NvibT4B, NvibT4_Pref.getId());
            setTrueOrFalse(NsouT4B, NsouT4_Pref.getId());
            setTrueOrFalse(NappT4B, NappT4_Pref.getId());
        }
    }
    @Override
    public void onClick(View view) {
        SwitchCompat sc = null;
        String desc = null;
        TextView tv = null;
        boolean newB;
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
                if (newB)
                    getAllowed(0);
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
                NvibT1_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NsouT1_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NappT1_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NrinT1_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
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
                if (newB)
                    getAllowed(1);
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
                NvibT2_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NsouT2_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NappT2_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NrinT2_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
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
                if (newB)
                    getAllowed(2);
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
                NvibT3_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NsouT3_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NappT3_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
                NrinT3_Pref.setVisibility(newB ? View.VISIBLE : View.GONE);
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
                if (newB)
                    getAllowed(3);
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
                if (newB)
                    getAllowed(4);
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
                NrinT_Pref.setClickable(set && NsouT.isChecked());
                NrinT_Pref.setAlpha(set && NsouT.isChecked() ? 1 : 0.4f);
                tv = NappT_Desc;
                break;
            /*T1***/
            case R.id.findT1_pref:
                sc = findT1;
                desc = !set ? getResources().getString(R.string.findT1_off) : getResources().getString(R.string.findT1_on);
                editor.putBoolean("callT1", set);
                tv = findT1_Desc;
                NvibT1_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NsouT1_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NappT1_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NrinT1_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
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
                NrinT1_Pref.setClickable(set && NsouT1.isChecked());
                NrinT1_Pref.setAlpha(set && NsouT1.isChecked() ? 1 : 0.4f);
                tv = NappT1_Desc;
                break;
            /*T2***/
            case R.id.findT2_pref:
                sc = findT2;
                desc = !set ? getResources().getString(R.string.findT2_off) : getResources().getString(R.string.findT2_on);
                editor.putBoolean("callT2", set);
                tv = findT2_Desc;
                NvibT2_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NsouT2_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NappT2_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NrinT2_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
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
                NrinT2_Pref.setClickable(set && NsouT2.isChecked());
                NrinT2_Pref.setAlpha(set && NsouT2.isChecked() ? 1 : 0.4f);
                tv = NappT2_Desc;
                break;
            /*T3***/
            case R.id.findT3_pref:
                sc = findT3;
                desc = !set ? getResources().getString(R.string.findT3_off) : getResources().getString(R.string.findT3_on);
                editor.putBoolean("callT3", set);
                tv = findT3_Desc;
                NvibT3_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NsouT3_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NappT3_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
                NrinT3_Pref.setVisibility(set ? View.VISIBLE : View.GONE);
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
                NrinT3_Pref.setClickable(set && NsouT3.isChecked());
                NrinT3_Pref.setAlpha(set && NsouT3.isChecked() ? 1 : 0.4f);
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
                NrinT4_Pref.setClickable(set && NsouT4.isChecked());
                NrinT4_Pref.setAlpha(set && NsouT4.isChecked() ? 1 : 0.4f);
                tv = NappT4_Desc;
                break;
        }
        editor.apply();
        if (sc != null && tv != null && !desc.isEmpty()) {
            sc.setChecked(set);
            tv.setText(desc);
        }
    }
    private void getAllowed(int T) {
        retPer = T == 0 ? PERMISSION_T : T == 1 ? PERMISSION_T1 : T == 2 ? PERMISSION_T2 : T == 3 ? PERMISSION_T3 : PERMISSION_T4;
        if(checkSelfPermission(getActivity(), permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permissionsRequired[0])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                builder.setTitle(getActivity().getResources().getString(R.string.permTitle));
                builder.setMessage(getActivity().getResources().getString(R.string.permDesSD));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.permYes), (dialog, which) -> {
                    dialog.cancel();
                    requestPermissions(permissionsRequired, retPer);
                });
                builder.setNegativeButton(getActivity().getString(R.string.ret), (dialog, which) -> {
                    dialog.cancel();
                    ConstraintLayout layout = retPer == 100 ? NappT_Pref : retPer == 101 ? NappT1_Pref :
                            retPer == 102 ? NappT2_Pref : retPer == 103 ? NappT3_Pref : NappT4_Pref;
                    layout.performClick();
                });
                builder.show();
            }
            else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
                builder.setTitle(getActivity().getResources().getString(R.string.permTitle));
                builder.setMessage(getActivity().getResources().getString(R.string.permDesSD));
                builder.setPositiveButton(getActivity().getResources().getString(R.string.permYes), (dialog, which) -> {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, retPer);
                });
                builder.setNegativeButton(getActivity().getString(R.string.ret), (dialog, which) -> {
                    dialog.cancel();
                    ConstraintLayout layout = retPer == 100 ? NappT_Pref : retPer == 101 ? NappT1_Pref :
                            retPer == 102 ? NappT2_Pref : retPer == 103 ? NappT3_Pref : NappT4_Pref;
                    layout.performClick();
                });
                builder.show();
            }
            else {
                //just request the permission
                requestPermissions(permissionsRequired, retPer);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.apply();
        }
    }
    private void ringtoner(int T) {
        if(checkSelfPermission(getActivity(), permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED)
            getAllowed(T);
        else {
            String uri = T > 0 ? "NuRiT" + T : "NuRi";
            Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pick_sound));
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.parse(sp.getString(uri, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL).toString())));
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(sp.getString(uri, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL).toString())));
            startActivityForResult(picker, T);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri ringUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.alert);;
        Log.e("return from permission", "return code is: " + requestCode);
        if (requestCode >99 && requestCode < 105) {
            if (checkSelfPermission(getActivity(), permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                Log.e("return from permission", "return was true");
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
                //ringtoner(requestCode-100);
            }
            else {
                Log.e("return from permission", "return permission denied");
                ConstraintLayout layout = requestCode == 100 ? NappT_Pref : requestCode == 101 ? NappT1_Pref :
                        requestCode == 102 ? NappT2_Pref : requestCode == 103 ? NappT3_Pref : NappT4_Pref;
                layout.performClick();
            }
        }
        if (requestCode < 6 && resultCode == RESULT_OK) {
            try {
                ringUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            }catch (Exception e) {
                Crashlytics.log("didnt prace the ringtone!");
                Crashlytics.logException(e);
            }
            SharedPreferences.Editor spe = sp.edit();
            if (requestCode == 0) {
                spe.putString("NuRi", ringUri.toString());
            }
            else if (requestCode == 1 || requestCode == 2 || requestCode == 3 || requestCode == 4) {
                spe.putString("NuRiT" + requestCode, ringUri.toString());
            }
            spe.apply();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode >99 && requestCode < 105) {
            Log.e("return from permission", "return code is: " + requestCode);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("return from permission", "return was true");
                //Got Permission
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("allowed", true);
                ed.apply();
                //ringtoner(requestCode-100);
            }
            else {
                Log.e("return from permission", "return permission denied");
                ConstraintLayout layout = requestCode == 100 ? NappT_Pref : requestCode == 101 ? NappT1_Pref :
                        requestCode == 102 ? NappT2_Pref : requestCode == 103 ? NappT3_Pref : NappT4_Pref;
                layout.performClick();
            }
        }
    }

}