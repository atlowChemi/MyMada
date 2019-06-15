package atlow.chemi.mymada;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FixedTextInputEditText;
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

import com.crashlytics.android.Crashlytics;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class contactChatActivity extends AppCompatActivity {
    private EditText ChatT;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private Button sendChat;
    private static ChatAdapter adapter;
    private ArrayList<Chat> chats;
    private boolean isMove = false;
    private float oldX1, oldY1;
    private MenuItem Detect;
    private DatabaseReference root;
    static String name, fcmToken;

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

        chats = new ArrayList<>();
        adapter = new ChatAdapter(chats, contactChatActivity.this);
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

        fcmToken = MyFirebaseMessagingService.getToken(getApplicationContext());
        root = FirebaseDatabase.getInstance().getReference().child("chats").child(fcmToken);
        ChatT = findViewById(R.id.chatT);
        TextInputLayout chatTil = findViewById(R.id.chatTil);
        sendChat = findViewById(R.id.sendChat);

        SharedPreferences sp = getSharedPreferences("Settings", 0);
        name = sp.getString("UserName", "");
        if (name.trim().length() < 1) {
            askForName(contactChatActivity.this, false);
        }

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        ChatT.setTypeface(myTypeface);
        chatTil.setTypeface(myTypeface);
        sendChat.setTypeface(myTypeface);
        Drawable img = getApplicationContext().getResources().getDrawable(R.drawable.ic_send);
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
                askForName(contactChatActivity.this, false);
            } else {
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
                } else {
                    di(getString(R.string.emailMsgNone));
                }
            }
        });
        chats.add(new Chat("NAME_CHANGE","NAME_CHANGE","NAME_CHANGE"));
        root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey() != null && !(dataSnapshot.getKey().equals("Name") || dataSnapshot.getKey().equals("LastSent") ||
                        dataSnapshot.getKey().equals("SeenByAdmin"))) {
                    String sender = dataSnapshot.child("sender").getValue(String.class);
                    String msg = dataSnapshot.child("msg").getValue(String.class);
                    String time;
                    try {
                        time = dataSnapshot.child("time").getValue(Long.class) + "";
                    } catch (NullPointerException e) {
                        long tsLong = System.currentTimeMillis() / 1000;
                        time = tsLong + "";
                    }
                    chats.add(new Chat(sender, msg, time));
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        msgs.smoothScrollToPosition(adapter.getItemCount());
                    }
                }
                else if (dataSnapshot.getKey() != null && dataSnapshot.getKey().equals("SeenByAdmin")) {
                    boolean seen = dataSnapshot.getValue(Boolean.class);
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
    static void askForName(Context context, boolean cancelable) {
        View time = View.inflate(context, R.layout.shabat_t, null);
        final FixedTextInputEditText shabatText = time.findViewById(R.id.shabatT);
        final TextInputLayout shabatTil = time.findViewById(R.id.shabatTil);
        shabatTil.setHint(context.getString(R.string.name));
        shabatText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        SharedPreferences sp = context.getSharedPreferences("Settings", 0);
        final String userN = sp.getString("UserName", "");
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setView(time).setTitle(R.string.writeName).setCancelable(cancelable);
        builder.setPositiveButton(context.getString(R.string.ok), null);
        final AlertDialog langPicker = builder.create();
        langPicker.setOnShowListener((dialog) -> {
            shabatText.setText(userN);
            Button ok = langPicker.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setOnClickListener((v) -> {
                if (shabatText.getText().toString().trim().isEmpty())
                    shabatTil.setError(context.getString(R.string.emailNameNone));
                else if (shabatText.getText().toString().trim().length() > 0 && TextUtils.isDigitsOnly(shabatText.getText().toString().trim())) {
                    shabatTil.setError(context.getString(R.string.chatNameNum));
                }
                else {
                    String newI = shabatText.getText().toString().trim();
                    shabatTil.setErrorEnabled(false);
                    SharedPreferences.Editor e = sp.edit();
                    e.putString("UserName", newI);
                    name = newI;
                    e.apply();
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                    FirebaseDatabase.getInstance().getReference().child("chats").child(fcmToken).child("Name").setValue(newI);
                }
            });
        });
        langPicker.show();
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
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.log(e.getMessage());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                SharedPreferences.Editor s = sp.edit();
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                String enabledNotificationListeners = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
                String packageName = getApplicationContext().getPackageName();
                if (!item.isChecked()) {
                    item.setChecked(true);
                    item.setIcon(R.drawable.ic_detect_on);
                    /*new*/
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    ContextCompat.startForegroundService(this, serviceIntent);
                    /*end new*/
                    Intent test = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    boolean activityExists = isCallable(test);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (activityExists) {
                            try {
                                if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            } catch (ActivityNotFoundException e) {
                                Intent intent = new Intent(this, NotificationService.class);
                                Intent old = new Intent(this, NotificationServiceOld.class);
                                stopService(intent);
                                startService(old);
                                if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                    builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                                }
                                            })
                                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                                    final AlertDialog alert = builder.create();
                                    alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialogInterface) {
                                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                        }
                                    });
                                    alert.show();
                                }
                                s.putBoolean("Mapp", true);
                            }
                        } else {
                            Intent intent = new Intent(this, NotificationService.class);
                            Intent old = new Intent(this, NotificationServiceOld.class);
                            stopService(intent);
                            startService(old);
                            if (!NotificationServiceOld.isAccessibilitySettingsOn(getApplicationContext())) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                                builder.setMessage(getResources().getString(R.string.dialog_help)).setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                            }
                                        })
                                        .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                                final AlertDialog alert = builder.create();
                                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialogInterface) {
                                        Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                        canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
                                    }
                                });
                                alert.show();
                            }
                            s.putBoolean("Mapp", true);
                        }
                    } else {
                        s.putBoolean("Mapp", true);
                    }
                } else {
                    Intent serviceIntent = new Intent(this, SMSMonitorService.class);
                    stopService(serviceIntent);
                    item.setChecked(false);
                    s.putBoolean("Mapp", false);
                    item.setIcon(R.drawable.ic_detect_off);
                }
                s.apply();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsDesigned.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private boolean isCallable(Intent intent) {
        try {
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }catch (Exception e)    {
            e.printStackTrace();
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
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing/9; // item bottom
            } else {
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
}