package atlow.chemi.mymada;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class SettingsDesignedAmpm extends AppCompatActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    private TextView dayDesc, nightDesc;
    private int nightStartI, nightEndI, sleepDayI, sleepNightI;
    private String[] sounds = {"צליל ורטט", "רטט", "שקט"};
    private boolean nightStart = false;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_designed_ampm);
        resetTitle();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sounds = getResources().getStringArray(R.array.ampm);

        ConstraintLayout startNightPref = findViewById(R.id.start_night_pref);
        ConstraintLayout endNightPref = findViewById(R.id.end_night_pref);
        dayDesc = findViewById(R.id.sleepDay_Desc);
        ConstraintLayout sleepDay = findViewById(R.id.sleepDay_pref);
        nightDesc = findViewById(R.id.sleepNight_Desc);
        ConstraintLayout sleepNight = findViewById(R.id.sleepNight_pref);

        startNightPref.setOnClickListener(this);
        endNightPref.setOnClickListener(this);
        sleepDay.setOnClickListener(this);
        sleepNight.setOnClickListener(this);

        //get SharedPref
        sp = getSharedPreferences("Settings", 0);
        nightStartI = sp.getInt("nightStart", 23);
        nightEndI = sp.getInt("nightEnd", 7);
        sleepDayI = sp.getInt("sleepDay", 0);
        sleepNightI = sp.getInt("sleepNight", 0);

        //set by the SharedPreferences
        setInfo(sleepDay.getId());
        setInfo(sleepNight.getId());
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
        switch (view.getId()) {
            case R.id.start_night_pref:
                SetStartAndEndDialog(0);
                break;
            case R.id.end_night_pref:
                SetStartAndEndDialog(1);
                break;
            case R.id.sleepDay_pref:
                SetSleepDialog(0);
                break;
            case R.id.sleepNight_pref:
                SetSleepDialog(1);
                break;
        }
    }
    private void setInfo(int id) {
        String desc = null;
        TextView tv = null;
        switch (id) {
            case R.id.sleepDay_pref:
                desc = sounds[sleepDayI];
                tv = dayDesc;
                break;
            case R.id.sleepNight_pref:
                desc = sounds[sleepNightI];
                tv = nightDesc;
                break;
        }
        if (tv != null && !desc.isEmpty()) {
            tv.setText(desc);
        }
    }
    private void SetSleepDialog(int morningOrNight) {
        int SoundToShow = morningOrNight == 0 ? sleepDayI : sleepNightI;
        TextView tvDesc = morningOrNight == 0 ? dayDesc : nightDesc;
        String title = morningOrNight == 0 ? getString(R.string.notifDay) : getString(R.string.notifNight);
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsDesignedAmpm.this, R.style.AlertDialogStyle);
        builder.setTitle(title);
        builder.setNegativeButton(getString(R.string.ret), (dialog, which) -> dialog.dismiss());
        builder.setSingleChoiceItems(sounds, SoundToShow, (dialog, which) -> {
            if (SoundToShow != which) {
                Sleep(which, morningOrNight);
                tvDesc.setText(sounds[which]);
                if (morningOrNight == 0)
                    sleepDayI = which;
                else
                    sleepNightI = which;
            }
            dialog.dismiss();
        });
        AlertDialog langPicker = builder.create();
        langPicker.show();
    }
    private void SetStartAndEndDialog(int morningOrNight) {
        int hour = morningOrNight == 0 ? nightStartI : nightEndI;
        nightStart = morningOrNight == 0;
        String title = morningOrNight == 0 ? getString(R.string.nightStart) : getString(R.string.nightEnd);
        TimePickerDialog pickerDialog = TimePickerDialog.newInstance(this, hour, 25, 0, true);
        pickerDialog.enableMinutes(false);
        pickerDialog.setTitle(title);
        pickerDialog.show(getSupportFragmentManager(), "tAg");
    }
    public void Sleep(int newValue, int morningOrNight)  {
        String title = morningOrNight == 0 ? "sleepDay" : "sleepNight";
        int result = 0;
        if (morningOrNight == 1)    {
            int other = sp.getInt("sleepDay", 0);
            switch (other)  {
                case 0:
                    switch (newValue) {
                        case 0:
                            result = 0;
                            break;
                        case 1:
                            result = 1;
                            break;
                        case 2:
                            result = 2;
                            break;
                    }
                    break;
                case 1:
                    switch (newValue) {
                        case 0:
                            result = 3;
                            break;
                        case 1:
                            result = 4;
                            break;
                        case 2:
                            result = 5;
                            break;
                    }
                    break;
                case 2:
                    switch (newValue) {
                        case 0:
                            result = 6;
                            break;
                        case 1:
                            result = 7;
                            break;
                        case 2:
                            result = 8;
                            break;
                    }
                    break;
            }
        }
        if (morningOrNight == 0)    {
            int night = sp.getInt("sleepNight", 0);
            switch (night)  {
                case 0:
                    switch (newValue) {
                        case 0:
                            result = 0;
                            break;
                        case 1:
                            result = 3;
                            break;
                        case 2:
                            result = 6;
                            break;
                    }
                    break;
                case 1:
                    switch (newValue) {
                        case 0:
                            result = 1;
                            break;
                        case 1:
                            result = 4;
                            break;
                        case 2:
                            result = 7;
                            break;
                    }
                    break;
                case 2:
                    switch (newValue) {
                        case 0:
                            result = 2;
                            break;
                        case 1:
                            result = 5;
                            break;
                        case 2:
                            result = 8;
                            break;
                    }
                    break;
            }
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(title, newValue);
        editor.putInt("sleep", result);
        editor.apply();
    }
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        SharedPreferences.Editor editor = sp.edit();
        String intToChange = nightStart ? "nightStart" : "nightEnd";
        if (nightStart)
            nightStartI = hourOfDay;
        else
            nightEndI = hourOfDay;
        editor.putInt(intToChange, hourOfDay);
        editor.apply();
    }
}