package atlow.chemi.mymada.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.SettingsPage;

public class SettingsAdvanced extends Fragment implements View.OnClickListener {
    private ConstraintLayout shabbatTimePref;
    private TextView notDesc, newWinDesc, rideDesc, loopDesc, shabatDesc, VolumeAppDesc;
    private SwitchCompat not, newWin, ride, loop, shabat, VolumeApp;
    private boolean VolumeAppB;
    private int VolumeAppNumber;
    private SharedPreferences sp;
    public SettingsAdvanced() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.pref_header_advanced);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_advanced, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        not = view.findViewById(R.id.not);
        notDesc = view.findViewById(R.id.not_Desc);
        ConstraintLayout notPref = view.findViewById(R.id.not_pref);
        newWin = view.findViewById(R.id.newWin);
        newWinDesc = view.findViewById(R.id.newWin_Desc);
        ConstraintLayout newWinPref = view.findViewById(R.id.newWin_pref);
        ride = view.findViewById(R.id.ride);
        rideDesc = view.findViewById(R.id.ride_Desc);
        ConstraintLayout ridePref = view.findViewById(R.id.ride_pref);
        loop = view.findViewById(R.id.loop);
        loopDesc = view.findViewById(R.id.loop_Desc);
        ConstraintLayout loopPref = view.findViewById(R.id.loop_pref);
        shabat = view.findViewById(R.id.shabat);
        shabatDesc = view.findViewById(R.id.shabat_Desc);
        ConstraintLayout shabatPref = view.findViewById(R.id.shabat_pref);
        shabbatTimePref = view.findViewById(R.id.shabatTime_pref);
        ConstraintLayout volumePref = view.findViewById(R.id.volume_pref);
        CardView ampm = view.findViewById(R.id.ampm_card);

        notPref.setOnClickListener(this);
        newWinPref.setOnClickListener(this);
        ridePref.setOnClickListener(this);
        loopPref.setOnClickListener(this);
        shabatPref.setOnClickListener(this);
        shabbatTimePref.setOnClickListener(this);
        volumePref.setOnClickListener(this);
        ampm.setOnClickListener(this);

        //get SharedPref
        sp = getActivity().getSharedPreferences("Settings", 0);
        boolean notB = sp.getString("not", "true").equals("true");
        boolean newWinB = sp.getString("win", "true").equals("true");
        boolean rideB= sp.getString("ride", "false").equals("true");
        boolean loopB = sp.getString("loop", "false").equals("true");
        boolean shabatB = sp.getString("killS", "false").equals("true");

        //set by the SharedPreferences
        setTrueOrFalse(notB, notPref.getId());
        setTrueOrFalse(newWinB, newWinPref.getId());
        setTrueOrFalse(rideB, ridePref.getId());
        setTrueOrFalse(loopB, loopPref.getId());
        setTrueOrFalse(shabatB, shabatPref.getId());
    }
    @Override
    public void onClick(View view) {
        SwitchCompat sc = null;
        String desc = null;
        TextView tv = null;
        SharedPreferences.Editor editor = sp.edit();
        switch (view.getId()) {
            case R.id.not_pref:
                sc = not;
                desc = not.isChecked() ? getResources().getString(R.string.pref_description_not_off) : getResources().getString(R.string.pref_description_not_on);
                editor.putString("not", String.valueOf(!sc.isChecked()));
                tv = notDesc;
                break;
            case R.id.newWin_pref:
                sc = newWin;
                desc = newWin.isChecked() ? getResources().getString(R.string.pref_description_newWin_off) : getResources().getString(R.string.pref_description_newWin_on);
                editor.putString("win", String.valueOf(!sc.isChecked()));
                tv = newWinDesc;
                break;
            case R.id.ride_pref:
                sc = ride;
                desc = ride.isChecked() ? getResources().getString(R.string.pref_description_ride_off) : getResources().getString(R.string.pref_description_ride_on);
                editor.putString("ride", String.valueOf(!sc.isChecked()));
                tv = rideDesc;
                break;
            case R.id.loop_pref:
                sc = loop;
                desc = loop.isChecked() ? getResources().getString(R.string.pref_description_loop_off) : getResources().getString(R.string.pref_description_loop_on);
                editor.putString("loop", String.valueOf(!sc.isChecked()));
                tv = loopDesc;
                break;
            case R.id.shabat_pref:
                sc = shabat;
                desc = shabat.isChecked() ? getResources().getString(R.string.pref_description_shabat_off) : getResources().getString(R.string.pref_description_shabat_on);
                editor.putString("killS", String.valueOf(!sc.isChecked()));
                tv = shabatDesc;
                shabbatTimePref.setClickable(!shabat.isChecked());
                shabbatTimePref.setAlpha(!shabat.isChecked() ? 1 : 0.4f);
                break;
            case R.id.shabatTime_pref:
                SetShabbatTimeDialog();
                break;
            case R.id.volume_pref:
                SetVolumeDialog();
                break;
            case R.id.ampm_card:
                sc = null;
                ((SettingsPage)getActivity()).displayFragment(new SettingsAmpm(), R.id.ampm);
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
            case R.id.not_pref:
                sc = not;
                desc = !set ? getResources().getString(R.string.pref_description_not_off) : getResources().getString(R.string.pref_description_not_on);
                editor.putString("not", String.valueOf(set));
                tv = notDesc;
                break;
            case R.id.newWin_pref:
                sc = newWin;
                desc = !set ? getResources().getString(R.string.pref_description_newWin_off) : getResources().getString(R.string.pref_description_newWin_on);
                editor.putString("win", String.valueOf(set));
                tv = newWinDesc;
                break;
            case R.id.ride_pref:
                sc = ride;
                desc = !set ? getResources().getString(R.string.pref_description_ride_off) : getResources().getString(R.string.pref_description_ride_on);
                editor.putString("ride", String.valueOf(set));
                tv = rideDesc;
                break;
            case R.id.loop_pref:
                sc = loop;
                desc = !set ? getResources().getString(R.string.pref_description_loop_off) : getResources().getString(R.string.pref_description_loop_on);
                editor.putString("loop", String.valueOf(set));
                tv = loopDesc;
                break;
            case R.id.shabat_pref:
                sc = shabat;
                desc = !set ? getResources().getString(R.string.pref_description_shabat_off) : getResources().getString(R.string.pref_description_shabat_on);
                editor.putString("killS", String.valueOf(set));
                tv = shabatDesc;
                shabbatTimePref.setClickable(set);
                shabbatTimePref.setAlpha(set ? 1 : 0.4f);
                break;
            case R.id.volume_app_pref:
                sc = VolumeApp;
                desc = sc.isChecked() ? getResources().getString(R.string.pref_description_volume_app_off) : getResources().getString(R.string.pref_description_volume_app_on);
                editor.putString("volumeApp", String.valueOf(!sc.isChecked()));
                tv = VolumeAppDesc;
                break;
        }
        editor.apply();
        if (sc != null && tv != null && !desc.isEmpty()) {
            sc.setChecked(set);
            tv.setText(desc);
        }
    }
    private void SetShabbatTimeDialog() {
        View time = View.inflate(getActivity(), R.layout.shabat_t, null);
        final EditText shabatText = time.findViewById(R.id.shabatT);
        final TextInputLayout shabatTil = time.findViewById(R.id.shabatTil);
        final String oldI = sp.getString("timeK", "6");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle).setView(time);
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss()).setPositiveButton(getString(R.string.ok), null);
        final AlertDialog langPicker = builder.create();
        langPicker.setOnShowListener((dialog) -> {
            shabatText.setText(oldI);
            Button ok = langPicker.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setOnClickListener((v) -> {
                if (shabatText.getText().toString().isEmpty())
                    shabatTil.setError(getResources().getString(R.string.numberNone));
                if (shabatText.getText().toString().trim().length() > 0 && TextUtils.isDigitsOnly(shabatText.getText().toString().trim())) {
                    String newI = shabatText.getText().toString().trim();
                    shabatTil.setErrorEnabled(false);
                    SharedPreferences.Editor e = sp.edit();
                    e.putString("timeK", newI);
                    e.apply();
                    dialog.dismiss();
                }
            });
        });
        langPicker.show();
    }
    private void SetVolumeDialog() {
        View volumer = View.inflate(getActivity(), R.layout.volume_dialog, null);
        ConstraintLayout VolumeAppPref = volumer.findViewById(R.id.volume_app_pref);
        VolumeApp = volumer.findViewById(R.id.volume_app);
        VolumeAppDesc = volumer.findViewById(R.id.volume_app_Desc);
        /*SeekBar setup*/
        SeekBar VolumeSeek = volumer.findViewById(R.id.VolumeSeekBar);
        AudioManager manager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        int vol = manager != null ? manager.getStreamMaxVolume(AudioManager.STREAM_ALARM) : 20;
        /*Setup dialog*/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle).setView(volumer);
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss()).setPositiveButton(getString(R.string.ok), null);
        final AlertDialog langPicker = builder.create();
        langPicker.setOnShowListener(dialog -> {
            VolumeAppPref.setOnClickListener((v) -> {
                String desc = VolumeApp.isChecked() ? getResources().getString(R.string.pref_description_volume_app_off) : getResources().getString(R.string.pref_description_volume_app_on);
                VolumeApp.setChecked(!VolumeApp.isChecked());
                VolumeAppDesc.setText(desc);
            });
            VolumeAppB = sp.getString("volumeApp", "true").equals("true");
            VolumeAppNumber = Integer.parseInt(sp.getString("volumeAppNum", String.valueOf(vol)));
            setTrueOrFalse(VolumeAppB, VolumeAppPref.getId());
            VolumeSeek.setProgress(VolumeAppNumber);
            VolumeSeek.setMax(vol);
            VolumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    VolumeAppNumber = progress;
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            Button ok = langPicker.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setOnClickListener((view) -> {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("volumeApp", String.valueOf(VolumeApp.isChecked()));
                editor.putString("volumeAppNum", String.valueOf(VolumeAppNumber));
                editor.apply();
                    dialog.dismiss();
            });
        });
        langPicker.show();
    }
}