package atlow.chemi.mymada.helpers;


import static androidx.core.content.ContextCompat.RECEIVER_EXPORTED;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import static atlow.chemi.mymada.Constants.ACTION_FOREGROUND_SERVICE_START;
import static atlow.chemi.mymada.Constants.ERROR_ANDROID_VERSION;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.ERROR_SERVICE_ERROR;
import static atlow.chemi.mymada.Constants.NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.NOTIFICATION_CONFIG;
import static atlow.chemi.mymada.Constants.NOTIFICATION_HELPER_SOURCE;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import atlow.chemi.mymada.MainReactActivity;
import atlow.chemi.mymada.R;
import atlow.chemi.mymada.classes.JSException;

public class NotificationHelper {
    private Context context;
    private static NotificationHelper instance = null;
    private final NotificationManager mNotificationManager;

    public static synchronized NotificationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
        return instance;
    }

    private NotificationHelper(Context context) {
        this.context = context;
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotificationChannelGroup(ReadableMap channelGroupConfig, Promise promise) {
        if (channelGroupConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel group config is invalid");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            promise.reject(ERROR_ANDROID_VERSION, "NotificationHelper: Notification channel group can be created on Android O+");
            return;
        }
        if (!channelGroupConfig.hasKey("id")) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel id is required");
            return;
        }
        var channelGroupId = channelGroupConfig.getString("id");
        if (!channelGroupConfig.hasKey("name")) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel name is required");
            return;
        }
        var channelGroupName = channelGroupConfig.getString("name");
        if (channelGroupId == null || channelGroupName == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel group id or name is not specified");
            return;
        }
        var channelGroupDescription = channelGroupConfig.getString("description");
        var grp = new NotificationChannelGroup(channelGroupId, channelGroupName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            grp.setDescription(channelGroupDescription);
        }
        mNotificationManager.createNotificationChannelGroup(grp);
        promise.resolve(null);
    }

    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel config is invalid");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            promise.reject(ERROR_ANDROID_VERSION, "NotificationHelper: Notification channel can be created on Android O+");
            return;
        }
        if (!channelConfig.hasKey("id")) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel id is required");
            return;
        }
        var channelId = channelConfig.getString("id");
        if (!channelConfig.hasKey("name")) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel name is required");
            return;
        }
        var channelName = channelConfig.getString("name");
        var channelDescription = channelConfig.getString("description");
        var channelImportance = channelConfig.hasKey("importance") ?
                channelConfig.getInt("importance") : NotificationManager.IMPORTANCE_LOW;
        var enableVibration = channelConfig.hasKey("enableVibration") && channelConfig.getBoolean("enableVibration");
        var enableLights = channelConfig.hasKey("enableLights") && channelConfig.getBoolean("enableLights");
        var showBadge = channelConfig.hasKey("showBadge") && channelConfig.getBoolean("showBadge");
        if (channelId == null || channelName == null) {
            promise.reject(ERROR_INVALID_CONFIG, "NotificationHelper: Channel id or name is not specified");
            return;
        }
        var channel = new NotificationChannel(channelId, channelName, channelImportance);
        channel.setDescription(channelDescription);
        channel.enableVibration(enableVibration);
        channel.enableLights(enableLights);
        if (enableLights) {
            var lightsColor = channelConfig.hasKey("lightsColor") ? channelConfig.getInt("lightsColor") : Color.RED;
            channel.setLightColor(lightsColor);
        }
        channel.setShowBadge(showBadge);
        var channelGroup = channelConfig.getString("group");
        channel.setGroup(channelGroup);
        mNotificationManager.createNotificationChannel(channel);
        promise.resolve(null);
    }

    public int getChannelImportance(String channelId) throws JSException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            throw new JSException(ERROR_ANDROID_VERSION, "NotificationHelper: Notification channel exists Android O+");
        }
        var channel = mNotificationManager.getNotificationChannel(channelId);
        return channel.getImportance();
    }

    public Notification buildNotification(Context context, Bundle notificationConfig) {
        if (notificationConfig == null) {
            return null;
        }
        var mainActivityClass = getMainActivityClass(context);
        if (mainActivityClass == null) {
            return null;
        }
        var notificationIntent = new Intent(context, mainActivityClass);
        notificationIntent.putExtra("source", NOTIFICATION_HELPER_SOURCE);
        notificationIntent.putExtras(notificationConfig);
        var pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        var channelId = notificationConfig.getString("channelId");
        if (channelId == null) {
            return null;
        }
        var notificationBuilder = new NotificationCompat.Builder(context, channelId);

        var priorityInt = notificationConfig.containsKey("priority") ? notificationConfig.getInt("priority"): Notification.PRIORITY_HIGH;
        var priority = getPriority(priorityInt);

        notificationBuilder.setContentTitle(notificationConfig.getString("title"))
                .setContentText(notificationConfig.getString("text"))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(notificationConfig.getBoolean("isAutoCancelable", false))
                .setOngoing(notificationConfig.getBoolean("isOnGoing", false));

        var ticker = notificationConfig.getString("ticker");
        if (ticker != null) {
            notificationBuilder.setTicker(ticker);
        }

        var bigText = notificationConfig.getString("bigText");
        if (bigText != null) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
        }

        var iconName = notificationConfig.getString("icon");
        if (iconName != null) {
            switch (iconName) {
                case "ic_noti" -> notificationBuilder.setSmallIcon(R.drawable.ic_noti);
                case "ic_siren" -> notificationBuilder.setSmallIcon(R.drawable.ic_siren);
                case "ic_metronome_tile" -> notificationBuilder.setSmallIcon(R.drawable.ic_metronome_tile);
                default -> notificationBuilder.setSmallIcon(getResourceIdForResourceName(context, iconName));
            }
        }
        var largeIconName = notificationConfig.getString("largeIcon");
        if (largeIconName != null) {
            switch (largeIconName) {
                case "ic_launcher" -> notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
                default -> notificationBuilder.setLargeIcon(getBitmapForResourceName(context, largeIconName));
            }
        }

        var color = notificationConfig.getString("color", "");
        if (color.equals("red")) {
            notificationBuilder.setColor(Color.argb(50, 255, 57, 50));
        }
        if (color.equals("blue")) {
            notificationBuilder.setColor(Color.argb(50, 76, 119, 247));
        }

        if (notificationConfig.getBoolean("useDefaults", false)) {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else {
            notificationBuilder.setSound(null).setVibrate(null);
        }

        if (notificationConfig.getBoolean("hideWhen", false)) {
            notificationBuilder.setShowWhen(false);
        }

        if (notificationConfig.containsKey("button")) {
            var buttonIntent = new Intent();
            buttonIntent.setAction(NOTIFICATION_BUTTON_PRESSED);
            var pendingButtonIntent = PendingIntent.getBroadcast(context, 0, buttonIntent,  PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.addAction(0, notificationConfig.getString("button"), pendingButtonIntent);
        }

        return notificationBuilder.build();
    }

    public void startForegroundService(Intent intent, ReadableMap notificationConfig, BroadcastReceiver buttonPressReceiver) throws JSException {
        if (context == null) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: context is missing");
        }
        if (notificationConfig == null) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: Notification config is invalid");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationConfig.hasKey("channelId")) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: channelId is required");
        }
        if (!notificationConfig.hasKey("id")) {
            throw new JSException(ERROR_INVALID_CONFIG , "ForegroundService: id is required");
        }
        if (!notificationConfig.hasKey("icon")) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: icon is required");
        }
        if (!notificationConfig.hasKey("title")) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: title is required");
        }
        if (!notificationConfig.hasKey("text")) {
            throw new JSException(ERROR_INVALID_CONFIG, "ForegroundService: text is required");
        }

        intent.setAction(ACTION_FOREGROUND_SERVICE_START);
        intent.putExtra(NOTIFICATION_CONFIG, Arguments.toBundle(notificationConfig));
        try {
            ContextCompat.startForegroundService(context, intent);
        } catch (IllegalStateException e) {
            throw new JSException(ERROR_SERVICE_ERROR, "ForegroundService: Foreground service is not started");
        }
        var filter = new IntentFilter();
        filter.addAction(NOTIFICATION_BUTTON_PRESSED);
        ContextCompat.registerReceiver(context, buttonPressReceiver, filter, RECEIVER_EXPORTED);
    }

    private Class getMainActivityClass(Context context) {
        var packageName = context.getPackageName();
        var launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null || launchIntent.getComponent() == null) {
            return null;
        }
        try {
            return Class.forName(launchIntent.getComponent().getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private int getPriority(int priorityInt) {
        switch (priorityInt) {
            case 0:
                return Notification.PRIORITY_DEFAULT;
            case -1:
                return Notification.PRIORITY_LOW;
            case -2:
                return Notification.PRIORITY_MIN;
            case 2:
                return Notification.PRIORITY_MAX;
            case 1:
            default:
                return Notification.PRIORITY_HIGH;
        }
    }

    private int getResourceIdForResourceName(Context context, String resourceName) {
        var resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId == 0) {
            resourceId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
        }
        return resourceId;
    }

    private Bitmap getBitmapForResourceName(Context context, String resourceName) {
        var resource = getResourceIdForResourceName(context, resourceName);
        return BitmapFactory.decodeResource(context.getResources(), resource);
    }
}