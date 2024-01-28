package atlow.chemi.mymada.recieversAndServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, SMSMonitorService.class);
        String action = intent.getStringExtra("action");
        if(action != null && action.equals("runInBG")) {
            context.stopService(serviceIntent);
            context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).apply();
        }
    }
}
