package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import atlow.chemi.mymada.helpers.NotificationHelper;

@ReactModule(name = NotificationsModule.NAME)
public class NotificationsModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;
    public static final String NAME = "NotificationsModule";

    public NotificationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void createNotificationChannelGroup(ReadableMap channelGroupConfig, Promise promise) {
        if (channelGroupConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel group config is invalid");
            return;
        }
        NotificationHelper.getInstance(reactContext).createNotificationChannelGroup(channelGroupConfig, promise);
    }

    @ReactMethod
    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel config is invalid");
            return;
        }
        NotificationHelper.getInstance(reactContext).createNotificationChannel(channelConfig, promise);
    }
}
