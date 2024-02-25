package atlow.chemi.mymada.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import atlow.chemi.mymada.R;

public class SettingsTools extends Fragment implements View.OnClickListener {
    private final String[] toolsS = {"contrac", "pulse", "metro", "vital", "oxy", "apgar", "glaz", "prot", "dict"};
    private SwitchCompat tzirim, pulse, metro, vital, oxy, apgar, glazgo, proto, dictionary;
    private boolean tzirimB, pulseB, metroB, vitalB, oxyB, apgarB, glazgoB, protoB, dictionaryB;
    private SharedPreferences sp;

    public SettingsTools() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.pref_header_tools);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_tools, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tzirim = view.findViewById(R.id.tzirim);
        ConstraintLayout tzirimPref = view.findViewById(R.id.tzirim_pref);
        pulse = view.findViewById(R.id.pulse);
        ConstraintLayout pulsePref = view.findViewById(R.id.pulse_pref);
        metro = view.findViewById(R.id.metro);
        ConstraintLayout metroPref = view.findViewById(R.id.metro_pref);
        vital = view.findViewById(R.id.vital);
        ConstraintLayout vitalPref = view.findViewById(R.id.vital_pref);
        oxy = view.findViewById(R.id.oxy);
        ConstraintLayout oxyPref = view.findViewById(R.id.oxy_pref);
        apgar = view.findViewById(R.id.apgar);
        ConstraintLayout apgarPref = view.findViewById(R.id.apgar_pref);
        glazgo = view.findViewById(R.id.glazgo);
        ConstraintLayout glazgoPref = view.findViewById(R.id.glazgo_pref);
        proto = view.findViewById(R.id.proto);
        ConstraintLayout protoPref = view.findViewById(R.id.proto_pref);
        dictionary = view.findViewById(R.id.dictionary);
        ConstraintLayout dictionaryPref = view.findViewById(R.id.dictionary_pref);

        tzirimPref.setOnClickListener(this);
        pulsePref.setOnClickListener(this);
        metroPref.setOnClickListener(this);
        vitalPref.setOnClickListener(this);
        oxyPref.setOnClickListener(this);
        apgarPref.setOnClickListener(this);
        glazgoPref.setOnClickListener(this);
        protoPref.setOnClickListener(this);
        dictionaryPref.setOnClickListener(this);

        //get SharedPref
        sp = getActivity().getSharedPreferences("Settings", 0);
        var toolsToShow = sp.getString("Tools", Arrays.asList(toolsS).toString());
        tzirimB = toolsToShow.contains("contrac");
        pulseB = toolsToShow.contains("pulse");
        metroB = toolsToShow.contains("metro");
        vitalB = toolsToShow.contains("vital");
        oxyB = toolsToShow.contains("oxy");
        apgarB = toolsToShow.contains("apgar");
        glazgoB = toolsToShow.contains("glaz");
        protoB = toolsToShow.contains("prot");
        dictionaryB = toolsToShow.contains("dict");

        //set by the SharedPreferences
        setTrueOrFalse(tzirimB, tzirimPref.getId());
        setTrueOrFalse(pulseB, pulsePref.getId());
        setTrueOrFalse(metroB, metroPref.getId());
        setTrueOrFalse(vitalB, vitalPref.getId());
        setTrueOrFalse(oxyB, oxyPref.getId());
        setTrueOrFalse(apgarB, apgarPref.getId());
        setTrueOrFalse(glazgoB, glazgoPref.getId());
        setTrueOrFalse(protoB, protoPref.getId());
        setTrueOrFalse(dictionaryB, dictionaryPref.getId());
    }
    public void onClick(View view) {
        SwitchCompat sc = null;
        SharedPreferences.Editor editor = sp.edit();
        int b = 0;
        switch (view.getId()) {
            case R.id.tzirim_pref:
                sc = tzirim;
                tzirimB = !sc.isChecked();
                b = 0;
                break;
            case R.id.pulse_pref:
                sc = pulse;
                pulseB = !sc.isChecked();
                b = 1;
                break;
            case R.id.metro_pref:
                sc = metro;
                metroB = !sc.isChecked();
                b = 2;
                break;
            case R.id.vital_pref:
                sc = vital;
                vitalB = !sc.isChecked();
                b = 3;
                break;
            case R.id.oxy_pref:
                sc = oxy;
                oxyB = !sc.isChecked();
                b = 4;
                break;
            case R.id.apgar_pref:
                sc = apgar;
                apgarB = !sc.isChecked();
                b = 5;
                break;
            case R.id.glazgo_pref:
                sc = glazgo;
                glazgoB = !sc.isChecked();
                b = 6;
                break;
            case R.id.proto_pref:
                sc = proto;
                protoB = !sc.isChecked();
                b = 7;
                break;
            case R.id.dictionary_pref:
                sc = dictionary;
                dictionaryB = !sc.isChecked();
                b = 8;
                break;
        }
        editor.apply();
        Set<String> t = new HashSet<>();
        if (sc != null) {
            sc.setChecked(!sc.isChecked());
            if (!tzirim.isChecked() && !pulse.isChecked() && !metro.isChecked() && !vital.isChecked() && !oxy.isChecked() &&
                    !apgar.isChecked() && !glazgo.isChecked() && !proto.isChecked() && !dictionary.isChecked()) {
                View v = getActivity().findViewById(android.R.id.content);
                if (v != null) {
                    Snackbar snk = Snackbar.make(v, getResources().getString(R.string.NoPicked), Snackbar.LENGTH_LONG);
                    View viewSnk = snk.getView();
                    TextView tv = viewSnk.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    snk.show();
                }
                sc.setChecked(true);
                switch (b) {
                    case 0:
                        tzirimB = true;
                        break;
                    case 1:
                        pulseB= true;
                        break;
                    case 2:
                        metroB = true;
                        break;
                    case 3:
                        vitalB = true;
                        break;
                    case 4:
                        oxyB = true;
                        break;
                    case 5:
                        apgarB = true;
                        break;
                    case 6:
                        glazgoB = true;
                        break;
                    case 7:
                        protoB = true;
                        break;
                    case 8:
                        dictionaryB = true;
                        break;
                }
            } else {
                if (tzirimB) t.add(toolsS[0]);
                if (pulseB) t.add(toolsS[1]);
                if (metroB) t.add(toolsS[2]);
                if (vitalB) t.add(toolsS[3]);
                if (oxyB) t.add(toolsS[4]);
                if (apgarB) t.add(toolsS[5]);
                if (glazgoB) t.add(toolsS[6]);
                if (protoB) t.add(toolsS[7]);
                if (dictionaryB) t.add(toolsS[8]);
                SharedPreferences.Editor spe = sp.edit();
                spe.putStringSet("Tools", t);
                spe.apply();
            }
        }
    }
    private void setTrueOrFalse(boolean set, int id) {
        SwitchCompat sc = null;
        SharedPreferences.Editor editor = sp.edit();
        switch (id) {
            case R.id.tzirim_pref:
                sc = tzirim;
                break;
            case R.id.pulse_pref:
                sc = pulse;
                break;
            case R.id.metro_pref:
                sc = metro;
                break;
            case R.id.vital_pref:
                sc = vital;
                break;
            case R.id.oxy_pref:
                sc = oxy;
                break;
            case R.id.apgar_pref:
                sc = apgar;
                break;
            case R.id.glazgo_pref:
                sc = glazgo;
                break;
            case R.id.proto_pref:
                sc = proto;
                break;
            case R.id.dictionary_pref:
                sc = dictionary;
                break;
        }
        editor.apply();
        if (sc != null) {
            sc.setChecked(set);
        }
    }
}
