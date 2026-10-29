package atlow.chemi.mymada.recieversAndServices;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import androidx.core.app.PendingIntentCompat;

import atlow.chemi.mymada.R;

/**
 * Implementation of App Widget functionality.
 */
public class MetroWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create an Intent to launch MainActivity when clicked
        Intent btn = new Intent(context, ActionReceiver.class);
        btn.putExtra("action", "widget");
        PendingIntent pendingIntent = PendingIntentCompat.getBroadcast(context, 0, btn, 0, false);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.metro_widget);
        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.metro_widget_image, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
