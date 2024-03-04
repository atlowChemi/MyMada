package atlow.chemi.mymada.recieversAndServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.Map;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        var action = intent.getAction();
        var actions = new String[]{Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_PACKAGE_ADDED, Intent.ACTION_PACKAGE_RESTARTED, Intent.ACTION_MY_PACKAGE_REPLACED};
        if (action != null && Arrays.stream(actions).anyMatch(action::equalsIgnoreCase)) {
            Log.d("started BCR", action);
            var sp = context.getSharedPreferences("Settings", 0);
            if (!((Map<String, Object>) sp.getAll()).compute("Mapp", (key, val) -> String.valueOf(val == null ? "false" : val)).equals("true")) {
                return;
            }
            var serviceIntent = new Intent(context, SMSMonitorService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
