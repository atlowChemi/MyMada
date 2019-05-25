package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;

public class MainActivity3 extends Fragment {
    private TextInputLayout freeMsg;
    private boolean isMove = false;
    private float oldX1, oldY1;
    private Button newSendButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View view = inflater.inflate(R.layout.activity_main3, container, false);
        if (SettingsDesigned.isRTL())
            view.setRotationY(180);
        return view;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sp = getActivity().getSharedPreferences("Settings", 0);
        final EditText editText = (EditText) view.findViewById(R.id.yourMsg);
        freeMsg = (TextInputLayout) view.findViewById(R.id.freeMsg);
        newSendButton = view.findViewById(R.id.yourMsgBtnNew);
        Drawable img = getActivity().getResources().getDrawable(R.drawable.ic_send);
        newSendButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        newSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });
        newSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().trim().length() < 1)  {
                    String customMsgErr = getResources().getString(R.string.customMsgErr);
                    freeMsg.setError(customMsgErr);
                }else   {
                    sms(editText.getText().toString());
                }
            }
        });
    }
    private void sms(final String msg)   {
        final String phones[] = {"+972523993348","+972523993345","+972523993339","+972523993338","+972523993342","+972523993337","+972523993343","+972523993340"
                ,"+972523993346","+972523993356","+972523993341","+972586309592"};
        final String merhavim[] = {"ירושלים","איילון","אשר","גלבוע","דן","ירדן","ירקון","כרמל","לכיש","נגב","שרון","מוקד 4"};
        SharedPreferences sp2 = getActivity().getSharedPreferences("Settings", 0);
        final int merhav = sp2.getInt("merhav", 0);
        int smss = 1 + msg.length()/70;
        String couT = msg.length() + " " + getResources().getString(R.string.smsCountChar) + " " + smss + " " + getResources().getString(R.string.smsCountMsg);
        String me = getActivity().getResources().getString(R.string.toDispatch) + merhavim[merhav] + ".";
        String a = me + "\n\n" + msg + "\n\n";
        SpannableString string = new SpannableString(a + couT);
        string.setSpan(new ForegroundColorSpan(Color.BLUE), 0, me.length(), 0);
        string.setSpan(new ForegroundColorSpan(Color.GRAY), a.length(), string.length(), 0);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle).setTitle(getActivity().getResources().getString(R.string.suc)).setMessage(string)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phones[merhav]));
                        intent.putExtra("sms_body", msg);
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Crashlytics.log("sms not sent!");
                            Crashlytics.logException(e);
                        }
                        dialog.cancel();
                    }
                }).create();
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
            }
        });
        builder.show();
    }
}
