package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.MADA_APP_PACKAGE;
import static atlow.chemi.mymada.Constants.TEST_CALL_ACTION;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import atlow.chemi.mymada.recieversAndServices.GrobootRec;

@ReactModule(name = MadaNotificationsModule.NAME)
public class MadaNotificationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private final ReactApplicationContext reactContext;
    private Promise permissionPromise;
    private final int REQUEST_CODE_CAN_DRAW_OVERLAYS = 8396;
    public static final String NAME = "MadaNotificationsModule";

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
        promise.resolve(list.size() > 0);
    }

    @ReactMethod
    public void getAppData(Promise promise) {
        var pkgName = reactContext.getPackageName();
        var uid = reactContext.getApplicationInfo().uid;

        var result = Arguments.createMap();
        result.putString("packageName", pkgName);
        result.putInt("uid", uid);
        promise.resolve(result);
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
            promise.resolve(canDrawOverlays());
        } catch (Error e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void requestOverlayPermission(Promise promise) {
        try {
            if (canDrawOverlays()) {
                promise.resolve(true);
            } else {
                permissionPromise = promise;
                var intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + reactContext.getPackageName()));
                reactContext.startActivityForResult(intent, REQUEST_CODE_CAN_DRAW_OVERLAYS, null);
            }
        } catch (Error e) {
            promise.reject(e);
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent intent) {
        if (permissionPromise == null || resultCode != REQUEST_CODE_CAN_DRAW_OVERLAYS) {
            return;
        }
        permissionPromise.resolve(canDrawOverlays());
        permissionPromise = null;
    }

    @Override public void onNewIntent(Intent intent) {}

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Settings.canDrawOverlays(reactContext)) {
            return true;
        }
        //USING APP OPS MANAGER
        var manager = (AppOpsManager)reactContext.getSystemService(Context.APP_OPS_SERVICE);
        if (manager != null) {
            try {
                var result = manager.checkOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, Binder.getCallingUid(), reactContext.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception ignore) {}
        }
        // Last chance - if this fails, we definitely can't do it,
        try {
            var mgr = (WindowManager)reactContext.getSystemService(Context.WINDOW_SERVICE);
            if (mgr == null) {
                return false;
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
            return true;
        } catch (Exception ignore) {
        }
        return false;
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
}
