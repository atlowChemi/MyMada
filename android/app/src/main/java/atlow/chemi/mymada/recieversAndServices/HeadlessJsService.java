package atlow.chemi.mymada.recieversAndServices;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

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
        if (extras != null) {
            return new HeadlessJsTaskConfig("HeadlessTask", Arguments.fromBundle(extras), 15000, true);
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
