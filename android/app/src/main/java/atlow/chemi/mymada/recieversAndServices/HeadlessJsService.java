package atlow.chemi.mymada.recieversAndServices;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import io.invertase.firebase.crashlytics.ReactNativeFirebaseCrashlyticsNativeHelper;

public class HeadlessJsService extends HeadlessJsTaskService {
    private Runnable onFinish;

    public final class LocalBinder extends Binder {
        public void startRunning(Intent intent, Runnable onFinish) {
            HeadlessJsService service = HeadlessJsService.this;

            var config = getTaskConfig(intent);
            service.startTask(config);
            service.onFinish = onFinish;
        }
    }
    private final Binder binder = new LocalBinder();

    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        var extras = intent.getExtras();
        ReactNativeFirebaseCrashlyticsNativeHelper.log("Parsing task config");
        if (extras != null) {
            WritableMap args;
            try {
                args = Arguments.fromBundle(extras);
            } catch (IllegalStateException | IllegalArgumentException e) {
                ReactNativeFirebaseCrashlyticsNativeHelper.log("Failed parsing args");
                ReactNativeFirebaseCrashlyticsNativeHelper.recordNativeException(e);
                return null;
            }
            ReactNativeFirebaseCrashlyticsNativeHelper.log("Parsed task successfully");
            return new HeadlessJsTaskConfig("HeadlessTask", args, 15000, true);
        }
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        super.onHeadlessJsTaskFinish(taskId);
        if (onFinish != null) {
            onFinish.run();
        }
    }
}
