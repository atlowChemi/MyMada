package atlow.chemi.mymada.recieversAndServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.Map;

import atlow.chemi.mymada.helpers.HeadlessJsTaskWorker;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        var action = intent.getAction();
        var actions = new String[]{Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_RESTARTED, Intent.ACTION_MY_PACKAGE_REPLACED};
        if (action == null || Arrays.stream(actions).noneMatch(action::equalsIgnoreCase)) {
            return;
        }
        var sp = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if (!((Map<String, Object>) sp.getAll()).compute("Mapp", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true")) {
            return;
        }
        var work = OneTimeWorkRequest.from(HeadlessJsTaskWorker.class);
        WorkManager.getInstance(context).enqueue(work);
    }
}
