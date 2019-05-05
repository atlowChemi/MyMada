package atlow.chemi.mymada;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SettingsDesignedTools extends AppCompatActivity implements View.OnClickListener {
    private final String[] toolsS = {"contrac", "pulse", "metro", "vital", "oxy", "apgar", "glaz", "prot"};
    private ConstraintLayout tzirimPref, pulsePref, metroPref, vitalPref, oxyPref, apgarPref, glazgoPref, protoPref;
    private SwitchCompat tzirim, pulse, metro, vital, oxy, apgar, glazgo, proto;
    boolean tzirimB, pulseB, metroB, vitalB, oxyB, apgarB, glazgoB, protoB;
    private SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_designed_tools);
        resetTitle();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tzirim = findViewById(R.id.tzirim);
        tzirimPref = findViewById(R.id.tzirim_pref);
        pulse = findViewById(R.id.pulse);
        pulsePref = findViewById(R.id.pulse_pref);
        metro = findViewById(R.id.metro);
        metroPref = findViewById(R.id.metro_pref);
        vital = findViewById(R.id.vital);
        vitalPref = findViewById(R.id.vital_pref);
        oxy = findViewById(R.id.oxy);
        oxyPref = findViewById(R.id.oxy_pref);
        apgar = findViewById(R.id.apgar);
        apgarPref = findViewById(R.id.apgar_pref);
        glazgo = findViewById(R.id.glazgo);
        glazgoPref = findViewById(R.id.glazgo_pref);
        proto = findViewById(R.id.proto);
        protoPref = findViewById(R.id.proto_pref);


        tzirimPref.setOnClickListener(this);
        pulsePref.setOnClickListener(this);
        metroPref.setOnClickListener(this);
        vitalPref.setOnClickListener(this);
        oxyPref.setOnClickListener(this);
        apgarPref.setOnClickListener(this);
        glazgoPref.setOnClickListener(this);
        protoPref.setOnClickListener(this);

        //get SharedPref
        sp = getSharedPreferences("Settings", 0);
        Set<String> toolsToShow = sp.getStringSet("Tools", new LinkedHashSet<>(Arrays.asList(toolsS)));
        tzirimB = toolsToShow.contains("contrac");
        pulseB = toolsToShow.contains("pulse");
        metroB = toolsToShow.contains("metro");
        vitalB = toolsToShow.contains("vital");
        oxyB = toolsToShow.contains("oxy");
        apgarB = toolsToShow.contains("apgar");
        glazgoB = toolsToShow.contains("glaz");
        protoB = toolsToShow.contains("prot");

        //set by the SharedPreferences
        setTrueOrFalse(tzirimB, tzirimPref.getId());
        setTrueOrFalse(pulseB, pulsePref.getId());
        setTrueOrFalse(metroB, metroPref.getId());
        setTrueOrFalse(vitalB, vitalPref.getId());
        setTrueOrFalse(oxyB, oxyPref.getId());
        setTrueOrFalse(apgarB, apgarPref.getId());
        setTrueOrFalse(glazgoB, glazgoPref.getId());
        setTrueOrFalse(protoB, protoPref.getId());
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
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
        }
        editor.apply();
        Set<String> t = new HashSet<>();
        if (sc != null) {
            sc.setChecked(!sc.isChecked());
            if (!tzirim.isChecked() && !pulse.isChecked() && !metro.isChecked() && !vital.isChecked() && !oxy.isChecked() &&
                    !apgar.isChecked() && !glazgo.isChecked() && !proto.isChecked()) {
                Snackbar snk = Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.NoPicked), Snackbar.LENGTH_LONG);
                View viewSnk = snk.getView();
                TextView tv = viewSnk.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                snk.show();
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
        }
        editor.apply();
        if (sc != null) {
            sc.setChecked(set);
        }
    }
}