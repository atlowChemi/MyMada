package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.MADA_APP_PACKAGE;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

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
}
