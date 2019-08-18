package atlow.chemi.mymada.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

public class ApgarCalc extends Fragment {
    private TextView a;
    private Spinner Apperence, Pulse, Grimace, Activity, Respirations;
    private int old,newer;
    private AnimatedColor color;
    public ApgarCalc()    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null)
            getActivity().setTitle(R.string.button_ag);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        return inflater.inflate(R.layout.activity_apgar_calc, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        TextView apgar1 = view.findViewById(R.id.ApperenceTxt);
        TextView apgar2 = view.findViewById(R.id.PulseTxt);
        TextView apgar3 = view.findViewById(R.id.GrimaceTxt);
        TextView apgar4 = view.findViewById(R.id.ActivityTxt);
        TextView apgar5 = view.findViewById(R.id.RespirationsTxt);
        LinearLayout apgarInfBG = view.findViewById(R.id.ApgarInfBG);
        TextView apgarInfi = view.findViewById(R.id.ApgarInfi);
        a = view.findViewById(R.id.ApgarGrade);
        a.setTypeface(myTypeface);
        apgarInfi.setTypeface(myTypeface);
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
        Apperence = view.findViewById(R.id.ApperenceS);
        Pulse = view.findViewById(R.id.PulseS);
        Grimace = view.findViewById(R.id.GrimaceS);
        Activity = view.findViewById(R.id.ActivityS);
        Respirations = view.findViewById(R.id.RespirationsS);
        Apperence.setAdapter(spinnerArrayAdapter1);
        Pulse.setAdapter(spinnerArrayAdapter2);
        Grimace.setAdapter(spinnerArrayAdapter3);
        Activity.setAdapter(spinnerArrayAdapter4);
        Respirations.setAdapter(spinnerArrayAdapter5);
        Apperence.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Pulse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Grimace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Activity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        Respirations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calc();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        GradientDrawable drawable = (GradientDrawable) apgarInfBG.getBackground();
        drawable.mutate();
        drawable.setColor(getActivity().getResources().getColor(R.color.White));
        apgarInfBG.setBackground(drawable);
        apgarInfi.setText(getActivity().getResources().getString(R.string.ApgarInfo));
        apgarInfi.setMovementMethod(new ScrollingMovementMethod());
    }
    private void calc() {
        newer = 0;
        newer += Apperence.getSelectedItemPosition();
        newer += Pulse.getSelectedItemPosition();
        newer += Grimace.getSelectedItemPosition();
        newer += Activity.getSelectedItemPosition();
        newer += Respirations.getSelectedItemPosition();
        String s = getActivity().getResources().getString(R.string.Apgar, newer);
        GradientDrawable drawable = (GradientDrawable) a.getBackground();
        drawable.mutate();
        if (newer < 4)
            Animate(old, newer);
        else if (newer > 6)
            Animate(old, newer);
        else
            Animate(old, newer);
        a.setBackground(drawable);
        a.setText(s);
    }
    private void Animate(int start, int end) {
        old = newer;
        Log.e("COLORS", "start: " + start + " end: " + end);
        int from = Color.rgb(191,21,22), to = Color.rgb(191,21,22);
        //starts at middle
        if (start > 3)
            from = Color.rgb(221,190,66);
        //starts at good
        if (start > 6)
            from = Color.rgb(70,178,58);
        //ends at  middle
        if (end > 3)
            to = Color.rgb(221,190,66);
        //ends at good
        if (end > 6)
            to = Color.rgb(70,178,58);
        color = new AnimatedColor(from, to);
        ValueAnimator animator = ObjectAnimator.ofFloat(0f, 1f).setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (float) animation.getAnimatedValue();
                Log.e("COLOR::", "r " + Color.red(color.with(0.4555f)));
                GradientDrawable gradientDrawable = (GradientDrawable)a.getBackground();
                gradientDrawable.mutate();
                gradientDrawable.setColor(color.with(v));
                a.setBackground(gradientDrawable);
            }
        });
        animator.start();
    }
}
