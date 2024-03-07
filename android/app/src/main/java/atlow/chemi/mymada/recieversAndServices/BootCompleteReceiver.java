package atlow.chemi.mymada.recieversAndServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;

import atlow.chemi.mymada.ReactModules.SharedPreferencesModule;
import atlow.chemi.mymada.helpers.HeadlessJsTaskWorker;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        var action = intent.getAction();
        var actions = new String[]{Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_RESTARTED, Intent.ACTION_MY_PACKAGE_REPLACED};
        if (action == null || Arrays.stream(actions).noneMatch(action::equalsIgnoreCase)) {
            return;
        }
        var isCallRecognitionOn = SharedPreferencesModule.isCallRecognitionOn(context);
        if (!isCallRecognitionOn) {
            return;
        }
        var work = OneTimeWorkRequest.from(HeadlessJsTaskWorker.class);
        WorkManager.getInstance(context).enqueue(work);
    }
}
