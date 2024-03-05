package atlow.chemi.mymada.recieversAndServices;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import atlow.chemi.mymada.R;
import atlow.chemi.mymada.helpers.HeadlessJsTaskWorker;

/**
 * Implementation of App Widget functionality.
 */
public class MetroWidget extends AppWidgetProvider {
    private static final String ON_CLICK_TAG = "ON_CLICK_TAG";
    private PendingIntent getPendingSelfIntent(Context context, int widgetId) {
        var intent = new Intent(context, getClass());
        intent.setAction(MetroWidget.ON_CLICK_TAG);
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_IMMUTABLE);
    }
    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        var views = new RemoteViews(context.getPackageName(), R.layout.metro_widget);
        views.setOnClickPendingIntent(R.id.metro_widget_image, getPendingSelfIntent(context, appWidgetId));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ON_CLICK_TAG.equals(intent.getAction())) {
            var data = new Data.Builder().putString("type", "metronomeToggle").build();
            var work = new OneTimeWorkRequest.Builder(HeadlessJsTaskWorker.class).setInputData(data).build();
            WorkManager.getInstance(context).enqueue(work);
        }
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (var appWidgetId : appWidgetIds) {
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