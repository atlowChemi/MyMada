package atlow.chemi.mymada.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import atlow.chemi.mymada.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsAmpm extends Fragment implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {
    private TextView dayDesc, nightDesc;
    private int nightStartI, nightEndI, sleepDayI, sleepNightI;
    private String[] sounds = {"צליל ורטט", "רטט", "שקט"};
    private boolean nightStart = false;
    private SharedPreferences sp;

    public SettingsAmpm() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        sounds = getResources().getStringArray(R.array.ampm);
        if (getActivity() != null)
            getActivity().setTitle(R.string.pref_header_ampm);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_ampm, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConstraintLayout startNightPref = view.findViewById(R.id.start_night_pref);
        ConstraintLayout endNightPref = view.findViewById(R.id.end_night_pref);
        dayDesc = view.findViewById(R.id.sleepDay_Desc);
        ConstraintLayout sleepDay = view.findViewById(R.id.sleepDay_pref);
        nightDesc = view.findViewById(R.id.sleepNight_Desc);
        ConstraintLayout sleepNight = view.findViewById(R.id.sleepNight_pref);

        startNightPref.setOnClickListener(this);
        endNightPref.setOnClickListener(this);
        sleepDay.setOnClickListener(this);
        sleepNight.setOnClickListener(this);

        //get SharedPref
        sp = getActivity().getSharedPreferences("Settings", 0);
        nightStartI = Integer.parseInt(sp.getString("nightStart", "23"));
        nightEndI = Integer.parseInt(sp.getString("nightEnd", "7"));
        sleepDayI = Integer.parseInt(sp.getString("sleepDay", "0"));
        sleepNightI = Integer.parseInt(sp.getString("sleepNight", "0"));

        //set by the SharedPreferences
        setInfo(sleepDay.getId());
        setInfo(sleepNight.getId());

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
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
        pickerDialog.show(getChildFragmentManager(), "tAg");
    }
    private void Sleep(int newValue, int morningOrNight)  {
        String title = morningOrNight == 0 ? "sleepDay" : "sleepNight";
        int result = 0;
        if (morningOrNight == 1)    {
            int day = Integer.parseInt(sp.getString("sleepDay", "0"));
            switch (day) {
                case 0 -> {
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
                }
                case 1 -> {
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
                }
                case 2 -> {
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
                }
            }
        }
        if (morningOrNight == 0)    {
            int night = Integer.parseInt(sp.getString("sleepNight", "0"));
            switch (night) {
                case 0 -> {
                    result = switch (newValue) {
                        case 0 -> 0;
                        case 1 -> 3;
                        case 2 -> 6;
                        default -> result;
                    };
                }
                case 1 -> {
                    result = switch (newValue) {
                        case 0 -> 1;
                        case 1 -> 4;
                        case 2 -> 7;
                        default -> result;
                    };
                }
                case 2 -> {
                    result = switch (newValue) {
                        case 0 -> 2;
                        case 1 -> 5;
                        case 2 -> 8;
                        default -> result;
                    };
                }
            }
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(title, String.valueOf(newValue));
        editor.putString("sleep", String.valueOf(result));
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
        editor.putString(intToChange, String.valueOf(hourOfDay));
        editor.apply();
    }
}
