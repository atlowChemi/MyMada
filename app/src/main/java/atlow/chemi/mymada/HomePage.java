package atlow.chemi.mymada;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;


public class HomePage extends AppCompatActivity {
    private Typeface myTypeface;
    private homeBAdapter adapter;
    public NotificationManager manager;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    public final String Grp2 = "grpOthers", Grp3 = "grpTog", CnlBg = "runInBg", CnlWin = "notWithWin";
    private MenuItem Detect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTitle();
        setContentView(R.layout.activity_home_page);
        setNotifChannels();
        rateUs.app_launched(HomePage.this);
        Context context = HomePage.this;
        Crashlytics.setUserIdentifier(MyFirebaseMessagingService.getToken(context));
        myTypeface = Typeface.createFromAsset(getAssets(), "fonts/opensanshebrew.ttf");
        final SharedPreferences sharedPreferences = getSharedPreferences("Settings", 0);
        boolean Mapp = sharedPreferences.getBoolean("Mapp", false);
        AdView mAdView;
        try {
            Log.e("FCM token: ", MyFirebaseMessagingService.getToken(context));
        }catch (Exception e)    {
            e.printStackTrace();
        }
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        FirebaseAnalytics.getInstance(getApplicationContext());
        try {
            MobileAds.initialize(getApplicationContext(), "ca-app-pub-7600269766434003/5732229370");
            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            Crashlytics.log("Ads Loaded");
        } catch (Exception e) {
            Crashlytics.log("Ads Not Loaded");
            Crashlytics.logException(e);
        }

        RecyclerView rv = findViewById(R.id.rv);
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                return i == 0 ? 2 : 1;
            }
        });
        List<HomeB> albumList = new ArrayList<>();
        adapter = new homeBAdapter(HomePage.this, albumList);
        rv.setLayoutManager(layoutManager);
        //rv.setRotationY(180);
        rv.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(), true));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(adapter);

        try {
            HomeB Tool = new HomeB("tol", R.drawable.new_tol);
            albumList.add(Tool);
            HomeB Moked = new HomeB("mok", R.drawable.new_mok);
            albumList.add(Moked);
            HomeB Test = new HomeB("tes", R.drawable.new_tes);
            albumList.add(Test);
            HomeB Contact = new HomeB("con", R.drawable.new_con);
            albumList.add(Contact);
            HomeB Info = new HomeB("inf", R.drawable.new_inf);
            albumList.add(Info);
            Crashlytics.log("HomeBtns Loaded properly!");
        } catch (Exception e) {
            Crashlytics.log("HomeBtns caused crash!");
            Crashlytics.logException(e);
        }

        adapter.notifyDataSetChanged();
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_SETTLING)
                    adapter.setBtnsAlpha();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        boolean Privacy = sharedPreferences.getBoolean("privacy", false);
        boolean oldU = sharedPreferences.getBoolean("oldU", false);
        if (!oldU) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.newSettings)).setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.title_activity_settings), (dialog, id) -> {
                        Intent intent = new Intent(getApplicationContext(), SettingsDesigned.class);
                        startActivity(intent);
                    }).setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
            final AlertDialog alert = builder.create();
            TextView textView = alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            alert.setOnShowListener(dialogInterface -> {
                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setTypeface(myTypeface);
                canc.setTypeface(myTypeface);
                canc.setTextColor(getResources().getColor(R.color.OldPrimary));
            });
            alert.show();
        }
        if (!Privacy) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
            builder.setMessage(getResources().getString(R.string.privacyMsg)).setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.title_activity_privacy), (dialog, id) -> {
                        Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
            final AlertDialog alert = builder.create();
            TextView textView = alert.findViewById(android.R.id.message);
            if (textView != null)
                textView.setTypeface(myTypeface);
            textView = alert.findViewById(android.R.id.title);
            if (textView != null)
                textView.setTypeface(myTypeface);
            alert.setOnShowListener(dialogInterface -> {
                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setTypeface(myTypeface);
                canc.setTypeface(myTypeface);
                canc.setTextColor(getResources().getColor(R.color.OldPrimary));
            });
            alert.show();
        }
        if(Mapp && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = context.getSystemService(NotificationManager.class);
            int importance = 1;
            if (manager != null) {
                Log.e("importance", "is" + manager.getNotificationChannel(CnlBg).getImportance());
                importance = manager.getNotificationChannel(CnlBg).getImportance();
            }
            Log.d("importance", "is " + importance);
            if (importance != NotificationManager.IMPORTANCE_MIN) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                builder.setMessage(getResources().getString(R.string.importanceMsg)).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.importance), (dialog, id) -> {
                            Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                            intent.putExtra(Settings.EXTRA_CHANNEL_ID, CnlBg);
                            startActivity(intent);
                        })
                        .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                final AlertDialog alert = builder.create();
                TextView textView = alert.findViewById(android.R.id.message);
                if (textView != null)
                    textView.setTypeface(myTypeface);
                textView = alert.findViewById(android.R.id.title);
                if (textView != null)
                    textView.setTypeface(myTypeface);
                alert.setOnShowListener(dialogInterface -> {
                    Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                    Button ok = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                    ok.setTypeface(myTypeface);
                    canc.setTypeface(myTypeface);
                    canc.setTextColor(ContextCompat.getColor(context, R.color.OldPrimary));
                });
                alert.show();
            }
        }
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
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }
        @Override
        public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position == 0) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            }
            else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= 1) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
    /**
     * Converting dp to pixel
     */
    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, r.getDisplayMetrics()));
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("Settings", 0);
        boolean newLang = sp.getBoolean("WasLangChanged", false);
        if (newLang)
            recreate();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("WasLangChanged", false);
        editor.apply();
        if (Detect != null) {
            int icon = sp.getBoolean("Mapp", false) ? R.drawable.ic_detect_on : R.drawable.ic_detect_off;
            Detect.setIcon(icon);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.one:
                detectionBtn(item);
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
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void setNotifChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelGroup group3 = new NotificationChannelGroup(Grp3, "התראות מקרים");
            NotificationChannelGroup group2 = new NotificationChannelGroup(Grp2, "התראות שונות");
            NotificationChannel serviceChannel = new NotificationChannel(CnlBg, "פעילות ברקע", NotificationManager.IMPORTANCE_MIN);
            serviceChannel.enableVibration(false);
            serviceChannel.enableLights(false);
            serviceChannel.setShowBadge(false);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_MIN);
            serviceChannel.setDescription("חיוני כדי לאפשר זיהוי מקרים");
            serviceChannel.setGroup(Grp2);
            NotificationChannel serviceChannel2 = new NotificationChannel("Push", "הודעות פוש", NotificationManager.IMPORTANCE_HIGH);
            serviceChannel2.enableVibration(true);
            serviceChannel2.setBypassDnd(true);
            serviceChannel2.setDescription("התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'");
            serviceChannel2.enableLights(true);
            serviceChannel2.setShowBadge(true);
            serviceChannel2.setLightColor(Color.RED);
            serviceChannel2.setGroup(Grp2);

            NotificationChannel serviceChannel4 = new NotificationChannel(CnlWin, "זיהוי הודעות", NotificationManager.IMPORTANCE_LOW);
            serviceChannel4.enableVibration(false);
            serviceChannel4.setBypassDnd(false);
            serviceChannel4.setDescription("התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון");
            serviceChannel4.enableLights(false);
            serviceChannel4.setShowBadge(true);
            serviceChannel4.setLightColor(Color.RED);
            serviceChannel4.setGroup(Grp3);

            manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannelGroup(group3);
                manager.createNotificationChannelGroup(group2);
                manager.createNotificationChannel(serviceChannel);
                manager.createNotificationChannel(serviceChannel2);
                manager.createNotificationChannel(serviceChannel4);
            }
        }
    }
    private void detectionBtn(MenuItem item) {
        SharedPreferences sp = getSharedPreferences("Settings", 0);
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
                                    .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                        startActivity(intent);
                                    })
                                    .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                            final AlertDialog alert = builder.create();
                            alert.setOnShowListener(dialogInterface -> {
                                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);

                                canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
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
                                    .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                        Intent intent1 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                        startActivityForResult(intent1, REQUEST_PERMISSION_SETTING);
                                    })
                                    .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                            final AlertDialog alert = builder.create();
                            alert.setOnShowListener(dialogInterface -> {
                                Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                                canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
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
                                .setPositiveButton(getResources().getString(R.string.dialog_yes), (dialog, id) -> {
                                    Intent intent12 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivityForResult(intent12, REQUEST_PERMISSION_SETTING);
                                })
                                .setNegativeButton(getResources().getString(R.string.dialog_no), (dialog, id) -> dialog.cancel());
                        final AlertDialog alert = builder.create();
                        alert.setOnShowListener(dialogInterface -> {
                            Button canc = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            canc.setTextColor(getApplicationContext().getResources().getColor(R.color.ColorPrimary));
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
    }
}