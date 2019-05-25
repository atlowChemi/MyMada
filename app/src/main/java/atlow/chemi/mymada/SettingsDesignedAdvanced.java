package atlow.chemi.mymada;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

public class SettingsDesignedAdvanced extends AppCompatActivity implements View.OnClickListener {
    private ConstraintLayout shabbatTimePref;
    private TextView notDesc, newWinDesc, rideDesc, loopDesc, shabatDesc, VolumeAppDesc;
    private SwitchCompat not, newWin, ride, loop, shabat, VolumeApp;
    private boolean VolumeAppB;
    private int VolumeAppNumber;
    private String[] Mokdim = new String[] {"ירושלים","איילון","אשר","גלבוע","דן","ירדן","ירקון","כרמל","לכיש","נגב","שרון","מוקד 4"};
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_designed_advanced);
        resetTitle();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        not = findViewById(R.id.not);
        notDesc = findViewById(R.id.not_Desc);
        ConstraintLayout notPref = findViewById(R.id.not_pref);
        newWin = findViewById(R.id.newWin);
        newWinDesc = findViewById(R.id.newWin_Desc);
        ConstraintLayout newWinPref = findViewById(R.id.newWin_pref);
        ride = findViewById(R.id.ride);
        rideDesc = findViewById(R.id.ride_Desc);
        ConstraintLayout ridePref = findViewById(R.id.ride_pref);
        loop = findViewById(R.id.loop);
        loopDesc = findViewById(R.id.loop_Desc);
        ConstraintLayout loopPref = findViewById(R.id.loop_pref);
        shabat = findViewById(R.id.shabat);
        shabatDesc = findViewById(R.id.shabat_Desc);
        ConstraintLayout shabatPref = findViewById(R.id.shabat_pref);
        shabbatTimePref = findViewById(R.id.shabatTime_pref);
        ConstraintLayout volumePref = findViewById(R.id.volume_pref);
        CardView moked = findViewById(R.id.moked_card);
        CardView ampm = findViewById(R.id.ampm_card);

        notPref.setOnClickListener(this);
        newWinPref.setOnClickListener(this);
        ridePref.setOnClickListener(this);
        loopPref.setOnClickListener(this);
        shabatPref.setOnClickListener(this);
        shabbatTimePref.setOnClickListener(this);
        volumePref.setOnClickListener(this);
        moked.setOnClickListener(this);
        ampm.setOnClickListener(this);

        //get SharedPref
        sp = getSharedPreferences("Settings", 0);
        boolean notB = sp.getBoolean("not", true);
        boolean newWinB = sp.getBoolean("win", true);
        boolean rideB= sp.getBoolean("ride", false);
        boolean loopB = sp.getBoolean("loop", false);
        boolean shabatB = sp.getBoolean("killS", false);

        //set by the SharedPreferences
        setTrueOrFalse(notB, notPref.getId());
        setTrueOrFalse(newWinB, newWinPref.getId());
        setTrueOrFalse(rideB, ridePref.getId());
        setTrueOrFalse(loopB, loopPref.getId());
        setTrueOrFalse(shabatB, shabatPref.getId());
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
        SharedPreferences.Editor editor = sp.edit();
        switch (view.getId()) {
            case R.id.not_pref:
                sc = not;
                desc = not.isChecked() ? getResources().getString(R.string.pref_description_not_off) : getResources().getString(R.string.pref_description_not_on);
                editor.putBoolean("not", !sc.isChecked());
                tv = notDesc;
                break;
            case R.id.newWin_pref:
                sc = newWin;
                desc = newWin.isChecked() ? getResources().getString(R.string.pref_description_newWin_off) : getResources().getString(R.string.pref_description_newWin_on);
                editor.putBoolean("win", !sc.isChecked());
                tv = newWinDesc;
                break;
            case R.id.ride_pref:
                sc = ride;
                desc = ride.isChecked() ? getResources().getString(R.string.pref_description_ride_off) : getResources().getString(R.string.pref_description_ride_on);
                editor.putBoolean("ride", !sc.isChecked());
                tv = rideDesc;
                break;
            case R.id.loop_pref:
                sc = loop;
                desc = loop.isChecked() ? getResources().getString(R.string.pref_description_loop_off) : getResources().getString(R.string.pref_description_loop_on);
                editor.putBoolean("loop", !sc.isChecked());
                tv = loopDesc;
                break;
            case R.id.shabat_pref:
                sc = shabat;
                desc = shabat.isChecked() ? getResources().getString(R.string.pref_description_shabat_off) : getResources().getString(R.string.pref_description_shabat_on);
                editor.putBoolean("killS", !sc.isChecked());
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
            case R.id.moked_card:
                PickMokedDialog();
                break;
            case R.id.ampm_card:
                sc = null;
                Intent intent = new Intent(SettingsDesignedAdvanced.this, SettingsDesignedAmpm.class);
                startActivity(intent);
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
                editor.putBoolean("not", set);
                tv = notDesc;
                break;
            case R.id.newWin_pref:
                sc = newWin;
                desc = !set ? getResources().getString(R.string.pref_description_newWin_off) : getResources().getString(R.string.pref_description_newWin_on);
                editor.putBoolean("win", set);
                tv = newWinDesc;
                break;
            case R.id.ride_pref:
                sc = ride;
                desc = !set ? getResources().getString(R.string.pref_description_ride_off) : getResources().getString(R.string.pref_description_ride_on);
                editor.putBoolean("ride", set);
                tv = rideDesc;
                break;
            case R.id.loop_pref:
                sc = loop;
                desc = !set ? getResources().getString(R.string.pref_description_loop_off) : getResources().getString(R.string.pref_description_loop_on);
                editor.putBoolean("loop", set);
                tv = loopDesc;
                break;
            case R.id.shabat_pref:
                sc = shabat;
                desc = !set ? getResources().getString(R.string.pref_description_shabat_off) : getResources().getString(R.string.pref_description_shabat_on);
                editor.putBoolean("killS", set);
                tv = shabatDesc;
                shabbatTimePref.setClickable(set);
                shabbatTimePref.setAlpha(set ? 1 : 0.4f);
                break;
            case R.id.volume_app_pref:
                sc = VolumeApp;
                desc = sc.isChecked() ? getResources().getString(R.string.pref_description_volume_app_off) : getResources().getString(R.string.pref_description_volume_app_on);
                editor.putBoolean("volumeApp", !sc.isChecked());
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
        View time = View.inflate(SettingsDesignedAdvanced.this, R.layout.shabat_t, null);
        final EditText shabatText = time.findViewById(R.id.shabatT);
        final TextInputLayout shabatTil = time.findViewById(R.id.shabatTil);
        final String oldI = sp.getString("timeK", "6");
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesignedAdvanced.this, R.style.AlertDialogStyle).setView(time);
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
        View volumer = View.inflate(SettingsDesignedAdvanced.this, R.layout.volume_dialog, null);
        ConstraintLayout VolumeAppPref = volumer.findViewById(R.id.volume_app_pref);
        VolumeApp = volumer.findViewById(R.id.volume_app);
        VolumeAppDesc = volumer.findViewById(R.id.volume_app_Desc);
        /*SeekBar setup*/
        SeekBar VolumeSeek = volumer.findViewById(R.id.VolumeSeekBar);
        AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int vol = manager != null ? manager.getStreamMaxVolume(AudioManager.STREAM_ALARM) : 20;
        /*Setup dialog*/
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesignedAdvanced.this, R.style.AlertDialogStyle).setView(volumer);
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss()).setPositiveButton(getString(R.string.ok), null);
        final AlertDialog langPicker = builder.create();
        langPicker.setOnShowListener(dialog -> {
            VolumeAppPref.setOnClickListener((v) -> {
                String desc = VolumeApp.isChecked() ? getResources().getString(R.string.pref_description_volume_app_off) : getResources().getString(R.string.pref_description_volume_app_on);
                VolumeApp.setChecked(!VolumeApp.isChecked());
                VolumeAppDesc.setText(desc);
            });
            VolumeAppB = sp.getBoolean("volumeApp", true);
            VolumeAppNumber = sp.getInt("volumeAppNum", vol);
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
                editor.putBoolean("volumeApp", VolumeApp.isChecked());
                editor.putInt("volumeAppNum", VolumeAppNumber);
                editor.apply();
                    dialog.dismiss();
            });
        });
        langPicker.show();
    }
    private void PickMokedDialog() {
        final int MokedToPick = sp.getInt("merhav", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesignedAdvanced.this, R.style.AlertDialogStyle);
        builder.setTitle(getString(R.string.pref_title_region));
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss());
        builder.setSingleChoiceItems(Mokdim, MokedToPick, (dialog, which) -> {
            if (MokedToPick != which) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("pickedMoked", true);
                editor.putInt("merhav", which);
                editor.apply();
                dialog.dismiss();
            }
        });
        AlertDialog langPicker = builder.create();
        langPicker.show();
    }
}