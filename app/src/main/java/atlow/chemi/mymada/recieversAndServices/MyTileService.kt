package atlow.chemi.mymada.recieversAndServices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.ContextCompat

/** Native installed component; RN's installed component below uses the same state. */
open class MyTileService : TileService() {
    private var listening = false
    private val updates = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refresh()
    }
    override fun onStartListening() {
        super.onStartListening()
        if (!listening) {
            ContextCompat.registerReceiver(this, updates, IntentFilter(MetroService.STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED)
            listening = true
        }
        refresh()
    }
    override fun onStopListening() {
        if (listening) unregisterReceiver(updates)
        listening = false
        super.onStopListening()
    }
    override fun onDestroy() {
        if (listening) unregisterReceiver(updates)
        listening = false
        super.onDestroy()
    }
    override fun onTileAdded() = refresh()
    override fun onClick() {
        super.onClick()
        try {
            if (MetroService.running) MetroService.stop(this) else MetroService.start(this)
        } catch (error: RuntimeException) { Log.w("MetroTile", "Control unavailable", error) }
        refresh()
    }
    private fun refresh() {
        qsTile?.apply {
            state = if (MetroService.running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            updateTile()
        }
    }
}

class MetronomeTileService : MyTileService()
