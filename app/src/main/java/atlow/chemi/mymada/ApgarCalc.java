package atlow.chemi.mymada;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ApgarCalc extends Fragment {
    private TextView a;
    private Spinner Apperence, Pulse, Grimace, Activity, Respirations;
    public ApgarCalc()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_apgar_calc, container, false);
        v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        TextView apgar1 = (TextView)view.findViewById(R.id.ApperenceTxt);
        TextView apgar2 = (TextView)view.findViewById(R.id.PulseTxt);
        TextView apgar3 = (TextView)view.findViewById(R.id.GrimaceTxt);
        TextView apgar4 = (TextView)view.findViewById(R.id.ActivityTxt);
        TextView apgar5 = (TextView)view.findViewById(R.id.RespirationsTxt);
        a = (TextView)view.findViewById(R.id.ResultTxt);
        a.setMovementMethod(new ScrollingMovementMethod());
        a.setTypeface(myTypeface);
        apgar1.setTypeface(myTypeface, Typeface.BOLD);
        apgar2.setTypeface(myTypeface, Typeface.BOLD);
        apgar3.setTypeface(myTypeface, Typeface.BOLD);
        apgar4.setTypeface(myTypeface, Typeface.BOLD);
        apgar5.setTypeface(myTypeface, Typeface.BOLD);
        String[] appes = getActivity().getResources().getStringArray(R.array.Appes);
        String[] pulss = getActivity().getResources().getStringArray(R.array.Pulss);
        String[] grims = getActivity().getResources().getStringArray(R.array.Grims);
        String[] actis = getActivity().getResources().getStringArray(R.array.Actis);
        String[] resps = getActivity().getResources().getStringArray(R.array.Resps);
        final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, appes);
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, pulss);
        final ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, grims);
        final ArrayAdapter<String> spinnerArrayAdapter4 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, actis);
        final ArrayAdapter<String> spinnerArrayAdapter5 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, resps);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Apperence = (Spinner)view.findViewById(R.id.ApperenceS);
        Pulse = (Spinner)view.findViewById(R.id.PulseS);
        Grimace = (Spinner)view.findViewById(R.id.GrimaceS);
        Activity = (Spinner)view.findViewById(R.id.ActivityS);
        Respirations = (Spinner)view.findViewById(R.id.RespirationsS);
        Apperence.setAdapter(spinnerArrayAdapter1);
        Pulse.setAdapter(spinnerArrayAdapter2);
        Grimace.setAdapter(spinnerArrayAdapter3);
        Activity.setAdapter(spinnerArrayAdapter4);
        Respirations.setAdapter(spinnerArrayAdapter5);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.sendVitals);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        if (!SettingsActivity.isRTL()) {
            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            fab.setLayoutParams(lp);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 0;
                i += Apperence.getSelectedItemPosition();
                i += Pulse.getSelectedItemPosition();
                i += Grimace.getSelectedItemPosition();
                i += Activity.getSelectedItemPosition();
                i += Respirations.getSelectedItemPosition();
                String s = "ציון אפגאר הוא: " + i;
                SpannableString ss = new SpannableString(s + "\nיילוד בציון נמוך מ4 זקוק להחייאה מיידית.\nניקוד בין 4 ל6 מסמן על יילוד במצוקה בינונית הזקוק לחמצון והמרצה.\nניקוד גבוה מ6 הינו אינדיקציה ליילוד תקין.");
                if (i < 4)
                    ss.setSpan(new ForegroundColorSpan(Color.RED),0,s.length(),0);
                else if (i > 6)
                    ss.setSpan(new ForegroundColorSpan(Color.rgb(70,178,58)),0,s.length(),0);
                else
                    ss.setSpan(new ForegroundColorSpan(Color.rgb(221, 190, 66)),0,s.length(),0);
                a.setText(ss);
            }
        });
    }
}
