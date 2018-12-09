package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
        if (SettingsActivity.isRTL())
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
        Drawable img = getActivity().getResources().getDrawable(R.drawable.ic_send_flip);
        if (!SettingsActivity.isRTL())
            img = getActivity().getResources().getDrawable(R.drawable.ic_send);
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
                        final PendingIntent deliveredIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent("SMS_DELIVERED"), 0);
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phones[merhav], null, msg, null, deliveredIntent);
                            Toast.makeText(getActivity(), getResources().getString(R.string.msgSent), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.msgNotSent), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
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
