package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.SettingsPage;
import atlow.chemi.mymada.adapters.mCodeAdapt;
import atlow.chemi.mymada.classes.mCodeKid;

public class MainActivity1 extends Fragment {
    private boolean bool, isMove = false, yishuv = true;
    private CharSequence[] _options = {
            "מצבי חירום סביבתיים", "2000 גוף זר באוזניים", "2001 גוף זר באף", "2002 גוף זר בעיניים", "2003 הכשת נחש", "2004 הרעלות","2005 התייבשות","2006 מכת ברק","2007 עקיצת עקרב","2008 פגיעה מבע\"ח","2009 פגיעות חום","2010 פגיעות קור","2011 רגישות יתר / אלרגיה","2012 שאיפת חומרים רעילים","2013 שאיפת עשן","2014 תגובה אנפילקטית","2015 צריכת יתר אלכוהול","2016 צריכת יתר סמים","2017 צריכת יתר תרופות","2018 תליה","2099 אחר",
            "טראומה","3000 חבלה בגפיים","3001 חבלה בצוואר","3002 חבלה רב מערכתית","3003 חבלת חזה","3004 חבלת ראש","3005 חבלת אגן","3006 חבלת בטן","3007 חבלת עמ\"ש","3008 חבלת פנים","3009 חשד לשבר צוואר ירך","3010 חשד לשבר בגפיים עליונות","3011  חשד לשבר בגפיים תחתונות","3012 כוויה","3017 כוויה דרגה 1","3018 כוויה דרגה 2","3019 כוויה דרגה 3","3013 כוויה כימית","3014 פגיעת מעיכה","3015 קטיעת אצבעות","3016 קטיעת גפיים","3099 אחר",
            "כאבים","4000 כאב/לחץ בחזה שאינו ממקור לבבי","4001 כאבי אוזניים","4002 כאבי בטן","4003 כאבי גב","4004 כאבי עיניים","4005 כאבי ראש","4006 כאבים באיבר מין","4007 כאבים בגרון","4008 כאבים בידיים","4009 כאבים בכל הגוף","4010 כאבים בצוואר","4011 כאבים ברגליים","4012 כאבי שיניים","4099 אחר",
            "מערכת הלב וכלי הדם","5010 בעיות בקוצב לב / דפיברילטור מושתל", "5000 בצקת ריאות / החמרה באי ספיקת לב", "5001 ברדיקרדיה", "5002 דום לב ונשימה", "5003 דפיקות לב","5011  הלם קרדיוגני","5004 הפרעות קצב אחרות","5012 חסם עלייתי - חדרי","5013 חשד לאוטם בשריר הלב","5009 חשד למוות בעריסה","5005 חשד לתסמונת כלילית חריפה","5014 טאכיקרדיה חדרית (VT)","5015 טאכיקרדיה על חדרית (PSVT)","5006 יתר לחץ דם","5007 לחץ דם נמוך","5016 פרפור / רפרוף פרוזדורים","5008 שינויים בא.ק.ג","5099 אחר",
            "מערכת הנשימה","6000 דום נשימה","6004 החמרה ב- COPD","6005 התקף אסטמה","6001 חנק / אספירציה / גוף זר","6006 חשד לדלקת ריאות","6007 חשד לתסחיף ריאתי","6008 סטרידור","6002 קוצר נשימה","6003 שיעול / שיעול דמי","6099 אחר",
            "נוירולוגיה","7001 בלבול","7002 התעלפות","7003 לאחר התעלפות","7004 חולשה כללית","7005 חוסר הכרה","7006 ירידה בתפקוד","7007  לאחר פרכוס","7008 סחרחורות","7009 פרכוסים","7010 שבץ מוחי / שבץ מוחי חולף (TIA / CVA)","7011 שינוי במצב ההכרה","7099 אחר",
            "מערכת העיכול","8000 בחילות ו/ או הקאות","8001 דימום רקטאלי","8002 הקאות דמיות","8005  חשד לגסטרואנטריטיס","8006 חשד לחסימת מעיים","8007 חשד למצב חירום בטני","8008 כיב פפטי","8009 מלנה","8003 עצירות","8004 שלשולים","8099 אחר",
            "זיהומי","9000 חום גבוה","9001 חשד למנינגיטיס","9002 חשד לספסיס / הלם ספטי","9003 צלוליטיס","9099 אחר",
            "פסיכיאטריה","1001 חולה משתולל","1005 חשד להפרעות נפשיות","1006 חשד להתקף פסיכוטי","1002 חשד לניסיון אובדני","1003 חשד לתגובת חרדה","1004 מחשבות אובדניות","1099 אחר",
            "אנדוקרינולוגיה","1102 בעיות בבלוטת התריס","1100 היפוגליקמיה","1101 היפרגליקמיה","1199 אחר",
            "מערכת המין והשתן","1200 אצירת שתן","1201 בעיות גניקולוגיות","1202 דימום וגינאלי","1203 דלקת בדרכי השתן","1204  דם בשתן","1205 חשד להפלה","1206 חשד לעווית כלייתית (colic renal)","1207 ירידת מים","1208 צירי לידה","1209 קבלת לידה","1299 אחר",
            "כללי","1300  אירוע ללא נפגעים","1305 המטופל מסרב לפינוי","1306 המטופל מסרב לקבל טיפול","1307 המשפחה מסרבת טיפול","1308 המשפחה מסרבת לפינוי","1312 ללא מחלה או פגיעה","1313 מוות ודאי - לא בוצעה החייאה","1314 קריאת שווא","1315  חשש לחיי אדם","1316 בדיקות דם לא תקינות","1317 ההעברה מבי\"ח לבי\"ח","1318 העברה מהבית למתקן רפואי","1319 העברה ממתקן רפואי הביתה","1320 העברה ממתקן רפואי לבית אבות","1350 קביעת מוות / הכרזה על מוות","1399 אחר"
            };
    private CharSequence[] _optionsNum = {
            "sep","2000","2001","2002","2003","2004","2005","2006","2007","2008","2009","2010","2011","2012","2013","2014","2015","2016","2017","2018","2099",
            "sep","3000","3001","3002","3003","3004","3005","3006","3007","3008","3009","3010","3011","3012","3017","3018","3019","3013","3014","3015","3016","3099",
            "sep","4000","4001","4002","4003","4004","4005","4006","4007","4008","4009","4010","4011","4012","4099",
            "sep","5010","5000","5001","5002","5003","5011","5004","5012","5013","5009","5005","5014","5015","5006","5007","5016","5008","5099",
            "sep","6000","6004","6005","6001","6006","6007","6008","6002","6003","6099",
            "sep","7001","7002","7003","7004","7005","7006","7007","7008","7009","7010","7011","7099",
            "sep","8000","8001","8002","8005","8006","8007","8008","8009","8003","8004","8099",
            "sep","9000","9001","9002","9003","9099",
            "sep","1001","1005","1006","1002","1003","1004","1099",
            "sep","1102","1100","1101","1199",
            "sep","1200","1201","1202","1203","1204","1205","1206","1207","1208","1209","1299",
            "sep","1300","1305","1306","1307","1308","1312","1313","1314","1315","1316","1317","1318","1319","1320","1350","1399"
        };
    private String[] _optionsCat = {
            "area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area","area",
            "trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma","trauma",
            "pain","pain","pain","pain","pain","pain","pain","pain","pain","pain","pain","pain","pain","pain","pain",
            "vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc","vasc",
            "resp","resp","resp","resp","resp","resp","resp","resp","resp","resp","resp",
            "neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro","neuro",
            "gastro","gastro","gastro","gastro","gastro","gastro","gastro","gastro","gastro","gastro","gastro","gastro",
            "infect","infect","infect","infect","infect","infect",
            "pshyc","pshyc","pshyc","pshyc","pshyc","pshyc","pshyc","pshyc",
            "ando","ando","ando","ando","ando",
            "pish","pish","pish","pish","pish","pish","pish","pish","pish","pish","pish","pish",
            "clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali","clali"
    };
    private mCodeAdapt codeAdapter;
    private TextInputLayout ambulance, hn, ht, first, last, sum, code;
    private Dialog dialog;
    private EditText edit1, editv, edita, edit2,edit3, edit4, edit5, edit6, sp;
    private String kodR = "", txt;
    private SharedPreferences sp2;
    private Typeface myTypeface;
    private float oldX1, oldY1;
    public MainActivity1()  {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View view = inflater.inflate(R.layout.activity_main, container, false);
        if (SettingsPage.isRTL())
            view.setRotationY(180);
        return view;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = MainActivity1.this.getContext();
        sp2 = context.getSharedPreferences("Settings", 0);
        ArrayList<mCodeKid> arrayOfMediCode = new ArrayList<>();
        boolean[] sep = new boolean[_options.length];
        for (int i = 0; i < _options.length; i++) {
            arrayOfMediCode.add(new mCodeKid(_options[i] + "",_optionsNum[i] + "",_optionsCat[i], i));
            sep[i] = _optionsNum[i].equals("sep");
        }
        codeAdapter = new mCodeAdapt(getContext(), arrayOfMediCode);
        sp = view.findViewById(R.id.M1button2);
        final Button newSendButton = view.findViewById(R.id.M1btnN);
        final EditText family = view.findViewById(R.id.M1txt5);
        assert family != null;
        Drawable img = VectorDrawableCompat.create(getResources(), R.drawable.ic_send, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
            newSendButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        else {
            int direction = getResources().getConfiguration().getLayoutDirection();
            if (direction == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL)
                newSendButton.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
            else
                newSendButton.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
        sp.setOnEditorActionListener((arg0, arg1, arg2) -> {
            if(arg1== EditorInfo.IME_ACTION_DONE){
                newSendButton.performClick();
                //do something
            }
            return false;
        });
        family.setOnEditorActionListener((arg0, arg1, arg2) -> {
            if (arg1 == EditorInfo.IME_ACTION_NEXT) {
                sp.performClick();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(family.getWindowToken(), 0);
                //do something
            }
            return false;
        });
        //sp.setHint(getResources().getString(R.string.pickC));
        sp.setOnClickListener(v -> {
            //kodR = getResources().getString(R.string.pickC);
            //showDialog(0);
            LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view12 = inflater.inflate(R.layout.list_medical, null);
            ListView lv = view12.findViewById(R.id.userListView);
            assert lv != null;
            codeAdapter.filter("");
            lv.setAdapter(codeAdapter);
            lv.setOnItemClickListener((parent, view1, position, id) -> {
                if (!codeAdapter.getId(position).equals("sep")) {
                    CheckBox cb = view1.findViewById(R.id.mcName);
                    cb.setChecked(!cb.isChecked());
                    String ret = codeAdapter.getChecked();
                    kodR = ret;
                    sp.setText(ret);
                }
            });
            EditText search = view12.findViewById(R.id.userSearch);
            assert search != null;
            search.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String cs = s.toString().toLowerCase(Locale.getDefault());
                    codeAdapter.filter(cs);
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
            alertDialog.setTitle("בחר קודים רפואיים");
            alertDialog.setView(view12);
            alertDialog.setPositiveButton(getActivity().getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            dialog = alertDialog.create();
            dialog.show();
        });
        newSendButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    boolean x = oldX1 - event.getX() > 10 || event.getX() - oldX1 > 10;
                    boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                    if (x || y) {
                        isMove = true;
                        newSendButton.setAlpha(1f);
                    }
                    return true;
                case MotionEvent.ACTION_DOWN:
                    oldY1 = event.getY();
                    oldX1 = event.getX();
                    isMove = false;
                    newSendButton.setAlpha(0.4f);
                    return true;
                case MotionEvent.ACTION_UP:
                    newSendButton.setAlpha(1f);
                    if (!isMove)
                        newSendButton.performClick();
                    return true;
            }
            return false;
        });
        newSendButton.setOnClickListener(v -> {
            bool = true;
            ambulance = getActivity().findViewById(R.id.M1ambulanceNumber);
            hn = getActivity().findViewById(R.id.M1HoleNifga);
            ht = getActivity().findViewById(R.id.M1Hitchayvut);
            first = getActivity().findViewById(R.id.M1firstName);
            last = getActivity().findViewById(R.id.M1familyName);
            sum = getActivity().findViewById(R.id.M1Schum);
            code = getActivity().findViewById(R.id.M1MedicalCode);
            edit1 = getActivity().findViewById(R.id.M1txt1E);
            editv = getActivity().findViewById(R.id.M1visaT);
            edita = getActivity().findViewById(R.id.M1addressT);
            edit2 = getActivity().findViewById(R.id.M1txt2E);
            edit3 = getActivity().findViewById(R.id.M1txt3);
            edit4 = getActivity().findViewById(R.id.M1txt4E);
            edit5 = getActivity().findViewById(R.id.M1txt5);
            edit6 = getActivity().findViewById(R.id.M1txt6E);
            assert edit1 != null;
            assert editv != null;
            assert edita != null;
            assert edit2 != null;
            assert edit3 != null;
            assert edit4 != null;
            assert edit5 != null;
            assert edit6 != null;
            sp.addTextChangedListener(new MyTextWatcher(sp));
            edit1.addTextChangedListener(new MyTextWatcher(edit1));
            editv.addTextChangedListener(new MyTextWatcher(editv));
            edita.addTextChangedListener(new MyTextWatcher(edita));
            edit2.addTextChangedListener(new MyTextWatcher(edit2));
            edit3.addTextChangedListener(new MyTextWatcher(edit3));
            edit4.addTextChangedListener(new MyTextWatcher(edit4));
            edit5.addTextChangedListener(new MyTextWatcher(edit5));
            edit6.addTextChangedListener(new MyTextWatcher(edit6));
            sp.setTypeface(myTypeface);
            edit1.setTypeface(myTypeface);
            editv.setTypeface(myTypeface);
            edita.setTypeface(myTypeface);
            edit2.setTypeface(myTypeface);
            edit3.setTypeface(myTypeface);
            edit4.setTypeface(myTypeface);
            edit5.setTypeface(myTypeface);
            edit6.setTypeface(myTypeface);
            ambulance.setTypeface(myTypeface);
            hn.setTypeface(myTypeface);
            ht.setTypeface(myTypeface);
            first.setTypeface(myTypeface);
            last.setTypeface(myTypeface);
            sum.setTypeface(myTypeface);
            code.setTypeface(myTypeface);
            /*create the sms text*/
            if (edit1.getText().toString().trim().isEmpty()) {
                ambulance.setError(getString(R.string.err_amb));
                bool = false;
            }else   {
                ambulance.setErrorEnabled(false);

            }
            if (edit2.getText().toString().length() < 1) {
                hn.setError(getString(R.string.err_hn));
                bool = false;
            }else   {
                hn.setErrorEnabled(false);
            }
            if (edit4.getText().toString().length() < 1) {
                first.setError(getString(R.string.err_fn));
                bool = false;
            }else   {
                first.setErrorEnabled(false);
            }
            if (edit5.getText().toString().length() < 1) {
                last.setError(getString(R.string.err_ln));
                bool = false;
            }else   {
                last.setErrorEnabled(false);
            }
            if (kodR.length() < 1 || !kodR.matches("^[0-9]{1,4}(\\s*,\\s*[0-9]{1,4})*$")) {
                code.setError(getString(R.string.err_mc));
                bool = false;
            }else   {
                code.setErrorEnabled(false);
            }
            if (edit6.getText().toString().length() > 0 || edit3.getText().toString().length() > 0) {
                yishuv = false;
                if (edit3.getText().toString().length() < 1) {
                    ht.setError(getString(R.string.err_ht));
                    bool = false;
                }else   {
                    ht.setErrorEnabled(false);
                }
                if (edit6.getText().toString().length() < 1) {
                    sum.setError(getString(R.string.err_sum));
                    bool = false;
                }else   {
                    sum.setErrorEnabled(false);
                }
            }
            if(bool)    {
                if(yishuv)  {
                    txt = ("אמב' " + edit1.getText().toString());
                    if (editv.getText().length() > 1)
                        txt += ("\nויזה: " + editv.getText().toString());
                    if(edita.getText().length() > 1)
                        txt += ("\nכתובת: " + edita.getText().toString());
                    txt += ("\nחולה נפגע " + edit2.getText().toString());
                    txt += ("\nקוד " + kodR);
                    txt += ("\nשם: " + edit4.getText().toString() + " " + edit5.getText().toString());
                }else   {
                    txt = ("אמב' " + edit1.getText().toString());
                    if (editv.getText().length() > 1)
                        txt += ("\nויזה: " + editv.getText().toString());
                    if(edita.getText().length() > 1)
                        txt += ("\nכתובת: " + edita.getText().toString());
                    txt += ("\nחולה נפגע " + edit2.getText().toString());
                    txt += ("\nהתחייבות " + edit3.getText().toString());
                    txt += ("\nסכום " + edit6.getText().toString());
                    txt += ("\nקוד " + kodR);
                    txt += ("\nשם: " + edit4.getText().toString() + " " + edit5.getText().toString());
                }
                sms(txt);
            }
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
                case R.id.M1txt1E:
                    if (edit1.getText().toString().trim().isEmpty())
                        ambulance.setError(getString(R.string.err_amb));
                    else
                        ambulance.setErrorEnabled(false);

                    break;
                case R.id.M1txt2E:
                    if (edit2.getText().toString().length() < 1)
                        hn.setError(getString(R.string.err_hn));
                    else
                        hn.setErrorEnabled(false);

                    break;
                case R.id.M1txt3:
                    if (!edit3.getText().toString().trim().isEmpty())
                        ht.setErrorEnabled(false);
                case R.id.M1txt4E:
                    if (edit4.getText().toString().trim().isEmpty())
                        first.setError(getString(R.string.err_fn));
                    else
                        first.setErrorEnabled(false);
                    break;
                case R.id.M1txt5:
                    if (edit5.getText().toString().length() < 1)
                        last.setError(getString(R.string.err_ln));
                    else
                        last.setErrorEnabled(false);
                    break;
                case R.id.M1txt6E:
                    if (!edit6.getText().toString().trim().isEmpty())
                        sum.setErrorEnabled(false);
                case R.id.M1button2:
                    if (kodR.length() < 1 || !kodR.matches("^[0-9]{1,4}(\\s*,\\s*[0-9]{1,4})*$"))
                        code.setError(getString(R.string.err_mc));
                    else
                        code.setErrorEnabled(false);
                    break;
            }
        }
    }
    private void sms(final String msg)   {
        final String[] phones = {"+972523993348","+972523993345","+972523993339","+972523993338","+972523993342","+972523993337","+972523993343","+972523993340"
                ,"+972523993346","+972523993356","+972523993341","+972586309592"};
        final String[] merhavim = {"ירושלים","איילון","אשר","גלבוע","דן","ירדן","ירקון","כרמל","לכיש","נגב","שרון","מוקד 4"};
        final int merhav = sp2.getInt("merhav", 0);
        int smss = 1 + msg.length()/70;
        String couT = msg.length() + " " + getResources().getString(R.string.smsCountChar) + " " + smss + " " + getResources().getString(R.string.smsCountMsg);
        final String me = getActivity().getResources().getString(R.string.toDispatch) + merhavim[merhav] + ".";
        String a = me + "\n\n" + msg + "\n\n";
        SpannableString string = new SpannableString(a + couT);
        string.setSpan(new ForegroundColorSpan(Color.BLUE), 0, me.length(), 0);
        string.setSpan(new ForegroundColorSpan(Color.GRAY), a.length(), string.length(), 0);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle).setTitle(getActivity().getResources().getString(R.string.suc)).setMessage(string)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), (dialog, id) -> dialog.cancel())
                .setPositiveButton(getResources().getString(R.string.ok), (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phones[merhav]));
                    intent.putExtra("sms_body", msg);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
//                        Crashlytics.log("sms not sent!");
//                        Crashlytics.logException(e);
                    }
                    dialog.cancel();
                }).create();
        builder.setOnShowListener(dialogInterface -> {
            Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
            Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
            canc.setTypeface(myTypeface);
            ok.setTypeface(myTypeface);
            canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
        });
        builder.show();
    }
}
