package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.MADA_APP_PACKAGE;
import static atlow.chemi.mymada.Constants.TEST_CALL_ACTION;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import atlow.chemi.mymada.recieversAndServices.GrobootRec;

@ReactModule(name = MadaNotificationsModule.NAME)
public class MadaNotificationsModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    public static final String NAME = "MadaNotificationsModule";

    public MadaNotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
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
        var launchIntent = buildMadaAppLaunceIntent();

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

    private Intent buildMadaAppLaunceIntent() {
        Intent launchIntent = null;
        try{
            launchIntent = reactContext.getPackageManager().getLeanbackLaunchIntentForPackage(MADA_APP_PACKAGE);
        } catch (NoSuchMethodError ignored){}
        if (launchIntent == null) launchIntent = reactContext.getPackageManager().getLaunchIntentForPackage(MADA_APP_PACKAGE);
        if (launchIntent != null) launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return launchIntent;
    }
}
