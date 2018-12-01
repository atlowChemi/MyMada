package atlow.chemi.mymada;

import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class SettingsFragemnt extends PreferenceFragment {
    String oldI = "6";
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("Settings", 0);
        String settings = getArguments().getString("type");
        if ("noti".equals(settings)) {
            addPreferencesFromResource(R.xml.pref_notification);
            boolean cut = sharedPreferences.getBoolean("cut", false);
            PreferenceScreen screen = getPreferenceScreen();
            PreferenceCategory all = (PreferenceCategory) findPreference("all");
            PreferenceCategory cut1 = (PreferenceCategory) findPreference("cut1");
            PreferenceCategory cut2 = (PreferenceCategory) findPreference("cut2");
            PreferenceCategory cut3 = (PreferenceCategory) findPreference("cut3");
            PreferenceCategory cut4 = (PreferenceCategory) findPreference("cut4");
            if (cut) {
                screen.removePreference(all);
                //1
                CheckBoxPreference onOffT1 = (CheckBoxPreference) findPreference("findT1");
                onOffT1.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NvibT1 = (CheckBoxPreference) findPreference("NvibT1");
                NvibT1.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NsouT1 = (CheckBoxPreference) findPreference("NsouT1");
                NsouT1.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NappT1 = (CheckBoxPreference) findPreference("NappT1");
                NappT1.setDependency("NsouT1");
                NappT1.setOnPreferenceChangeListener(list2);
                RingtonePreference NrinT1 = (RingtonePreference) findPreference("NrinT1");
                NrinT1.setDependency("NappT1");
                NrinT1.setOnPreferenceChangeListener(list2);
                //2
                CheckBoxPreference onOffT2 = (CheckBoxPreference) findPreference("findT2");
                onOffT2.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NvibT2 = (CheckBoxPreference) findPreference("NvibT2");
                NvibT2.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NsouT2 = (CheckBoxPreference) findPreference("NsouT2");
                NsouT2.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NappT2 = (CheckBoxPreference) findPreference("NappT2");
                NappT2.setDependency("NsouT2");
                NappT2.setOnPreferenceChangeListener(list2);
                RingtonePreference NrinT2 = (RingtonePreference) findPreference("NrinT2");
                NrinT2.setDependency("NappT2");
                NrinT2.setOnPreferenceChangeListener(list2);
                //3
                CheckBoxPreference onOffT3 = (CheckBoxPreference) findPreference("findT3");
                onOffT3.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NvibT3 = (CheckBoxPreference) findPreference("NvibT3");
                NvibT3.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NsouT3 = (CheckBoxPreference) findPreference("NsouT3");
                NsouT3.setOnPreferenceChangeListener(list2);
                CheckBoxPreference NappT3 = (CheckBoxPreference) findPreference("NappT3");
                NappT3.setDependency("NsouT3");
                NappT3.setOnPreferenceChangeListener(list2);
                RingtonePreference NrinT3 = (RingtonePreference) findPreference("NrinT3");
                NrinT3.setDependency("NappT3");
                NrinT3.setOnPreferenceChangeListener(list2);
            }
            if (!cut) {
                screen.removePreference(cut1);
                screen.removePreference(cut2);
                screen.removePreference(cut3);
                CheckBoxPreference Nvib = (CheckBoxPreference) findPreference("NvibT");
                Nvib.setOnPreferenceChangeListener(list2);
                CheckBoxPreference Nsou = (CheckBoxPreference) findPreference("NsouT");
                Nsou.setOnPreferenceChangeListener(list2);
                CheckBoxPreference Napp = (CheckBoxPreference) findPreference("NappT");
                Napp.setDependency("NsouT");
                Napp.setOnPreferenceChangeListener(list2);
                RingtonePreference Nrin = (RingtonePreference) findPreference("NrinT");
                Nrin.setDependency("NappT");
                Nrin.setOnPreferenceChangeListener(list2);
            }
            //4 - minhala
            CheckBoxPreference onOffT4 = (CheckBoxPreference) findPreference("findT4");
            onOffT4.setOnPreferenceChangeListener(list2);
            CheckBoxPreference NvibT4 = (CheckBoxPreference) findPreference("NvibT4");
            NvibT4.setOnPreferenceChangeListener(list2);
            CheckBoxPreference NsouT4 = (CheckBoxPreference) findPreference("NsouT4");
            NsouT4.setOnPreferenceChangeListener(list2);
            CheckBoxPreference NappT4 = (CheckBoxPreference) findPreference("NappT4");
            NappT4.setDependency("NsouT4");
            NappT4.setOnPreferenceChangeListener(list2);
            RingtonePreference NrinT4 = (RingtonePreference) findPreference("NrinT4");
            NrinT4.setDependency("NappT4");
            NrinT4.setOnPreferenceChangeListener(list2);
        }else if ("ampm".equals(settings))  {
            addPreferencesFromResource(R.xml.pref_ampm);
            TimePreference nightStart = (TimePreference)findPreference("nightStart");
            nightStart.setOnPreferenceChangeListener(list2);
            TimePreference nightEnd = (TimePreference)findPreference("nightEnd");
            nightEnd.setOnPreferenceChangeListener(list2);
            ListPreference sleepDay = (ListPreference)findPreference("sleepDay");
            sleepDay.setOnPreferenceChangeListener(list2);
            ListPreference sleepNight = (ListPreference)findPreference("sleepNight");
            sleepNight.setOnPreferenceChangeListener(list2);
        }else if ("rego".equals(settings))   {
            addPreferencesFromResource(R.xml.pref_moked);
            ListPreference region = (ListPreference)findPreference("moked");
            region.setOnPreferenceChangeListener(list2);
        }else if ("adva".equals(settings)) {
            addPreferencesFromResource(R.xml.pref_advanced);

            CheckBoxPreference not = (CheckBoxPreference)findPreference("not");
            not.setOnPreferenceChangeListener(list2);
            CheckBoxPreference win = (CheckBoxPreference)findPreference("newWin");
            win.setOnPreferenceChangeListener(list2);
            CheckBoxPreference cut = (CheckBoxPreference)findPreference("cutAlarm");
            cut.setOnPreferenceChangeListener(list2);
            CheckBoxPreference ride = (CheckBoxPreference)findPreference("ride");
            ride.setOnPreferenceChangeListener(list2);
            ride.setDependency("not");
            CheckBoxPreference stat = (CheckBoxPreference)findPreference("stat");
            stat.setOnPreferenceChangeListener(list2);
            CheckBoxPreference statVib = (CheckBoxPreference)findPreference("statVib");
            statVib.setOnPreferenceChangeListener(list2);
            CheckBoxPreference loop = (CheckBoxPreference)findPreference("loop");
            loop.setOnPreferenceChangeListener(list2);
            CheckBoxPreference shabat = (CheckBoxPreference)findPreference("shabat");
            shabat.setOnPreferenceChangeListener(list2);
            IntPre shabatT = (IntPre) findPreference("shabatTime");
            shabatT.setDependency("shabat");
            /*if (shabatT.getEditText().getText().toString().trim().isEmpty())
                shabatT.setText("6");*/
            shabatT.setOnPreferenceChangeListener(list2);
        } else if ("lang".equals(settings)) {
            addPreferencesFromResource(R.xml.pref_lang);
            ListPreference lang = (ListPreference)findPreference("Lang");
            lang.setOnPreferenceChangeListener(list2);
        }
    }

    Preference.OnPreferenceChangeListener list2 = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "not":
                    pe(newValue, "not");
                    break;
                case "newWin":
                    pe(newValue, "win");
                    break;
                case "cutAlarm":
                    pe(newValue, "cut");
                case "ride":
                    pe(newValue, "ride");
                    break;
                case "stat":
                    pe(newValue, "stat");
                    break;
                case "statVib":
                    pe(newValue, "statVib");
                    break;
                case "nightStart":
                    Moked(newValue, "nightStart");
                    break;
                case "nightEnd":
                    Moked(newValue, "nightEnd");
                    break;
                case "sleepDay":
                    Sleep(newValue, "sleepDay");
                    break;
                case "sleepNight":
                    Sleep(newValue, "sleepNight");
                    break;
                case "NvibT":
                    pe(newValue, "Nvib");
                    break;
                case "NsouT":
                    pe(newValue, "Nsou");
                    break;
                case "NrinT":
                    Spe(newValue, "NuRi");
                    break;
                case "NappT":
                    pe(newValue, "Napp");
                    break;
                case "findT1":
                    pe(newValue, "callT1");
                    break;
                case "findT2":
                    pe(newValue, "callT2");
                    break;
                case "findT3":
                    pe(newValue, "callT3");
                    break;
                case "findT4":
                    pe(newValue, "callT4");
                    break;
                case "NvibT1":
                    pe(newValue, "NvibT1");
                    break;
                case "NvibT2":
                    pe(newValue, "NvibT2");
                    break;
                case "NvibT3":
                    pe(newValue, "NvibT3");
                    break;
                case "NvibT4":
                    pe(newValue, "NvibT4");
                    break;
                case "NsouT1":
                    pe(newValue, "NsouT1");
                    break;
                case "NsouT2":
                    pe(newValue, "NsouT2");
                    break;
                case "NsouT3":
                    pe(newValue, "NsouT3");
                    break;
                case "NsouT4":
                    pe(newValue, "NsouT4");
                    break;
                case "NrinT1":
                    Spe(newValue, "NuRiT1");
                    break;
                case "NrinT2":
                    Spe(newValue, "NuRiT2");
                    break;
                case "NrinT3":
                    Spe(newValue, "NuRiT3");
                    break;
                case "NrinT4":
                    Spe(newValue, "NuRiT4");
                    break;
                case "NappT1":
                    pe(newValue, "NappT1");
                    break;
                case "NappT2":
                    pe(newValue, "NappT2");
                    break;
                case "NappT3":
                    pe(newValue, "NappT3");
                    break;
                case "NappT4":
                    pe(newValue, "NappT4");
                    break;
                case "loop":
                    pe(newValue, "loop");
                    break;
                case "shabat":
                    pe(newValue, "killS");
                    break;
                case "shabatTime":
                    OSTT(newValue);
                    break;
                case "moked":
                    Moked(newValue, "merhav");
                    break;
                case "Lang":
                    OLangC(newValue);
                    break;
            }
            return true;
        }
    };


    public boolean pe(Object newValue, String sett) {
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(sett, (Boolean)newValue);
        editor.apply();
        return true;
    }
    public void Spe(Object newValue, String sett)   {
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sett, newValue.toString());
        editor.apply();
    }
    public void OSTT(Object newValue)   {
        EditTextPreference shabatT = (EditTextPreference)findPreference("shabatTime");
        final String value = (String)newValue;
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        if (value.equals("")) {
            oldI = "6";
            shabatT.setText("6");
            editor.putString("timeK","6");
        }
        else {
            oldI = value;
            editor.putString("timeK",value);
        }
        editor.apply();
    }
    public void Moked(Object newValue, String sett)  {
        int newVal = Integer.parseInt(newValue.toString().replaceAll("[\\D]",""));
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav0");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav1");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav2");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav3");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav22");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav23");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav4");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav5");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav6");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav7");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav8");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav9");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav10");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("merhav11");
        String n = "merhav" + newVal;
        FirebaseMessaging.getInstance().subscribeToTopic(n);
        //Toast.makeText(getActivity(), n, Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        if (sett.equals("merhav") && !preferences.getBoolean("pickedMoked", false))
            editor.putBoolean("pickedMoked", true);
        editor.putInt(sett, newVal);
        editor.apply();
    }
    public void Sleep(Object newValue, String sett)  {
        int newVal = Integer.parseInt(newValue.toString().replaceAll("[\\D]",""));
        int result = 0;
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        if (sett.equals("sleepNight"))    {
            int other = preferences.getInt("sleepDay", 0);
            switch (other)  {
                case 0:
                    switch (newVal) {
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
                    switch (newVal) {
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
                    switch (newVal) {
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
        if (sett.equals("sleepDay"))    {
            int night = preferences.getInt("sleepNight", 0);
            switch (night)  {
                case 0:
                    switch (newVal) {
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
                    switch (newVal) {
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
                    switch (newVal) {
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
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(sett, newVal);
        editor.putInt("sleep", result);
        editor.apply();
    }
    public void OLangC(Object newValue) {
        SharedPreferences preferences = getActivity().getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lng", (String) newValue);
        editor.putBoolean("WasLangChanged", true);
        editor.apply();
        Resources resources = getActivity().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = new Locale((String)newValue);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        getActivity().recreate();
        /*AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                .setMessage(getResources().getString(R.string.langSwitched)).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).create();
        builder.show();*/
    }
}
