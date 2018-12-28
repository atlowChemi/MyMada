package atlow.chemi.mymada;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class GlazgoCalc extends Fragment {
    private TextView a;
    private Spinner eye, verb, motor;
    public GlazgoCalc()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_glazgo_calc, container, false);
        if (SettingsActivity.isRTL())
            v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        TextView glazgo1 = view.findViewById(R.id.EyeTxt);
        TextView glazgo2 = view.findViewById(R.id.VerbalTxt);
        TextView glazgo3 = view.findViewById(R.id.MotorTxt);
        TextView glazgoInf = view.findViewById(R.id.GLZInf);
        LinearLayout glazgoInfBG = view.findViewById(R.id.GLZInfBG);
        a = view.findViewById(R.id.GLZGrade);
        a.setTypeface(myTypeface);
        glazgoInf.setTypeface(myTypeface);
        glazgo1.setTypeface(myTypeface, Typeface.BOLD);
        glazgo2.setTypeface(myTypeface, Typeface.BOLD);
        glazgo3.setTypeface(myTypeface, Typeface.BOLD);
        String[] Eyes = getActivity().getResources().getStringArray(R.array.eyes);
        String[] verbals = getActivity().getResources().getStringArray(R.array.verb);
        String[] motors = getActivity().getResources().getStringArray(R.array.moto);
        final ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, Eyes);
        final ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, verbals);
        final ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, motors);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eye = view.findViewById(R.id.EyeS);
        verb = view.findViewById(R.id.VerbalS);
        motor = view.findViewById(R.id.MotorS);
        eye.setAdapter(spinnerArrayAdapter1);
        verb.setAdapter(spinnerArrayAdapter2);
        motor.setAdapter(spinnerArrayAdapter3);
        eye.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        verb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        motor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        GradientDrawable drawable = (GradientDrawable) glazgoInfBG.getBackground();
        drawable.mutate();
        drawable.setColor(getActivity().getResources().getColor(R.color.White));
        glazgoInfBG.setBackground(drawable);
        glazgoInf.setText(getActivity().getResources().getString(R.string.GlasgowInfo));
    }
    private void calc(){
        int i = 0;
        i += (eye.getSelectedItemPosition()+1);
        i += (verb.getSelectedItemPosition()+1);
        i += (motor.getSelectedItemPosition()+1);
        GradientDrawable drawable = (GradientDrawable) a.getBackground();
        drawable.mutate();
        String c = " (";
        if (i < 9) {
            drawable.setColor(Color.rgb(191,23,22));
            c += getActivity().getResources().getString(R.string.GlasgowHigh) + ")";
        } else if (i > 12) {
            drawable.setColor(Color.rgb(70, 178, 58));
            c += getActivity().getResources().getString(R.string.GlasgowLow) + ")";
        } else {
            drawable.setColor(Color.rgb(221, 190, 66));
            c += getActivity().getResources().getString(R.string.GlasgowMed) + ")";
        }
        a.setBackground(drawable);
        String s = getActivity().getResources().getString(R.string.Glasgow, i + c);
        a.setText(s);
    }
}
