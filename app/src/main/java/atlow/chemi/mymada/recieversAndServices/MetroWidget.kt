package atlow.chemi.mymada.recieversAndServices

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.PendingIntentCompat
import atlow.chemi.mymada.R

class MetroWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Existing RN widget RemoteViews can retain this PendingIntent until their next update.
        if (intent.action == "ON_CLICK_TAG") {
            ActionReceiver().onReceive(context,
                Intent(context, ActionReceiver::class.java).putExtra("action", "widget"))
        }
    }
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val action = PendingIntentCompat.getBroadcast(context, 0,
            Intent(context, ActionReceiver::class.java).putExtra("action", "widget"),
            PendingIntent.FLAG_UPDATE_CURRENT, false)
        val views = RemoteViews(context.packageName, R.layout.metro_widget).apply {
            setOnClickPendingIntent(R.id.metro_widget_image, action)
            setContentDescription(R.id.metro_widget_image,
                context.getString(if (MetroService.running) R.string.MetroStop else R.string.startT))
        }
        ids.forEach { manager.updateAppWidget(it, views) }
    }
    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            MetroWidget().onUpdate(context, manager,
                manager.getAppWidgetIds(ComponentName(context, MetroWidget::class.java)))
        }
    }
}
