package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_STOP;
import static atlow.chemi.mymada.Constants.CALL_DETECTION_STATE;
import static atlow.chemi.mymada.Constants.ERROR_ACTIVITY_RESULT_BUSY;
import static atlow.chemi.mymada.Constants.ERROR_FAILED_TO_SHOW_PICKER;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_PICKER_CANCELLED;
import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.MADA_APP_PACKAGE;
import static atlow.chemi.mymada.Constants.NOTIFICATION_LISTENER_NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.TEST_CALL_ACTION;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import atlow.chemi.mymada.BuildConfig;
import atlow.chemi.mymada.NotificationService;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.helpers.NotificationHelper;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;
import atlow.chemi.mymada.recieversAndServices.NotificationListenerForegroundService;

@ReactModule(name = MadaNotificationsModule.NAME)
public class MadaNotificationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private final ReactApplicationContext reactContext;
    private Promise resultPromise;
    private final int REQUEST_CODE_CAN_DRAW_OVERLAYS = 8396;
    private final int REQUEST_CODE_RINGTONE_PICKER = 8397;
    public static final String NAME = "MadaNotificationsModule";
    private final class ButtonPressReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(stopForegroundService()) {
                reactContext.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit().putString("Mapp", "false").apply();
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(CALL_DETECTION_STATE, null);
            }
        }
    }
    private final ButtonPressReceiver buttonPressReceiver = new ButtonPressReceiver();

    public MadaNotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void hasMadaApp(Promise promise) {
        final var packageManager = reactContext.getPackageManager();
        var intent = packageManager.getLaunchIntentForPackage(MADA_APP_PACKAGE);
        if (intent == null) {
            promise.resolve(false);
            return;
        }
        var list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        promise.resolve(!list.isEmpty());
    }

    @ReactMethod
    public void triggerTestCall(ReadableMap config, Promise promise) {
        if (config == null) {
            promise.reject(ERROR_INVALID_CONFIG, "MadaNotificationsModule: Config is required");
            return;
        }
        var launchIntent = buildMadaAppLaunchIntent();

        PendingIntent testPI = null;
        if (launchIntent != null) {
            testPI = PendingIntent.getActivity(reactContext, 99, launchIntent, PendingIntent.FLAG_IMMUTABLE);
        }
        var intent = new Intent(TEST_CALL_ACTION);
        intent.setAction(TEST_CALL_ACTION);
        intent.putExtra("callT", config.getInt("callType"));
        intent.putExtra("text", config.getString("callText"));
        GrobootRec.madasApp(testPI);
        var manager = LocalBroadcastManager.getInstance(reactContext);
        var wasScheduled = manager.sendBroadcast(intent);
        if (!wasScheduled) {
            promise.reject(ERROR_SERVICE_ERROR, "MadaNotificationsModule: broadcast scheduling failed");
            return;
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void hasOverlayPermission(Promise promise) {
        try {
            promise.resolve(canDrawOverlays().equals("authorized"));
        } catch (Error e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void requestOverlayPermission(Promise promise) {
        try {
            if (canDrawOverlays().equals("authorized")) {
                promise.resolve("authorized");
            } else {
                if (resultPromise != null) {
                    promise.reject(ERROR_ACTIVITY_RESULT_BUSY, "MadaNotificationsModule: can't request overlay permissions when result promise is set.");
                    return;
                }
                resultPromise = promise;
                var intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + reactContext.getPackageName()));
                reactContext.startActivityForResult(intent, REQUEST_CODE_CAN_DRAW_OVERLAYS, null);
            }
        } catch (Error e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getDefaultRingtone(Promise promise) {
        promise.resolve(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL).toString());
    }

    @ReactMethod
    public void getDefaultAppRingtone(Promise promise) {
        var ringUri = Uri.parse("android.resource://" + reactContext.getPackageName() + "/" + R.raw.alert);
        promise.resolve(ringUri.toString());
    }

    @ReactMethod
    public void selectRingtone(String title, String uri, Promise promise) {
        if (resultPromise != null) {
            promise.reject(ERROR_ACTIVITY_RESULT_BUSY, "MadaNotificationsModule: can't select ringtone when result promise is set.");
            return;
        }
        try {
            if (uri == null || uri.isEmpty()) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALL).toString();
            }
            resultPromise = promise;
            var picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, title);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.parse(uri));
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri));
            reactContext.startActivityForResult(picker, REQUEST_CODE_RINGTONE_PICKER, null);
        } catch (Exception e) {
            promise.reject(ERROR_FAILED_TO_SHOW_PICKER, e);
        }
    }

    @ReactMethod
    public void getNotificationListenerPermissionStatus(Promise promise) {
        if (reactContext == null) {
            promise.resolve("unknown");
            return;
        }
        var packageName = reactContext.getPackageName();
        var enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(this.reactContext);
        promise.resolve(enabledPackages.contains(packageName) ? "authorized" : "denied");
    }

    @ReactMethod
    public void startCallRecognitionForegroundService(ReadableMap notificationConfig, Promise promise) {
        try {
            NotificationHelper.from(reactContext).startForegroundService(new Intent(reactContext, NotificationListenerForegroundService.class), notificationConfig, buttonPressReceiver, NOTIFICATION_LISTENER_NOTIFICATION_BUTTON_PRESSED);
            promise.resolve(null);
        } catch (Exception e) {
            promise.reject(ERROR_SERVICE_ERROR, e);
        }
    }

    @ReactMethod
    public void stopCallRecognitionForegroundService(Promise promise) {
        promise.resolve(stopForegroundService());
    }

    @ReactMethod
    public void restartAndRebindNotificationListener(Promise promise) {
        var pm = reactContext.getPackageManager();
        var componentName = new ComponentName(reactContext, NotificationService.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(componentName);
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void addListener(String eventName) {}
    @ReactMethod
    public void removeListeners(Integer count) {}

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent intent) {
        if (resultPromise == null || (requestCode != REQUEST_CODE_CAN_DRAW_OVERLAYS && requestCode != REQUEST_CODE_RINGTONE_PICKER)) {
            return;
        }
        try {
            if (requestCode == REQUEST_CODE_CAN_DRAW_OVERLAYS) {
                resultPromise.resolve(canDrawOverlays());
                return;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                resultPromise.reject(ERROR_PICKER_CANCELLED, "MadaNotificationsModule: Ringtone picker canceled");
                return;
            }
            if (resultCode != Activity.RESULT_OK) {
                resultPromise.reject(ERROR_PICKER_CANCELLED, "MadaNotificationsModule: Ringtone picker failed with result code: " + resultCode);
                return;
            }
            if (intent == null) {
                resultPromise.reject(ERROR_PICKER_CANCELLED, "MadaNotificationsModule: intent not set");
                return;
            }
            var ringUri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            resultPromise.resolve(ringUri.toString());
        } finally {
            resultPromise = null;
        }
    }

    @Override public void onNewIntent(Intent intent) {}

    private String canDrawOverlays() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Settings.canDrawOverlays(reactContext)) {
            return "authorized";
        }
        //USING APP OPS MANAGER
        var manager = (AppOpsManager)reactContext.getSystemService(Context.APP_OPS_SERVICE);
        if (manager != null) {
            try {
                var result = manager.checkOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, Binder.getCallingUid(), reactContext.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED  ? "authorized" : "denied";
            } catch (Exception ignore) {}
        }
        // Last chance - if this fails, we definitely can't do it,
        try {
            var mgr = (WindowManager)reactContext.getSystemService(Context.WINDOW_SERVICE);
            if (mgr == null) {
                return "denied";
            }
            var viewToAdd = new View(reactContext);
            var params = new WindowManager.LayoutParams(
                    0,
                    0 ,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSPARENT
            );
            viewToAdd.setLayoutParams(params);
            mgr.addView(viewToAdd, params);
            mgr.removeView(viewToAdd);
            return "authorized";
        } catch (Exception ignore) {
        }
        return "denied";
    }

    private boolean stopForegroundService() {
        var intent = new Intent(reactContext, NotificationListenerForegroundService.class);
        intent.setAction(ACTION_FOREGROUND_SERVICE_STOP);
        return reactContext.stopService(intent);
    }

    private Intent buildMadaAppLaunchIntent() {
        Intent launchIntent = null;
        try{
            launchIntent = reactContext.getPackageManager().getLeanbackLaunchIntentForPackage(MADA_APP_PACKAGE);
        } catch (NoSuchMethodError ignored){}
        if (launchIntent == null) launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage(MADA_APP_PACKAGE);
        if (launchIntent != null) launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return launchIntent;
    }

    @Override
    public Map<String, Object> getConstants() {
        final var constants = new HashMap<String, Object>();

        var fields = BuildConfig.class.getDeclaredFields();
        for (Field f : fields) {
            try {
                constants.put(f.getName(), f.get(null));
            } catch (IllegalAccessException e) {
                Log.d(NAME, "Could not access BuildConfig field " + f.getName());
            }
        }

        constants.put("uid", reactContext.getApplicationInfo().uid);

        return constants;
    }
}
