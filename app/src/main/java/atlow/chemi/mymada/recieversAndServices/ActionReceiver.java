package atlow.chemi.mymada.recieversAndServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MetroService.class);
        String action = intent.getStringExtra("action");
        if(action != null) {
            if(action.equals("widget") && ! MetroService.running) {
                try {
                    MetroService.start(context);
                } catch(Exception e) {
                    android.util.Log.w("MetroWidget", "Control unavailable", e);
                }
            } else if(action.equals("runInBG")) {
                context.getSharedPreferences("Settings", 0).edit().putBoolean("Mapp", false).apply();
                context.stopService(new Intent(context, SMSMonitorService.class));
            } else if(action.equals("widget") || action.equals("stopM")) {
                MetroService.stop(context);
            }
        }
    }
}
