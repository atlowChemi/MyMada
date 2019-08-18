package atlow.chemi.mymada.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import atlow.chemi.mymada.classes.AnimatedColor;
import atlow.chemi.mymada.R;

public class GlazgoCalc extends Fragment {
    private TextView a;
    private Spinner eye, verb, motor;
    private int old, newer;
    private AnimatedColor color;
    public GlazgoCalc()    {

    }
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.button_gl);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        return inflater.inflate(R.layout.activity_glazgo_calc, container, false);
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
        GradientDrawable draw = (GradientDrawable) a.getBackground();
        draw.mutate();
        draw.setColor(ContextCompat.getColor(getActivity(), R.color.calcB));
        a.setBackground(draw);
        old = 3;
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
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        verb.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        motor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        GradientDrawable drawable = (GradientDrawable) glazgoInfBG.getBackground();
        drawable.mutate();
        drawable.setColor(getActivity().getResources().getColor(R.color.White));
        glazgoInfBG.setBackground(drawable);
        glazgoInf.setText(getActivity().getResources().getString(R.string.GlasgowInfo));
        glazgoInf.setMovementMethod(new ScrollingMovementMethod());
    }
    private void calc(){
        newer = 0;
        newer += (eye.getSelectedItemPosition()+1);
        newer += (verb.getSelectedItemPosition()+1);
        newer += (motor.getSelectedItemPosition()+1);
        final GradientDrawable drawable = (GradientDrawable) a.getBackground();
        drawable.mutate();
        String c = " (";
        if (newer < 9) {
            Animate(old, newer);
            //drawable.setColor(Color.rgb(191,23,22));
            c += getActivity().getResources().getString(R.string.GlasgowHigh) + ")";
        } else if (newer > 12) {
            Animate(old, newer);
            //drawable.setColor(Color.rgb(70, 178, 58));a.setBackground(drawable);
            c += getActivity().getResources().getString(R.string.GlasgowLow) + ")";
        } else {
            Animate(old, newer);
            //drawable.setColor(Color.rgb(221, 190, 66));a.setBackground(drawable);
            c += getActivity().getResources().getString(R.string.GlasgowMed) + ")";
        }
        String s = getActivity().getResources().getString(R.string.Glasgow, newer + c);
        a.setText(s);
    }
    private void Animate(int start, int end) {
        old = newer;
        Log.e("COLORS", "start: " + start + " end: " + end);
        int from = Color.rgb(193,21,22), to = Color.rgb(193,21,22);
        if (start >= 9)
            from = Color.rgb(221,190,66);
        if (start > 12)
            from = Color.rgb(70,178,58);
        if (end >= 9)
            to = Color.rgb(221,190,66);
        if (end > 12)
            to = Color.rgb(70,178,58);
        color = new AnimatedColor(from, to);
        ValueAnimator animator = ObjectAnimator.ofFloat(0f, 1f).setDuration(1000);
        animator.addUpdateListener(animation -> {
            float v = (float) animation.getAnimatedValue();
           Log.e("COLOR::", "r " + Color.red(color.with(0.4555f)));
            GradientDrawable gradientDrawable = (GradientDrawable)a.getBackground();
            gradientDrawable.mutate();
            gradientDrawable.setColor(color.with(v));
            a.setBackground(gradientDrawable);
        });
        animator.start();
    }
}
