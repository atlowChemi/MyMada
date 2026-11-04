package atlow.chemi.mymada.calls

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.os.SystemClock
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.util.Log
import atlow.chemi.mymada.permissions.NotificationPermission
import org.json.JSONArray
import java.security.MessageDigest
import java.util.UUID

/** Main-thread listener/receiver bookkeeping, private to this installed flavor/user. */
object CallRecovery {
    private const val REVISION = "recoveryRevision"
    private const val EPOCH = "recoveryEpoch"
    private const val POSTED = "recoveryPostedAt"
    private const val SOURCE = "recoverySourceAt"
    private const val RECOVERED = "recoveredCall"
    private val pending = mutableSetOf<String>()
    private var connectedComponent: ComponentName? = null
    private fun preferences(context: Context) = context.getSharedPreferences("CallRecovery", 0)
    private fun boot(context: Context) = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
    private fun grants(context: Context) = ListenerAccess.granted(context).joinToString(":") { it.flattenToString() }
    private fun enabled(context: Context) = context.getSharedPreferences("Settings", 0).getBoolean("Mapp", false)
    private fun now(context: Context): Long = preferences(context).let {
        RecoveryWindow.recoveryNow(System.currentTimeMillis(), it.getLong("wall", 0),
            it.getLong("elapsed", -1), SystemClock.elapsedRealtime())
    }
    private fun eligible(context: Context, revision: String, postedAt: Long, sourceAt: Long): Boolean {
        val wall = System.currentTimeMillis()
        return RecoveryWindow.fresh(postedAt, wall) && RecoveryWindow.fresh(sourceAt, wall) &&
            window(context)?.eligible(revision, postedAt, sourceAt, now(context)) == true
    }

    private fun baseline(context: Context) {
        pending.clear()
        val wall = System.currentTimeMillis()
        if (!preferences(context).edit().clear().putString("epoch", UUID.randomUUID().toString())
                .putInt("boot", boot(context)).putString("grants", grants(context)).putLong("floor", wall)
                .putLong("wall", wall).putLong("elapsed", SystemClock.elapsedRealtime()).commit()) {
            Log.e("CallRecovery", "Cannot persist recovery baseline")
        }
    }

    fun enable(context: Context) {
        // Enabling an already bound listener may not cause another onListenerConnected callback.
        if (connectedComponent != null && connectedComponent == ListenerAccess.selected(context)) baseline(context)
        else stop(context)
    }

    fun disconnected(component: ComponentName) {
        if (connectedComponent == component) connectedComponent = null
    }

    private fun window(context: Context): RecoveryWindow? = try {
        val prefs = preferences(context)
        val records = prefs.getStringSet("handled", emptySet()).orEmpty().associate { record ->
            val fields = record.split(':', limit = 2)
            require(fields.size == 2 && fields[1].matches(Regex("[0-9a-f]{64}")))
            fields[1] to fields[0].toLong()
        }.toMutableMap()
        require(records.size <= RecoveryWindow.MAX_REVISIONS)
        RecoveryWindow(prefs.getLong("floor", Long.MAX_VALUE), records)
    } catch (error: RuntimeException) {
        Log.e("CallRecovery", "Invalid recovery state; baseline required", error)
        null
    }

    /** First grant/upgrade, reboot and changed grant identities baseline, never replay history. */
    fun connected(context: Context): Boolean {
        connectedComponent = ListenerAccess.selected(context)
        if (!enabled(context)) return false
        val prefs = preferences(context)
        val boot = boot(context)
        val grants = grants(context)
        if (boot < 0 || grants.isEmpty()) return false
        val continuing = prefs.getString("epoch", null) != null && prefs.getInt("boot", -1) == boot &&
            prefs.getString("grants", null) == grants && window(context) != null
        if (!continuing) {
            baseline(context)
            return false
        }
        return NotificationPermission.granted(context)
    }

    fun track(context: Context, posted: StatusBarNotification, title: CharSequence?, body: CharSequence?,
              event: Intent, recovered: Boolean): Boolean {
        val sourceAt = posted.notification.`when`
        // Key includes user/package/uid/id/tag. Post time + source time + content distinguish updates;
        // content alone is never an event identity. Only a digest is persisted, not call text.
        val data = JSONArray().put(posted.key).put(posted.postTime).put(sourceAt)
            .put(title?.toString()).put(body?.toString()).toString()
        val revision = MessageDigest.getInstance("SHA-256").digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
        if (recovered && (revision in pending || !eligible(context, revision, posted.postTime, sourceAt))) return false
        pending += revision
        event.putExtra(REVISION, revision).putExtra(EPOCH, preferences(context).getString("epoch", null))
            .putExtra(POSTED, posted.postTime).putExtra(SOURCE, sourceAt).putExtra(RECOVERED, recovered)
        return true
    }

    /** Recheck on actual receipt: queued recovery may have expired or detection may have stopped. */
    fun allow(context: Context, event: Intent): Boolean {
        if (!event.getBooleanExtra(RECOVERED, false)) return true
        val prefs = preferences(context)
        return enabled(context) && NotificationPermission.granted(context) &&
            event.getStringExtra(EPOCH) != null && event.getStringExtra(EPOCH) == prefs.getString("epoch", null) &&
            prefs.getInt("boot", -1) == boot(context) && prefs.getString("grants", null) == grants(context) &&
            eligible(context, event.getStringExtra(REVISION) ?: "", event.getLongExtra(POSTED, 0),
                event.getLongExtra(SOURCE, 0))
    }

    /** Ack only after the receiver processed the event, not when the asynchronous broadcast queued. */
    fun handled(context: Context, event: Intent, dispatched: Boolean = false) {
        val revision = event.getStringExtra(REVISION) ?: return
        pending.remove(revision)
        val prefs = preferences(context)
        if (event.getStringExtra(EPOCH) == null || event.getStringExtra(EPOCH) != prefs.getString("epoch", null)) return
        val window = window(context) ?: return
        window.remember(revision, event.getLongExtra(POSTED, 0), now(context))
        if (!prefs.edit().putLong("floor", window.excludedThrough)
                .putStringSet("handled", window.handled.map { "${it.value}:${it.key}" }.toSet()).commit()) {
            Log.e("CallRecovery", "Cannot persist handled revision")
        } else if (dispatched && event.getBooleanExtra(RECOVERED, false)) {
            Log.i("CallRecovery", "Dispatched recovered revision=$revision")
        }
    }

    fun stop(context: Context) {
        pending.clear()
        if (!preferences(context).edit().clear().commit()) Log.e("CallRecovery", "Cannot clear recovery epoch")
    }
}
