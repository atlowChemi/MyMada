package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import atlow.chemi.mymada.adapters.ChatAdapter;
import atlow.chemi.mymada.classes.Chat;
import atlow.chemi.mymada.helpers.FixedTextInputEditText;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService;
import atlow.chemi.mymada.recieversAndServices.NotificationServiceOld;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

public class contactChatActivity extends AppCompatActivity {
    private EditText ChatT;
    private final int REQUEST_PERMISSION_SETTING = 101;
    private Button sendChat;
    private ChatAdapter adapter;
    private ArrayList<Chat> chats;
    private boolean isMove = false;
    private float oldX1, oldY1;
    private MenuItem Detect;
    private DatabaseReference root;
    private String name, fcmToken;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_contact_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null)
            makeAnnouncement();
        chats = new ArrayList<>();
        adapter = new ChatAdapter(chats, contactChatActivity.this);
        adapter.nameChangeRequestCallback.addListener(() -> askForName(true));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView msgs = findViewById(R.id.chatMsg);
        msgs.setLayoutManager(mLayoutManager);
        msgs.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(4), true));
        msgs.setItemAnimator(new DefaultItemAnimator());
        msgs.setAdapter(adapter);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        msgs.setItemAnimator(itemAnimator);

        MyFirebaseMessagingService.getToken(getApplicationContext());
        fcmToken = getSharedPreferences("Settings", 0).getString("fcm", "empty");
        root = FirebaseDatabase.getInstance().getReference().child("chats").child(fcmToken);
        ChatT = findViewById(R.id.chatT);
        TextInputLayout chatTil = findViewById(R.id.chatTil);
        sendChat = findViewById(R.id.sendChat);

        SharedPreferences sp = getSharedPreferences("Settings", 0);
        name = sp.getString("UserName", "");
        if (name.trim().length() < 1) {
            askForName(false);
        }

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        ChatT.setTypeface(myTypeface);
        chatTil.setTypeface(myTypeface);
        sendChat.setTypeface(myTypeface);
        Drawable img = VectorDrawableCompat.create(getResources(), R.drawable.ic_send, null);
        sendChat.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img, null);
        sendChat.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        boolean x = oldX1 - event.getX() > 60 || event.getX() - oldX1 > 60;
                        boolean y = oldY1 - event.getY() > 50 || event.getY() - oldY1 > 50;
                        if (x || y) {
                            isMove = true;
                            sendChat.setAlpha(1f);
                        }
                        return true;
                    case MotionEvent.ACTION_DOWN:
                        oldY1 = event.getY();
                        oldX1 = event.getX();
                        isMove = false;
                        sendChat.setAlpha(0.4f);
                        return true;
                    case MotionEvent.ACTION_UP:
                        sendChat.setAlpha(1f);
                        if (!isMove)
                            sendChat.performClick();
                        return true;
                }
                return false;
        });
        sendChat.setOnClickListener((view) -> {
            if (name.trim().length() < 1) {
                askForName(false);
            }
            else {
                String msg = ChatT.getText().toString().trim();
                if (msg.length() > 0) {
                    //send new chat
                    Map<String, Object> map = new HashMap<>();
                    map.put("Name", name);
                    map.put("LastSent", System.currentTimeMillis());
                    map.put("SeenByAdmin", false);
                    String tempKey = root.push().getKey();
                    root.updateChildren(map);

                    DatabaseReference messageRoot = root.child(tempKey);
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("sender", fcmToken);
                    msgMap.put("msg", msg);
                    msgMap.put("time", System.currentTimeMillis());
                    messageRoot.updateChildren(msgMap).addOnSuccessListener(aVoid -> ChatT.setText(""));
                }
                else {
                    di(getString(R.string.emailMsgNone));
                }
            }
        });
        chats.add(new Chat("NAME_CHANGE","NAME_CHANGE","NAME_CHANGE", false));
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() != null && !(dataSnapshot.getKey().equals("Name") || dataSnapshot.getKey().equals("LastSent") ||
                        dataSnapshot.getKey().equals("SeenByAdmin"))) {
                    String sender = dataSnapshot.child("sender").getValue(String.class);
                    String msg = dataSnapshot.child("msg").getValue(String.class);
                    boolean WhatsApp;
                    try {
                        WhatsApp = (boolean)dataSnapshot.child("WhatsApp").getValue();
                    }
                    catch (NullPointerException e) {
                        WhatsApp = false;
                    }
                    String time;
                    try {
                        time = dataSnapshot.child("time").getValue(Long.class) + "";
                    }
                    catch (NullPointerException e) {
                        long tsLong = System.currentTimeMillis() / 1000;
                        time = tsLong + "";
                    }
                    chats.add(new Chat(sender, msg, time, WhatsApp));
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        msgs.smoothScrollToPosition(adapter.getItemCount());
                    }
                }
                else if (dataSnapshot.getKey() != null && dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("SeenByAdmin")) {
                    boolean seen = (boolean)dataSnapshot.getValue();
                    TextView seenTv = findViewById(R.id.msgSeen);
                    String text = seen ? getString(R.string.msgSeen) : getString(R.string.msgNotSeen);
                    seenTv.setText(text);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
    private void makeAnnouncement() {
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(R.string.contactMsg).setCancelable(false).setTitle(R.string.plsNote)
                .setNegativeButton(R.string.ok, (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            TextView textView = alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            Button cancel = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            cancel.setTypeface(myTypeface);
        });
        alert.show();
    }
    public void askForName(boolean cancelable) {
        View time = View.inflate(contactChatActivity.this, R.layout.shabat_t, null);
        final FixedTextInputEditText nameText = time.findViewById(R.id.shabatT);
        final TextInputLayout nameTil = time.findViewById(R.id.shabatTil);
        nameTil.setHint(getString(R.string.name));
        nameText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        final String userN = sp.getString("UserName", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(contactChatActivity.this, R.style.AlertDialogStyle)
                .setView(time).setTitle(R.string.writeName).setCancelable(cancelable);
        builder.setPositiveButton(getString(R.string.ok), null);
        final AlertDialog namePicker = builder.create();
        namePicker.setOnShowListener((dialog) -> {
            nameText.setText(userN);
            Button ok = namePicker.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setOnClickListener((v) -> {
                String nameEditable = nameText.getText() != null ? nameText.getText().toString().trim() : "";
                if (nameEditable.isEmpty())
                    nameTil.setError(getString(R.string.emailNameNone));
                else {
                    if (TextUtils.isDigitsOnly(nameEditable))
                        nameTil.setError(getString(R.string.chatNameNum));
                    else {
                        nameTil.setErrorEnabled(false);
                        SharedPreferences.Editor e = sp.edit();
                        e.putString("UserName", nameEditable);
                        name = nameEditable;
                        e.apply();
                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                        FirebaseDatabase.getInstance().getReference().child("chats").child(fcmToken).child("Name").setValue(nameEditable);
                    }
                }
            });
        });
        namePicker.show();
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
    }
    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).labelRes;
            if (label != 0)
                setTitle(label);
        }
        catch (PackageManager.NameNotFoundException e) {
//            Crashlytics.log(e.getMessage());
        }
    }
    private void di(String err)   {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setTitle(getResources().getString(R.string.err)).setMessage(err).setCancelable(false).setNegativeButton(getResources().getString(R.string.ok), (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
        int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
        Detect = menu.findItem(R.id.one);
        menu.findItem(R.id.one).setCheckable(true).setChecked(sp.getBoolean("Mapp", false)).setIcon(icon);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                atlow.chemi.mymada.settings.DetectionControls.toggle(this, item);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsPage.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private boolean isCallable(Intent intent) {
        try {
            return getPackageManager().queryIntentActivities(intent,  PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
        }
        catch (Exception e)    {
//            Crashlytics.logException(e);
            return false;
        }
    }

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
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing/9; // item bottom
            }
            else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing/9; // item top
                }
            }
        }
    }
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    private void PermissionDialog(boolean old) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                    if(!old) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(intent);
                    }
                    else {
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            Button cancel = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            cancel.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
        });
        alert.show();
    }
}
