package atlow.chemi.mymada.helpers;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import static atlow.chemi.mymada.Constants.ERROR_ANDROID_VERSION;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.METRONOME_NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.NOTIFICATION_HELPER_SOURCE;

import androidx.core.app.NotificationCompat;

import atlow.chemi.mymada.MainReactActivity;

public class NotificationHelper {
    private static NotificationHelper instance = null;
    private final NotificationManager mNotificationManager;

    public static synchronized NotificationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHelper(context);
        }
        return instance;
    }

    private NotificationHelper(Context context) {
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void createNotificationChannelGroup(ReadableMap channelGroupConfig, Promise promise) {
        if (channelGroupConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel group config is invalid");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelGroupConfig.hasKey("id")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel id is required");
                return;
            }
            var channelGroupId = channelGroupConfig.getString("id");
            if (!channelGroupConfig.hasKey("name")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel name is required");
                return;
            }
            var channelGroupName = channelGroupConfig.getString("name");
            if (channelGroupId == null || channelGroupName == null) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel group id or name is not specified");
                return;
            }
            var channelGroupDescription = channelGroupConfig.getString("description");
            var grp = new NotificationChannelGroup(channelGroupId, channelGroupName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                grp.setDescription(channelGroupDescription);
            }
            mNotificationManager.createNotificationChannelGroup(grp);
            promise.resolve(null);
        } else {
            promise.reject(ERROR_ANDROID_VERSION, "ForegroundService: Notification channel can be created on Android O+");
        }
    }

    public void createNotificationChannel(ReadableMap channelConfig, Promise promise) {
        if (channelConfig == null) {
            promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel config is invalid");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelConfig.hasKey("id")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel id is required");
                return;
            }
            var channelId = channelConfig.getString("id");
            if (!channelConfig.hasKey("name")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel name is required");
                return;
            }
            var channelName = channelConfig.getString("name");
            var channelDescription = channelConfig.getString("description");
            var channelImportance = channelConfig.hasKey("importance") ?
                    channelConfig.getInt("importance") : NotificationManager.IMPORTANCE_LOW;
            var enableVibration = channelConfig.hasKey("enableVibration") && channelConfig.getBoolean("enableVibration");
            if (channelId == null || channelName == null) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel id or name is not specified");
                return;
            }
            var channel = new NotificationChannel(channelId, channelName, channelImportance);
            channel.setDescription(channelDescription);
            channel.enableVibration(enableVibration);
            var channelGroup = channelConfig.getString("group");
            channel.setGroup(channelGroup);
            mNotificationManager.createNotificationChannel(channel);
            promise.resolve(null);
        } else {
            promise.reject(ERROR_ANDROID_VERSION, "ForegroundService: Notification channel can be created on Android O+");
        }
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
        var pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT) : PendingIntent.FLAG_UPDATE_CURRENT);

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
            notificationBuilder.setSmallIcon(getResourceIdForResourceName(context, iconName));
        }
        var largeIconName = notificationConfig.getString("largeIcon");
        if (largeIconName != null) {
            notificationBuilder.setLargeIcon(getBitmapForResourceName(context, largeIconName));
        }

        var color = notificationConfig.getString("color", "");
        if (color.equals("red")) {
            notificationBuilder.setColor(Color.argb(50, 255, 57, 50));
        }

        if (notificationConfig.getBoolean("useDefaults", false)) {
            notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        } else {
            notificationBuilder.setSound(null).setVibrate(null);
        }

        if (notificationConfig.containsKey("button")) {
            var buttonIntent = new Intent();
            buttonIntent.setAction(METRONOME_NOTIFICATION_BUTTON_PRESSED);
            var pendingButtonIntent = PendingIntent.getBroadcast(context, 0, buttonIntent,
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT) : PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.addAction(0, notificationConfig.getString("button"), pendingButtonIntent);
        }

        return notificationBuilder.build();
    }

    private Class getMainActivityClass(Context context) {
        // temp
        return MainReactActivity.class;
//        var packageName = context.getPackageName();
//        var launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
//        if (launchIntent == null || launchIntent.getComponent() == null) {
//            return null;
//        }
//        try {
//            return Class.forName(launchIntent.getComponent().getClassName());
//        } catch (ClassNotFoundException e) {
//            return null;
//        }
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