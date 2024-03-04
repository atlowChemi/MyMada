package atlow.chemi.mymada.recieversAndServices;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class HeadlessJsService extends HeadlessJsTaskService {
    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        var extras = intent.getExtras();
        if (extras != null) {
            return new HeadlessJsTaskConfig("HeadlessTask", Arguments.fromBundle(extras), 15000, true);
        }
        return null;
    }
}
