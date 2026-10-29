package atlow.chemi.mymada.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.SettingsPage;

public class SettingsNotifications extends Fragment implements View.OnClickListener {
    private ConstraintLayout cutAPref, statPref, statVibPref, T6Pref, findT4Pref;
    private TextView cutADesc, statDesc, statVibDesc, T6Desc, findT4Desc;
    private SwitchCompat cutA, stat, statVib, T6, findT4;
    boolean cutAB, statB, statVibB, T6B, findT4B;
    private SharedPreferences sp;

    public SettingsNotifications() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.pref_header_notifications);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_notifications, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cutA = view.findViewById(R.id.cutAlarm);
        cutADesc = view.findViewById(R.id.cutA_Desc);
        cutAPref = view.findViewById(R.id.cutA_pref);
        stat = view.findViewById(R.id.stat);
        statDesc = view.findViewById(R.id.stat_Desc);
        statPref = view.findViewById(R.id.stat_pref);
        statVib = view.findViewById(R.id.statVib);
        statVibDesc = view.findViewById(R.id.statVib_Desc);
        statVibPref = view.findViewById(R.id.statVib_pref);
        T6 = view.findViewById(R.id.T6);
        T6Desc = view.findViewById(R.id.T6_Desc);
        T6Pref = view.findViewById(R.id.T6_pref);
        findT4 = view.findViewById(R.id.findT4);
        findT4Desc = view.findViewById(R.id.findT4_Desc);
        findT4Pref = view.findViewById(R.id.findT4_pref);

        CardView SoundVib = view.findViewById(R.id.sound_vib_card);


        cutAPref.setOnClickListener(this);
        statPref.setOnClickListener(this);
        statVibPref.setOnClickListener(this);
        T6Pref.setOnClickListener(this);
        findT4Pref.setOnClickListener(this);
        SoundVib.setOnClickListener(this);

        //get SharedPref
        sp = getActivity().getSharedPreferences("Settings", 0);
        cutAB = sp.getBoolean("cut", false);
        statB = sp.getBoolean("stat", false);
        statVibB = sp.getBoolean("statVib", false);
        T6B = sp.getBoolean("callT6", true);
        findT4B = sp.getBoolean("callT4", true);

        //set by the SharedPreferences
        setTrueOrFalse(cutAB, cutAPref.getId());
        setTrueOrFalse(statB, statPref.getId());
        setTrueOrFalse(statVibB, statVibPref.getId());
        setTrueOrFalse(T6B, T6Pref.getId());
        setTrueOrFalse(findT4B, findT4Pref.getId());
    }

    public void onClick(View view) {
        SwitchCompat sc = null;
        String desc = null;
        TextView tv = null;
        SharedPreferences.Editor editor = sp.edit();
        switch (view.getId()) {
            case R.id.cutA_pref:
                sc = cutA;
                desc = cutA.isChecked() ? getResources().getString(R.string.pref_description_cutA_off) : getResources().getString(R.string.pref_description_cutA_on);
                editor.putBoolean("cut", !sc.isChecked());
                tv = cutADesc;
                break;
            case R.id.stat_pref:
                sc = stat;
                desc = stat.isChecked() ? getResources().getString(R.string.pref_description_state_off) : getResources().getString(R.string.pref_description_state_on);
                editor.putBoolean("stat", !sc.isChecked());
                tv = statDesc;
                break;
            case R.id.statVib_pref:
                sc = statVib;
                desc = statVib.isChecked() ? getResources().getString(R.string.pref_description_stateVib_off) : getResources().getString(R.string.pref_description_stateVib_on);
                editor.putBoolean("statVib", !sc.isChecked());
                tv = statVibDesc;
                break;
            case R.id.T6_pref:
                sc = T6;
                desc = T6.isChecked() ? getResources().getString(R.string.findT6_off) : getResources().getString(R.string.findT6_on);
                editor.putBoolean("callT6", !sc.isChecked());
                tv = T6Desc;
                break;
            case R.id.findT4_pref:
                sc = findT4;
                desc = findT4.isChecked() ? getResources().getString(R.string.findT4_off) : getResources().getString(R.string.findT4_on);
                editor.putBoolean("callT4", !sc.isChecked());
                tv = findT4Desc;
                break;
            case R.id.sound_vib_card:
                sc = null;
                ((SettingsPage)getActivity()).displayFragment(new SettingsNotificationsSounds(), R.id.soundAndVib);
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
            case R.id.cutA_pref:
                sc = cutA;
                desc = !set ? getResources().getString(R.string.pref_description_cutA_off) : getResources().getString(R.string.pref_description_cutA_on);
                editor.putBoolean("cut", set);
                tv = cutADesc;
                break;
            case R.id.stat_pref:
                sc = stat;
                desc = !set ? getResources().getString(R.string.pref_description_state_off) : getResources().getString(R.string.pref_description_state_on);
                editor.putBoolean("stat", set);
                tv = statDesc;
                break;
            case R.id.statVib_pref:
                sc = statVib;
                desc = !set ? getResources().getString(R.string.pref_description_stateVib_off) : getResources().getString(R.string.pref_description_stateVib_on);
                editor.putBoolean("statVib", set);
                tv = statVibDesc;
                break;

            case R.id.T6_pref:
                sc = T6;
                desc = !set ? getResources().getString(R.string.findT6_off) : getResources().getString(R.string.findT6_on);
                editor.putBoolean("callT6", set);
                tv = T6Desc;
                break;
            case R.id.findT4_pref:
                sc = findT4;
                desc = !set ? getResources().getString(R.string.findT4_off) : getResources().getString(R.string.findT4_on);
                editor.putBoolean("callT4", set);
                tv = findT4Desc;
                break;
        }
        editor.apply();
        if (sc != null && tv != null && !desc.isEmpty()) {
            sc.setChecked(set);
            tv.setText(desc);
        }
    }
}