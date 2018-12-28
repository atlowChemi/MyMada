package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends Fragment {
    private String textS = "";
    private static EditText txtboxN, addManName;
    private static TextInputLayout ambNum, addManTIL;
    private static String name;
    private TextView explain;
    private static int job;
    private static LayoutInflater inflater;
    private static VolunteerAdapter adapter;
    private static List<Volunteer> albumList;
    private Typeface myTypeface;
    private static String[] colors = {"נהג","חובש","פראמדיק","רופא","מתנדב","משתלם","שירות לאומי"};
    String DELIVERED = "SMS_DELIVERED";
    private boolean isMove = false, isMove2 = false;
    private float oldX1, oldY1, oldX2, oldY2;
    private Button newSendButtonM2;
    private static Button newPplBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle)  {
        View view = inflater.inflate(R.layout.activity_main2, container, false);
        if (SettingsActivity.isRTL())
            view.setRotationY(180);
        return view;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sp2 = getActivity().getSharedPreferences("Settings",0);
        final SharedPreferences sp = getActivity().getSharedPreferences("Test", 0);
        newSendButtonM2 = view.findViewById(R.id.M2btn1ENew);
        newPplBtn = view.findViewById(R.id.newManNew);
        GradientDrawable drawable = (GradientDrawable) newPplBtn.getBackground();
        drawable.mutate();
        drawable.setColor(getActivity().getResources().getColor(R.color.White));
        newPplBtn.setBackground(drawable);
        newPplBtn.setTextColor(getActivity().getResources().getColor(R.color.ColorNice));
        Drawable img = getActivity().getResources().getDrawable(R.drawable.ic_send_flip);
        if (!SettingsActivity.isRTL()) {
            img = getActivity().getResources().getDrawable(R.drawable.ic_send);
            LinearLayout l = view.findViewById(R.id.LinBtns);
            l.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        newSendButtonM2.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        albumList = new ArrayList<>();
        adapter = new VolunteerAdapter(getActivity(), albumList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setRotationY(180);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        explain = (TextView) view.findViewById(R.id.explain);
        explain.setTypeface(myTypeface);
        txtboxN = (EditText) view.findViewById(R.id.M2txt1E);
        txtboxN.setTypeface(myTypeface);
        txtboxN.addTextChangedListener(new MyTextWatcher(txtboxN));
        ambNum = (TextInputLayout) view.findViewById(R.id.M2ambulance);
        ambNum.setTypeface(myTypeface);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newPplBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX2 - event.getX() > 10 || event.getX() - oldX2 > 10;
                        boolean y = oldY2 - event.getY() > 50 || event.getY() - oldY2 > 50;
                        if (x || y) {
                            isMove2 = true;
                            newPplBtn.setAlpha(1f);
                        }
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY2 = event.getY();
                        oldX2 = event.getX();
                        isMove2 = false;
                        newPplBtn.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        newPplBtn.setAlpha(1f);
                        if (!isMove2)
                            newPplBtn.performClick();
                        return true;
                }
                return false;
            }
        });
        newPplBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View newMan = inflater.inflate(R.layout.add_human, null);
                final Spinner addManSppiner = (Spinner) newMan.findViewById(R.id.addManSpinner);
                addManSppiner.setAdapter(spinnerArrayAdapter);
                addManName = (EditText) newMan.findViewById(R.id.addManName);
                addManTIL = (TextInputLayout) newMan.findViewById(R.id.addManTIL);
                addManName.addTextChangedListener(new MyTextWatcher(addManName));
                addManName.setTypeface(myTypeface);
                addManTIL.setTypeface(myTypeface);
                final AlertDialog builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle).setTitle(getActivity().getResources().getString(R.string.Nteam)).setView(newMan).setCancelable(false)
                        .setNegativeButton(getActivity().getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton(getActivity().getResources().getString(R.string.ok), null).create();
                builder.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                        canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
                        Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(addManName.getText().toString());
                                if (addManName.getText().toString().isEmpty())
                                    addManTIL.setError(getActivity().getResources().getString(R.string.emailNameNone));
                                if (addManName.getText().toString().trim().length() > 0) {
                                    addManTIL.setErrorEnabled(false);
                                    Volunteer a = new Volunteer(addManName.getText().toString(), addManSppiner.getSelectedItemPosition(), R.drawable.ic_arrow);
                                    albumList.add(a);
                                    adapter.notifyDataSetChanged();
                                    int size = albumList.size();
                                    switch (size) {
                                        case 1:
                                            explain.setVisibility(View.GONE);
                                            break;
                                        case 4:
                                            newPplBtn.setVisibility(View.GONE);
                                            break;
                                    }
                                    builder.dismiss();
                                }
                            }
                        });
                    }
                });
                builder.show();
            }
        });
        newSendButtonM2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX1 - event.getX() > 10 || event.getX() - oldX1 > 10;
                        boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                        if (x || y) {
                            isMove = true;
                            newSendButtonM2.setAlpha(1f);
                        }
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY1 = event.getY();
                        oldX1 = event.getX();
                        isMove = false;
                        newSendButtonM2.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        newSendButtonM2.setAlpha(1f);
                        if (!isMove)
                            newSendButtonM2.performClick();
                        return true;
                }
                return false;
            }
        });
        newSendButtonM2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtboxN.getText().toString().trim().isEmpty())
                    ambNum.setError(getActivity().getResources().getString(R.string.err_amb));
                else {
                    ambNum.setErrorEnabled(false);
                    final String merhavim[] = {"י-ם","איילון","אשר","גלבוע","דן","ירדן","ירקון","כרמל","לכיש","נגב","שרון","מוקד 4"};
                    SharedPreferences sp2 = getActivity().getSharedPreferences("Settings", 0);
                    final int merhav = sp2.getInt("merhav", 0);
                    textS = "צוות אמבולנס " + merhavim[merhav] + " ";
                    textS += txtboxN.getText() + ":\n";
                    if (albumList.size() == 0) {
                        di(getActivity().getResources().getString(R.string.SendNoNames));
                        return;
                    } else  {
                        for (int i = 0; i<albumList.size(); i++)  {
                            if (i!=0)
                                textS += "\n";
                            textS += colors[albumList.get(i).getJob()] + " - " + albumList.get(i).getName();
                        }
                    }
                    sms(textS);
                }
            }
        });
    }

    public static void deleteVol(int position)    {
        albumList.remove(position);
        adapter.notifyDataSetChanged();
        if (newPplBtn.getVisibility() != View.VISIBLE)
            newPplBtn.setVisibility(View.VISIBLE);
    }
    public static void editVol(final int position, final Context context)    {
        View newMan = inflater.inflate(R.layout.add_human, null);
        final Spinner addManSppiner = (Spinner) newMan.findViewById(R.id.addManSpinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addManSppiner.setAdapter(spinnerArrayAdapter);
        addManName = (EditText) newMan.findViewById(R.id.addManName);
        addManName.setText(albumList.get(position).getName());
        addManSppiner.setSelection(albumList.get(position).getJob());
        addManTIL = (TextInputLayout) newMan.findViewById(R.id.addManTIL);
        final AlertDialog builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle).setTitle(context.getResources().getString(R.string.Nteam)).setView(newMan).setCancelable(false)
                .setNegativeButton(context.getResources().getString(R.string.ret), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).setPositiveButton(context.getResources().getString(R.string.ok), null).create();
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                canc.setTextColor(context.getResources().getColor(R.color.OldPrimary));
                Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(addManName.getText().toString());
                        if (addManName.getText().toString().isEmpty())
                            addManTIL.setError(context.getResources().getString(R.string.emailNameNone));
                        if (addManName.getText().toString().trim().length() > 0) {
                            addManTIL.setErrorEnabled(false);
                            name = addManName.getText().toString();
                            job = addManSppiner.getSelectedItemPosition();
                            albumList.get(position).setName(name);
                            albumList.get(position).setJob(job);
                            adapter.notifyDataSetChanged();
                            builder.dismiss();
                        }
                    }
                });
            }
        });
        builder.show();
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
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
                case R.id.addManName:
                    if (addManName.getText().toString().isEmpty())
                        addManTIL.setError(getActivity().getResources().getString(R.string.emailNameNone));
                    else
                        addManTIL.setErrorEnabled(false);
                    break;
                case R.id.M2txt1E:
                    if(txtboxN.getText().toString().isEmpty())
                        ambNum.setError(getActivity().getResources().getString(R.string.err_amb));
                    else
                        ambNum.setErrorEnabled(false);
                    break;
            }
        }
    }
    private void di(String err)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
                Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                canc.setTypeface(myTypeface);
                ok.setTypeface(myTypeface);
                canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
            }
        });
        builder.show();
    }
}