package atlow.chemi.mymada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("started BCR", intent.getAction());
        String act = intent.getAction();
        if (act != null && act.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) || act.equalsIgnoreCase(Intent.ACTION_PACKAGE_ADDED) ||
                act.equalsIgnoreCase(Intent.ACTION_PACKAGE_RESTARTED) || act.equalsIgnoreCase(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            SharedPreferences sp = context.getSharedPreferences("Settings", 0);
            boolean mapp = sp.getBoolean("Mapp", false);
            if (mapp) {
                Intent serviceIntent = new Intent(context, SMSMonitorService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
