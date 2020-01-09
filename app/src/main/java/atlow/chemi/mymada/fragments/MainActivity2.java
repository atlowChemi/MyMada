package atlow.chemi.mymada.fragments;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;

import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
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

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.SettingsPage;
import atlow.chemi.mymada.classes.Volunteer;
import atlow.chemi.mymada.adapters.VolunteerAdapter;

public class MainActivity2 extends Fragment implements View.OnTouchListener {
    private EditText txtboxN, addManName;
    private TextInputLayout ambNum, addManTIL;
    private String name;
    private TextView explain;
    private int job;
    private LayoutInflater inflater;
    private VolunteerAdapter adapter;
    private List<Volunteer> albumList;
    private Typeface myTypeface;
    private String[] colors = {"נהג", "חובש", "פראמדיק", "רופא", "מתנדב", "משתלם", "שירות לאומי"};
    private boolean isMove = false;
    private float oldX, oldY;
    private Button newPplBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.activity_main2, container, false);
        if (SettingsPage.isRTL())
            view.setRotationY(180);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/opensanshebrew.ttf");
        Button newSendButtonM2 = view.findViewById(R.id.M2btn1ENew);
        newPplBtn = view.findViewById(R.id.newManNew);
        GradientDrawable drawable = (GradientDrawable) newPplBtn.getBackground();
        drawable.mutate();
        drawable.setColor(ContextCompat.getColor(getActivity(), R.color.White));
        newPplBtn.setBackground(drawable);
        newPplBtn.setTextColor(ContextCompat.getColor(getActivity(), R.color.ColorNice));
        if (!SettingsPage.isRTL()) {
            LinearLayout l = view.findViewById(R.id.LinBtns);
            l.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        Drawable img = VectorDrawableCompat.create(getResources(), R.drawable.ic_send, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
            newSendButtonM2.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        else {
            int direction = getResources().getConfiguration().getLayoutDirection();
            if (direction == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL)
                newSendButtonM2.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
            else
                newSendButtonM2.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
        }
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        albumList = new ArrayList<>();
        adapter = new VolunteerAdapter(getActivity(), albumList);
        adapter.DeleteVolunteerEvent.addListener(this::deleteVol);
        adapter.EditVolunteerEvent.addListener(this::editVol);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setRotationY(180);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        explain = view.findViewById(R.id.explain);
        explain.setTypeface(myTypeface);
        txtboxN = view.findViewById(R.id.M2txt1E);
        txtboxN.setTypeface(myTypeface);
        txtboxN.addTextChangedListener(new MyTextWatcher(txtboxN));
        ambNum = view.findViewById(R.id.M2ambulance);
        ambNum.setTypeface(myTypeface);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        newPplBtn.setOnTouchListener(this);
        newPplBtn.setOnClickListener(view1 -> {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View newMan = inflater.inflate(R.layout.add_human, null);
            final Spinner addManSppiner = newMan.findViewById(R.id.addManSpinner);
            addManSppiner.setAdapter(spinnerArrayAdapter);
            addManName = newMan.findViewById(R.id.addManName);
            addManTIL = newMan.findViewById(R.id.addManTIL);
            addManName.addTextChangedListener(new MyTextWatcher(addManName));
            addManName.setTypeface(myTypeface);
            addManTIL.setTypeface(myTypeface);
            final AlertDialog builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle).setTitle(getActivity().getResources().getString(R.string.Nteam)).setView(newMan).setCancelable(false)
                    .setNegativeButton(getActivity().getResources().getString(R.string.ret), (dialogInterface, i) -> dialogInterface.dismiss()).setPositiveButton(getActivity().getResources().getString(R.string.ok), null).create();
            builder.setOnShowListener(dialogInterface -> {
                Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
                canc.setTextColor(getActivity().getResources().getColor(R.color.OldPrimary));
                Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setOnClickListener(view11 -> {
                    if (addManName.getText().toString().isEmpty())
                        addManTIL.setError(getActivity().getResources().getString(R.string.emailNameNone));
                    if (addManName.getText().toString().trim().length() > 0) {
                        addManTIL.setErrorEnabled(false);
                        Volunteer a = new Volunteer(addManName.getText().toString(), addManSppiner.getSelectedItemPosition(), R.drawable.ic_person);
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
                });
            });
            builder.show();
        });
        newSendButtonM2.setOnTouchListener(this);
        newSendButtonM2.setOnClickListener(view12 -> {
            if (txtboxN.getText().toString().trim().isEmpty())
                ambNum.setError(getActivity().getResources().getString(R.string.err_amb));
            else {
                ambNum.setErrorEnabled(false);
                final String[] merhavim = {"י-ם", "איילון", "אשר", "גלבוע", "דן", "ירדן", "ירקון", "כרמל", "לכיש", "נגב", "שרון", "מוקד 4"};
                SharedPreferences sp2 = getActivity().getSharedPreferences("Settings", 0);
                final int merhav = sp2.getInt("merhav", 0);
                StringBuilder textSb = new StringBuilder("צוות אמבולנס ").append(merhavim[merhav]).append(" ").append(txtboxN.getText()).append(":\n");
                if (albumList.size() == 0) {
                    di(getActivity().getResources().getString(R.string.SendNoNames));
                    return;
                } else {
                    for (int i = 0; i < albumList.size(); i++) {
                        if (i != 0)
                            textSb.append("\n");
                        textSb.append(colors[albumList.get(i).getJob()]).append(" - ").append(albumList.get(i).getName());
                    }
                }
                sms(textSb.toString());
            }
        });
    }

    private void deleteVol(int position) {
        albumList.remove(position);
        adapter.notifyDataSetChanged();
        if (newPplBtn.getVisibility() != View.VISIBLE)
            newPplBtn.setVisibility(View.VISIBLE);
    }

    public void editVol(final int position) {
        View newMan = inflater.inflate(R.layout.add_human, null);
        final Spinner addManSppiner = newMan.findViewById(R.id.addManSpinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addManSppiner.setAdapter(spinnerArrayAdapter);
        addManName = newMan.findViewById(R.id.addManName);
        addManName.setText(albumList.get(position).getName());
        addManSppiner.setSelection(albumList.get(position).getJob());
        addManTIL = newMan.findViewById(R.id.addManTIL);
        final AlertDialog builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle).setTitle(getContext().getResources().getString(R.string.Nteam)).setView(newMan).setCancelable(false)
                .setNegativeButton(getContext().getResources().getString(R.string.ret), (dialogInterface, i) -> dialogInterface.dismiss()).setPositiveButton(getContext().getResources().getString(R.string.ok), null).create();
        builder.setOnShowListener(dialogInterface -> {
            Button canc = builder.getButton(DialogInterface.BUTTON_NEGATIVE);
            canc.setTextColor(getContext().getResources().getColor(R.color.OldPrimary));
            Button ok = builder.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setOnClickListener(view -> {
                if (addManName.getText().toString().isEmpty())
                    addManTIL.setError(getContext().getResources().getString(R.string.emailNameNone));
                if (addManName.getText().toString().trim().length() > 0) {
                    addManTIL.setErrorEnabled(false);
                    name = addManName.getText().toString();
                    job = addManSppiner.getSelectedItemPosition();
                    albumList.get(position).setName(name);
                    albumList.get(position).setJob(job);
                    adapter.notifyDataSetChanged();
                    builder.dismiss();
                }
            });
        });
        builder.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                boolean x = oldX - event.getX() > 10 || event.getX() - oldX > 10;
                boolean y = oldY - event.getY() > 50 || event.getY() - oldY > 50;
                if (x || y) {
                    isMove = true;
                    v.setAlpha(1f);
                }
                return true;
            case MotionEvent.ACTION_DOWN:
                oldY = event.getY();
                oldX = event.getX();
                isMove = false;
                v.setAlpha(0.4f);
                return true;
            case MotionEvent.ACTION_UP:
                v.setAlpha(1f);
                if (!isMove)
                    v.performClick();
                return true;
        }
        return false;
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
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
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

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.addManName:
                    if (addManName.getText().toString().isEmpty())
                        addManTIL.setError(getActivity().getString(R.string.emailNameNone));
                    else
                        addManTIL.setErrorEnabled(false);
                    break;
                case R.id.M2txt1E:
                    if (txtboxN.getText().toString().isEmpty())
                        ambNum.setError(getActivity().getResources().getString(R.string.err_amb));
                    else
                        ambNum.setErrorEnabled(false);
                    break;
            }
        }
    }

    private void di(String err) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sms(final String msg) {

        final String[] phones = {"+972523993348", "+972523993345", "+972523993339", "+972523993338", "+972523993342", "+972523993337", "+972523993343", "+972523993340"
                , "+972523993346", "+972523993356", "+972523993341", "+972586309592"};
        final String[] merhavim = {"ירושלים", "איילון", "אשר", "גלבוע", "דן", "ירדן", "ירקון", "כרמל", "לכיש", "נגב", "שרון", "מוקד 4"};
        SharedPreferences sp2 = getActivity().getSharedPreferences("Settings", 0);
        final int merhav = sp2.getInt("merhav", 0);
        int smss = 1 + msg.length() / 70;
        String couT = msg.length() + " " + getResources().getString(R.string.smsCountChar) + " " + smss + " " + getResources().getString(R.string.smsCountMsg);
        String me = getActivity().getResources().getString(R.string.toDispatch) + merhavim[merhav] + ".";
        String a = me + "\n\n" + msg + "\n\n";
        SpannableString string = new SpannableString(a + couT);
        string.setSpan(new ForegroundColorSpan(Color.BLUE), 0, me.length(), 0);
        string.setSpan(new ForegroundColorSpan(Color.GRAY), a.length(), string.length(), 0);
        final AlertDialog builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle).setTitle(getActivity().getResources().getString(R.string.suc)).setMessage(string)
                .setCancelable(false)
                .setNegativeButton(getResources().getString(R.string.ret), (dialog, id) -> dialog.cancel()).setPositiveButton(getResources().getString(R.string.ok), (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phones[merhav]));
                    intent.putExtra("sms_body", msg);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Crashlytics.log("sms not sent!");
                        Crashlytics.logException(e);
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