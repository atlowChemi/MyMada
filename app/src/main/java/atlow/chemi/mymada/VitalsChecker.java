package atlow.chemi.mymada;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VitalsChecker extends Fragment {
    private int ageRank = 0, topP, lowP, topB, lowB, topSYS, lowSYS, topDIA, lowDIA;
    private EditText pulsa, breathing, systolic, diastolic;
    private TextInputLayout pulsaTIL, breathingTIL, systolicTIL, diastolicTIL;
    private TextView vitalR, vitalInf, vitalMsg;
    private LinearLayout vitalInfBG, vitalMsgBG;
    private boolean goodP, goodB, goodSYS, goodDIA, goodLD;
    private int[][][] correct = new int[4][6][6];
    private String breathingS, bpS, pulseS, goodS, bad;
    public VitalsChecker()  {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Systolic high
        correct[0][0] = new int[]{90,110, 140};
        //Systolic low
        correct[0][1] = new int[]{60,90, 90};
        //Diastolic high
        correct[1][0] = new int[]{60,80, 90};
        //Diastolic low
        correct[1][1] = new int[]{40, 50, 60};
        //Breathing high
        correct[2][0] = new int[]{60, 30, 20};
        //Breathing low
        correct[2][1] = new int[]{22, 18, 12};
        //Pulse high
        correct[3][0] = new int[]{130, 120, 100};
        //Pulse low
        correct[3][1] = new int[]{125, 90, 60};
        DatabaseReference mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabase = mFirebaseDatabase.child("Vitals");
        mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Diastolic
                correct[1][0][0] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Baby_high").getValue(Integer.class);
                correct[1][0][1] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Child_high").getValue(Integer.class);
                correct[1][0][2] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Old_high").getValue(Integer.class);
                correct[1][1][0] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Baby_low").getValue(Integer.class);
                correct[1][1][1] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Child_low").getValue(Integer.class);
                correct[1][1][2] = dataSnapshot.child("BloodPressure").child("Diastolic").child("Old_low").getValue(Integer.class);
                //Systolic
                correct[0][0][0] = dataSnapshot.child("BloodPressure").child("Systolic").child("Baby_high").getValue(Integer.class);
                correct[0][0][1] = dataSnapshot.child("BloodPressure").child("Systolic").child("Child_high").getValue(Integer.class);
                correct[0][0][2] = dataSnapshot.child("BloodPressure").child("Systolic").child("Old_high").getValue(Integer.class);
                correct[0][1][0] = dataSnapshot.child("BloodPressure").child("Systolic").child("Baby_low").getValue(Integer.class);
                correct[0][1][1] = dataSnapshot.child("BloodPressure").child("Systolic").child("Child_low").getValue(Integer.class);
                correct[0][1][2] = dataSnapshot.child("BloodPressure").child("Systolic").child("Old_low").getValue(Integer.class);
                //Breathing
                correct[2][0][0] = dataSnapshot.child("Breathing").child("Baby_high").getValue(Integer.class);
                correct[2][0][1] = dataSnapshot.child("Breathing").child("Child_high").getValue(Integer.class);
                correct[2][0][2] = dataSnapshot.child("Breathing").child("Old_high").getValue(Integer.class);
                correct[2][1][0] = dataSnapshot.child("Breathing").child("Baby_low").getValue(Integer.class);
                correct[2][1][1] = dataSnapshot.child("Breathing").child("Child_low").getValue(Integer.class);
                correct[2][1][2] = dataSnapshot.child("Breathing").child("Old_low").getValue(Integer.class);
                //Pulse
                correct[3][0][0] = dataSnapshot.child("Pulse").child("Baby_high").getValue(Integer.class);
                correct[3][0][1] = dataSnapshot.child("Pulse").child("Child_high").getValue(Integer.class);
                correct[3][0][2] = dataSnapshot.child("Pulse").child("Old_high").getValue(Integer.class);
                correct[3][1][0] = dataSnapshot.child("Pulse").child("Baby_low").getValue(Integer.class);
                correct[3][1][1] = dataSnapshot.child("Pulse").child("Child_low").getValue(Integer.class);
                correct[3][1][2] = dataSnapshot.child("Pulse").child("Old_low").getValue(Integer.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_vital, container, false);
        if (SettingsActivity.isRTL())
            v.setRotationY(180);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        breathingS = getActivity().getResources().getString(R.string.vitalsBreath);
        bpS = getActivity().getResources().getString(R.string.vitalsBP);
        pulseS = getActivity().getResources().getString(R.string.vitalsPulse);
        goodS = getActivity().getResources().getString(R.string.vitalsTakin);
        bad = getActivity().getResources().getString(R.string.vitalsNotTakin);
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        RadioButton a = (RadioButton)view.findViewById(R.id.old), b = (RadioButton)view.findViewById(R.id.kid), c = (RadioButton)view.findViewById(R.id.baby);
        a.setTypeface(myTypeface);
        b.setTypeface(myTypeface);
        c.setTypeface(myTypeface);
        final RadioGroup group = (RadioGroup) view.findViewById(R.id.radioAge);
        if (!SettingsActivity.isRTL())
            group.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                View radioButton = group.findViewById(i);
                ageRank = group.indexOfChild(radioButton);
                calc();
            }
        });
        //txt
        pulsa =  view.findViewById(R.id.pulsa);
        breathing = view.findViewById(R.id.breathing);
        systolic = view.findViewById(R.id.systolic);
        diastolic = view.findViewById(R.id.diastolic);
        pulsa.setTypeface(myTypeface);
        breathing.setTypeface(myTypeface);
        systolic.setTypeface(myTypeface);
        diastolic.setTypeface(myTypeface);
        //TIL
        pulsaTIL = view.findViewById(R.id.pulsaTIL);
        breathingTIL = view.findViewById(R.id.breathingTIL);
        systolicTIL = view.findViewById(R.id.systolicTIL);
        diastolicTIL = view.findViewById(R.id.diastolicTIL);
        pulsaTIL.setTypeface(myTypeface);
        breathingTIL.setTypeface(myTypeface);
        systolicTIL.setTypeface(myTypeface);
        diastolicTIL.setTypeface(myTypeface);
        //txt watcher
        pulsa.addTextChangedListener(new MyTextWatcher(pulsa));
        breathing.addTextChangedListener(new MyTextWatcher(breathing));
        systolic.addTextChangedListener(new MyTextWatcher(systolic));
        diastolic.addTextChangedListener(new MyTextWatcher(diastolic));

        //vr
        vitalR = view.findViewById(R.id.vitalsResult);
        vitalR.setMovementMethod(new ScrollingMovementMethod());
        vitalR.setTypeface(myTypeface);
        vitalInf = view.findViewById(R.id.VitalInf);
        vitalInf.setTypeface(myTypeface);
        vitalMsg = view.findViewById(R.id.VitalMsg);
        vitalMsg.setTypeface(myTypeface);
        vitalInfBG = view.findViewById(R.id.VitalInfBG);
        vitalMsgBG = view.findViewById(R.id.VitalMsgBG);
        if (SettingsActivity.isRTL()) {
            vitalInfBG.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            vitalMsgBG.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        GradientDrawable drawable = (GradientDrawable) vitalInfBG.getBackground();
        drawable.mutate();
        drawable.setColor(getActivity().getResources().getColor(R.color.White));
        vitalInfBG.setBackground(drawable);
        GradientDrawable drawable2 = (GradientDrawable) vitalMsgBG.getBackground();
        drawable2.mutate();
        drawable2.setColor(getActivity().getResources().getColor(R.color.White));
        vitalMsgBG.setBackground(drawable2);
        SetNums();
        String goodVitalsAge[] = getActivity().getResources().getStringArray(R.array.goodVitals);
        String goodVitalsNew = getActivity().getResources().getString(R.string.goodVitalsOLD, lowP, topP, lowB, topB, lowSYS, topSYS, lowDIA, topDIA, goodVitalsAge[ageRank]);
        vitalInf.setText(goodVitalsNew);
        vitalMsg.setText(getActivity().getResources().getString(R.string.vitalsAlert));
        vitalInf.setMovementMethod(new ScrollingMovementMethod());
        vitalMsg.setMovementMethod(new ScrollingMovementMethod());
    }

    private class MyTextWatcher implements TextWatcher {
        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.systolic:
                    if (!systolic.getText().toString().trim().isEmpty() || systolic.getText().toString().trim().matches("[0-9]+")) {
                        systolicTIL.setErrorEnabled(false);
                        if (systolic.getText().toString().length() == 2 && !systolic.getText().toString().startsWith("1"))
                            diastolic.requestFocus();
                        else if (systolic.getText().toString().length() == 3)
                            diastolic.requestFocus();
                    }
                    break;
            }
            calc();
        }
    }
    private void SetNums() {
        topP = ageRank == 0 ? correct[3][0][2] : ageRank == 1 ? correct[3][0][1] : correct[3][0][0];
        lowP = ageRank == 0 ? correct[3][1][2] : ageRank == 1 ? correct[3][1][1] : correct[3][1][0];
        topB = ageRank == 0 ? correct[2][0][2] : ageRank == 1 ? correct[2][0][1] : correct[2][0][0];
        lowB = ageRank == 0 ? correct[2][1][2] : ageRank == 1 ? correct[2][1][1] : correct[2][1][0];
        topSYS = ageRank == 0 ? correct[0][0][2] : ageRank == 1 ? correct[0][0][1] : correct[0][0][0];
        lowSYS = ageRank == 0 ? correct[0][1][2] : ageRank == 1 ? correct[0][1][1] : correct[0][1][0];
        topDIA = ageRank == 0 ? correct[1][0][2] : ageRank == 1 ? correct[1][0][1] : correct[1][0][0];
        lowDIA = ageRank == 0 ? correct[1][1][2] : ageRank == 1 ? correct[1][1][1] : correct[1][1][0];
    }
    private void calc() {
        //pulse
        topP = ageRank == 0 ? correct[3][0][2] : ageRank == 1 ? correct[3][0][1] : correct[3][0][0];
        lowP = ageRank == 0 ? correct[3][1][2] : ageRank == 1 ? correct[3][1][1] : correct[3][1][0];
        if (!pulsa.getText().toString().trim().isEmpty() || pulsa.getText().toString().trim().matches("[0-9]+")) {
            String shortPulse = pulsa.getText().toString().trim();
            int pulse2c = Integer.parseInt(shortPulse.replaceAll("[\\D]", ""));
            goodP = pulse2c >= lowP && pulse2c <= topP;
        }
        //breathing
        topB = ageRank == 0 ? correct[2][0][2] : ageRank == 1 ? correct[2][0][1] : correct[2][0][0];
        lowB = ageRank == 0 ? correct[2][1][2] : ageRank == 1 ? correct[2][1][1] : correct[2][1][0];
        if (!breathing.getText().toString().trim().isEmpty() || breathing.getText().toString().trim().matches("[0-9]+")) {
            String shortBreath = breathing.getText().toString().trim();
            int breath2c = Integer.parseInt(shortBreath.replaceAll("[\\D]", ""));
            goodB = breath2c >= lowB && breath2c <= topB;
        }
        //sys
        topSYS = ageRank == 0 ? correct[0][0][2] : ageRank == 1 ? correct[0][0][1] : correct[0][0][0];
        lowSYS = ageRank == 0 ? correct[0][1][2] : ageRank == 1 ? correct[0][1][1] : correct[0][1][0];
        if (!systolic.getText().toString().trim().isEmpty() || systolic.getText().toString().trim().matches("[0-9]+")) {
            String SYS = systolic.getText().toString().trim();
            int sys2c = Integer.parseInt(SYS.replaceAll("[\\D]", ""));
            goodSYS = sys2c >= lowSYS && sys2c <= topSYS;
        }
        //dia
        topDIA = ageRank == 0 ? correct[1][0][2] : ageRank == 1 ? correct[1][0][1] : correct[1][0][0];
        lowDIA = ageRank == 0 ? correct[1][1][2] : ageRank == 1 ? correct[1][1][1] : correct[1][1][0];
        if (!diastolic.getText().toString().trim().isEmpty() || diastolic.getText().toString().trim().matches("[0-9]+")) {
            String DIA = diastolic.getText().toString().trim();
            int dia2c = Integer.parseInt(DIA.replaceAll("[\\D]", ""));
            goodDIA = dia2c >= lowDIA && dia2c <= topDIA;
        }
        //sys
        if ((!systolic.getText().toString().trim().isEmpty() || systolic.getText().toString().trim().matches("[0-9]+" )) && (!diastolic.getText().toString().trim().isEmpty() || diastolic.getText().toString().trim().matches("[0-9]+"))) {
            String SYS9 = systolic.getText().toString().trim();
            int sys2c9 = Integer.parseInt(SYS9.replaceAll("[\\D]", ""));
            String DIA9 = diastolic.getText().toString().trim();
            int dia2c9 = Integer.parseInt(DIA9.replaceAll("[\\D]", ""));
            goodLD = (sys2c9 - dia2c9 < 61) && (sys2c9 - dia2c9 > 29);
        }
        //calc
        String goodVitalsAge[] = getActivity().getResources().getStringArray(R.array.goodVitals);
        String goodVitalsNew = getActivity().getResources().getString(R.string.goodVitalsOLD, lowP, topP, lowB, topB, lowSYS, topSYS, lowDIA, topDIA, goodVitalsAge[ageRank]);
        vitalInf.setText(goodVitalsNew);
        int green = Color.rgb(70,178,58);
        int red = Color.rgb(191,21,22);
        int aqua = Color.rgb(50,167,195);
        /*pulse**/
        if (!pulsa.getText().toString().trim().isEmpty() && goodP) {
            //green
            pulsa.setTextColor(green);
            pulsaTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {green}));
            Drawable bg = pulsaTIL.getEditText().getBackground();
            DrawableCompat.setTint(bg, green);
            pulsaTIL.getEditText().setBackground(bg);
        }else if (!pulsa.getText().toString().trim().isEmpty() && !goodP) {
            //red
            pulsa.setTextColor(red);
            pulsaTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {red}));
            Drawable bg = pulsaTIL.getEditText().getBackground();
            DrawableCompat.setTint(bg, red);
            pulsaTIL.getEditText().setBackground(bg);
        } else {
            //blue
            pulsa.setTextColor(aqua);
            pulsaTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {aqua}));
            Drawable bg = pulsaTIL.getEditText().getBackground();
            DrawableCompat.setTint(bg, aqua);
            pulsaTIL.getEditText().setBackground(bg);
        }
        /*breathing**/
        if (!breathing.getText().toString().trim().isEmpty() && goodB) {
            //green
            breathing.setTextColor(green);
            breathingTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {green}));
            Drawable bgB = breathingTIL.getEditText().getBackground();
            DrawableCompat.setTint(bgB, green);
            breathingTIL.getEditText().setBackground(bgB);
        }else if (!breathing.getText().toString().trim().isEmpty() && !goodB) {
            //red
            breathing.setTextColor(red);
            breathingTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {red}));
            Drawable bgB = breathingTIL.getEditText().getBackground();
            DrawableCompat.setTint(bgB, red);
            breathingTIL.getEditText().setBackground(bgB);
        } else {
            //blue
            breathing.setTextColor(aqua);
            breathingTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {aqua}));
            Drawable bgB = breathingTIL.getEditText().getBackground();
            DrawableCompat.setTint(bgB, aqua);
            breathingTIL.getEditText().setBackground(bgB);
        }
        /*blood pressure**/
        if (!systolic.getText().toString().trim().isEmpty() && !diastolic.getText().toString().trim().isEmpty()) {
            if (goodSYS && goodDIA && goodLD) {
                //green
                systolic.setTextColor(green);
                diastolic.setTextColor(green);
                systolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[]{green}));
                diastolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[]{green}));
                Drawable bgBP1 = systolicTIL.getEditText().getBackground();
                DrawableCompat.setTint(bgBP1, green);
                systolicTIL.getEditText().setBackground(bgBP1);
                diastolicTIL.getEditText().setBackground(bgBP1);
            } else {
                //red
                systolic.setTextColor(red);
                diastolic.setTextColor(red);
                systolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {red}));
                diastolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {red}));
                Drawable bgBP1 = systolicTIL.getEditText().getBackground();
                DrawableCompat.setTint(bgBP1, red);
                systolicTIL.getEditText().setBackground(bgBP1);
                diastolicTIL.getEditText().setBackground(bgBP1);
            }
        }else if (!systolic.getText().toString().trim().isEmpty() && diastolic.getText().toString().trim().isEmpty()) {
            if (goodSYS) {
                //green
                systolic.setTextColor(green);
                systolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[]{green}));
                Drawable bgBP1 = systolicTIL.getEditText().getBackground();
                DrawableCompat.setTint(bgBP1, green);
                systolicTIL.getEditText().setBackground(bgBP1);
            } else {
                //red
                systolic.setTextColor(red);
                systolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {red}));
                Drawable bgBP1 = systolicTIL.getEditText().getBackground();
                DrawableCompat.setTint(bgBP1, red);
                systolicTIL.getEditText().setBackground(bgBP1);
            }
        } else {
            //blue
            systolic.setTextColor(aqua);
            diastolic.setTextColor(aqua);
            systolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {aqua}));
            diastolicTIL.setDefaultHintTextColor(new ColorStateList(new int[][]{{0}}, new int[] {aqua}));
            Drawable bgBP1 = systolicTIL.getEditText().getBackground();
            DrawableCompat.setTint(bgBP1, aqua);
            systolicTIL.getEditText().setBackground(bgBP1);
            diastolicTIL.getEditText().setBackground(bgBP1);
        }
    }
}
