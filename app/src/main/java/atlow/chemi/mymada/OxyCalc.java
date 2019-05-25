package atlow.chemi.mymada;

import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class OxyCalc extends Fragment {
    private EditText psi, atm;
    private TextView time;
    private Spinner oxyType;
    public OxyCalc()    {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View v = inflater.inflate(R.layout.activity_oxy_calc, container, false);
        if (SettingsDesigned.isRTL())
            v.setRotationY(180);
        oxyType = v.findViewById(R.id.OxyTypeS);
        psi = v.findViewById(R.id.PSI);
        atm = v.findViewById(R.id.ATM);
        time = v.findViewById(R.id.txtOxyMin);
        return v;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.refreshDrawableState();
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        /*oxyType = (Spinner)view.findViewById(R.id.OxyTypeS);
        psi = (EditText)view.findViewById(R.id.PSI);
        atm = (EditText)view.findViewById(R.id.ATM);*/
        atm.addTextChangedListener(new MyTextWatcher(atm));
        psi.addTextChangedListener(new MyTextWatcher(psi));
        //time = (TextView)view.findViewById(R.id.txtOxyMin);
        TextInputLayout atm_til = (TextInputLayout)view.findViewById(R.id.ATM_TIL);
        TextInputLayout psi_til = (TextInputLayout)view.findViewById(R.id.PSI_TIL);
        TextView one = (TextView) view.findViewById(R.id.txt_TimerSecond), two = (TextView) view.findViewById(R.id.twoOxy);
        one.setFocusable(true);
        two.setClickable(true);
        atm.setText(getActivity().getResources().getString(R.string.atm_100));
        psi.setText(getActivity().getResources().getString(R.string.psi_1500));
        psi.setTypeface(myTypeface);
        atm.setTypeface(myTypeface);
        time.setTypeface(myTypeface);
        atm_til.setTypeface(myTypeface);
        psi_til.setTypeface(myTypeface);
        one.setTypeface(myTypeface);
        two.setTypeface(myTypeface);
        String[] colors = getActivity().getResources().getStringArray(R.array.oxySizes);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        oxyType.setAdapter(spinnerArrayAdapter);
        oxyType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                atm.setText(atm.getText().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
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
                case R.id.ATM:
                    String ATM = atm.getText().toString().trim();
                    if (!ATM.isEmpty() && ATM.matches("[0-9]+"))  {
                        double pulse2c = Integer.parseInt(ATM.replaceAll("[\\D]",""));
                        double oxy = oxyType.getSelectedItemPosition() == 0 ? 2.4 : 20;
                        time.setText("" + (int)((pulse2c*oxy)/10));
                    }else
                        time.setText("0");
                    break;
                case R.id.PSI:
                    String PSI = psi.getText().toString().trim();
                    if (!PSI.isEmpty() && PSI.matches("[0-9]+"))  {
                        double pulse2c = Integer.parseInt(PSI.replaceAll("[\\D]",""));
                        atm.setText("" + (int)(pulse2c/15));
                        //time.setText("" + pulse2c*4);
                    }
                    break;
            }
        }
    }
}
