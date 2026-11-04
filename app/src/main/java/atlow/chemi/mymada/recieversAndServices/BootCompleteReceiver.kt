package atlow.chemi.mymada.recieversAndServices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import atlow.chemi.mymada.calls.ListenerAccess
import atlow.chemi.mymada.calls.DetectionStatus

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED && intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        if (!context.getSharedPreferences("Settings", 0).getBoolean("Mapp", false)) return
        ListenerAccess.rebind(context)
        DetectionStatus.refresh(context)
    }
}
