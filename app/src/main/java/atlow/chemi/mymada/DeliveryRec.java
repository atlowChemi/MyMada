package atlow.chemi.mymada;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

public class DeliveryRec extends BroadcastReceiver {
    public DeliveryRec() {
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("Settings", 0);
        boolean deli = sp.getBoolean("deli", true);
        if (deli) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "הודעה נמסרה למוקד!", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "הודעה לא נמסרה...", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
        }
    }
}

