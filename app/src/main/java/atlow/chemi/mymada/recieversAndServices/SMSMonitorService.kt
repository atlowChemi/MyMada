package atlow.chemi.mymada.recieversAndServices

import android.app.Service
import android.content.Intent
import android.os.IBinder
import atlow.chemi.mymada.calls.DetectionStatus

/** Retained installed identity: old explicit starts retire immediately, never restart an idle FGS. */
class SMSMonitorService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopForeground(STOP_FOREGROUND_REMOVE)
        DetectionStatus.refresh(this)
        stopSelf(startId)
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
