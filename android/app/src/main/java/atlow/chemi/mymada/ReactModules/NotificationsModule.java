package atlow.chemi.mymada.ReactModules;

import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_PERMISSION;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.bridge.Arguments;
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
            promise.reject(ERROR_INVALID_CONFIG, "NotificationsModule: Channel group config is invalid");
            return;
        }
        NotificationHelper.getInstance(reactContext).createNotificationChannelGroup(channelGroupConfig, promise);
    }

    @ReactMethod
    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationsModule: Channel config is invalid");
            return;
        }
        NotificationHelper.getInstance(reactContext).createNotificationChannel(channelConfig, promise);
    }

    @ReactMethod
    public void publishNotification(ReadableMap notificationConfig, Promise promise) {
        if (notificationConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationsModule: notification config is invalid");
            return;
        }
        var notification = NotificationHelper.getInstance(reactContext).buildNotification(reactContext, Arguments.toBundle(notificationConfig));
        if (notification == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationsModule: failed building notification");
            return;
        }
        if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            promise.reject(ERROR_INVALID_PERMISSION, "Lacking permission for posting a notification");
            return;
        }
        var notificationManager = NotificationManagerCompat.from(reactContext);
        notificationManager.notify(notificationConfig.getInt("id"), notification);
    }
}
