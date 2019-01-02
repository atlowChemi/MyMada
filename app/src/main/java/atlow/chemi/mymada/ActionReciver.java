package atlow.chemi.mymada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ActionReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MetroService.class);
        context.stopService(serviceIntent);
    }
}
