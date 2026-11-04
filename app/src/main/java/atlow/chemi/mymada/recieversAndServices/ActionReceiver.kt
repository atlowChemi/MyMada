package atlow.chemi.mymada.recieversAndServices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import atlow.chemi.mymada.calls.DetectionStatus

open class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action")
        if (action == "widget" && !MetroService.running) {
            try {
                MetroService.start(context)
            } catch (error: Exception) {
                Log.w("MetroWidget", "Control unavailable", error)
            }
        } else if (action == "runInBG") {
            DetectionStatus.stop(context)
        } else if (action == "widget" || action == "stopM") {
            MetroService.stop(context)
        }
    }
}
