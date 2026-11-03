package atlow.chemi.mymada.calls

import android.app.Notification
import android.os.Bundle
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import android.util.Log
import atlow.chemi.mymada.BuildConfig
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/** Debug metadata only. No notification values, raw identities, PendingIntents or exception messages. */
internal object CallDiagnostics {
    const val EXTRA = "callDiagnosticEvent"
    private val sequence = AtomicLong()
    private val lines = AtomicLong()
    private val salt by lazy { UUID.randomUUID().toString() }

    fun next(): Long = if (BuildConfig.DEBUG) sequence.incrementAndGet() else 0

    fun relevant(pack: String?, ownPackage: String): Boolean = pack == ownPackage ||
        pack == "atlow.mymadaadmin" || pack?.contains("mdaemergency") == true || pack?.contains("mda.health") == true

    fun hash(value: String?): String = if (value == null) "absent" else MessageDigest.getInstance("SHA-256")
        .digest("$salt\u0000$value".toByteArray()).take(8).joinToString("") { "%02x".format(it) }

    fun shape(present: Boolean, value: Any?): String =
        "present=$present,type=${when (value) {
            null -> "null"
            is String -> "String"
            is CharSequence -> "CharSequence"
            is Number -> "Number"
            is Boolean -> "Boolean"
            is Array<*> -> "Array"
            is Bundle -> "Bundle"
            else -> "other"
        }},len=${if (value is CharSequence) value.length else -1}"

    fun extraName(key: String): String = when (key) {
        Notification.EXTRA_TITLE, Notification.EXTRA_TEXT, Notification.EXTRA_BIG_TEXT,
        Notification.EXTRA_TITLE_BIG, Notification.EXTRA_SUB_TEXT, Notification.EXTRA_SUMMARY_TEXT,
        Notification.EXTRA_TEXT_LINES, Notification.EXTRA_TEMPLATE, Notification.EXTRA_SHOW_WHEN,
        Notification.EXTRA_SMALL_ICON, "android.appInfo", "android.originatingUserId",
        "android.reduced.images" -> key
        else -> "custom#${hash(key)}"
    }

    fun source(posted: StatusBarNotification): String = try {
        val notification = posted.notification
        val extras = notification.extras
        fun field(key: String) = shape(extras?.containsKey(key) == true, extras?.get(key))
        // Framework extra names are useful; custom names may themselves contain private values.
        val keys = extras?.keySet().orEmpty().sorted().take(32).joinToString(",", transform = ::extraName)
        "pkg=${posted.packageName} id=${posted.id} tag=${hash(posted.tag)} key=${hash(posted.key)} " +
            "group=${hash(notification.group)} override=${hash(posted.overrideGroupKey)} flags=${notification.flags} " +
            "summary=${notification.flags and Notification.FLAG_GROUP_SUMMARY != 0} " +
            "keys=[$keys] keyCount=${extras?.size() ?: 0} title={${field(Notification.EXTRA_TITLE)}} " +
            "text={${field(Notification.EXTRA_TEXT)}} bigText={${field(Notification.EXTRA_BIG_TEXT)}} " +
            "sourcePI=${notification.contentIntent != null} tickerLen=${notification.tickerText?.length ?: -1} " +
            "actions=${notification.actions?.size ?: 0} fullScreen=${notification.fullScreenIntent != null} " +
            "views=${notification.contentView != null}/${notification.bigContentView != null}/${notification.headsUpContentView != null}"
    } catch (_: RuntimeException) {
        "metadata=unavailable"
    }

    fun record(event: Long, stage: String, metadata: String) {
        if (!BuildConfig.DEBUG || event == 0L) return
        val count = lines.incrementAndGet()
        if (count > 1024) return
        val detail = if (count == 1024L) "limitReached=true" else metadata.take(3000)
        Log.d("MadaCallTrace", "t=${SystemClock.elapsedRealtime()} event=$event stage=$stage $detail")
    }
}
