package atlow.chemi.mymada.helpers;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import static atlow.chemi.mymada.Constants.ERROR_ANDROID_VERSION;
import static atlow.chemi.mymada.Constants.ERROR_INVALID_CONFIG;
import static atlow.chemi.mymada.Constants.METRONOME_NOTIFICATION_BUTTON_PRESSED;
import static atlow.chemi.mymada.Constants.NOTIFICATION_HELPER_SOURCE;

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
            String channelGroupId = channelGroupConfig.getString("id");
            if (!channelGroupConfig.hasKey("name")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel name is required");
                return;
            }
            String channelGroupName = channelGroupConfig.getString("name");
            if (channelGroupId == null || channelGroupName == null) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel group id or name is not specified");
                return;
            }
            String channelGroupDescription = channelGroupConfig.getString("description");
            NotificationChannelGroup grp = new NotificationChannelGroup(channelGroupId, channelGroupName);
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
            String channelId = channelConfig.getString("id");
            if (!channelConfig.hasKey("name")) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel name is required");
                return;
            }
            String channelName = channelConfig.getString("name");
            String channelDescription = channelConfig.getString("description");
            int channelImportance = channelConfig.hasKey("importance") ?
                    channelConfig.getInt("importance") : NotificationManager.IMPORTANCE_LOW;
            boolean enableVibration = channelConfig.hasKey("enableVibration") && channelConfig.getBoolean("enableVibration");
            if (channelId == null || channelName == null) {
                promise.reject(ERROR_INVALID_CONFIG, "ForegroundService: Channel id or name is not specified");
                return;
            }
            NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
            channel.setDescription(channelDescription);
            channel.enableVibration(enableVibration);
            String channelGroup = channelConfig.getString("group");
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
        Class mainActivityClass = getMainActivityClass(context);
        if (mainActivityClass == null) {
            return null;
        }
        Intent notificationIntent = new Intent(context, mainActivityClass);
        notificationIntent.putExtra("source", NOTIFICATION_HELPER_SOURCE);
        notificationIntent.putExtras(notificationConfig);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT) : PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = notificationConfig.getString("channelId");
            if (channelId == null) {
                return null;
            }
            notificationBuilder = new Notification.Builder(context, channelId);
        } else {
            notificationBuilder = new Notification.Builder(context);
        }

        int priorityInt = notificationConfig.containsKey("priority") ? notificationConfig.getInt("priority"): Notification.PRIORITY_HIGH;

        int priority = getPriority(priorityInt);

        notificationBuilder.setContentTitle(notificationConfig.getString("title"))
                .setContentText(notificationConfig.getString("text"))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setSound(null)
                .setVibrate(null)
                .setOngoing(true);

        String iconName = notificationConfig.getString("icon");
        if (iconName != null) {
            notificationBuilder.setSmallIcon(getResourceIdForResourceName(context, iconName));
        }

        if (notificationConfig.containsKey("color")) {
            notificationBuilder.setColor(Color.argb(50, 255, 57, 50));
        }

        if (notificationConfig.containsKey("button")) {
            Intent buttonIntent = new Intent();
            buttonIntent.setAction(METRONOME_NOTIFICATION_BUTTON_PRESSED);
            PendingIntent pendingButtonIntent = PendingIntent.getBroadcast(context, 0, buttonIntent,
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT) : PendingIntent.FLAG_UPDATE_CURRENT);

            notificationBuilder.addAction(0, notificationConfig.getString("button"), pendingButtonIntent);
        }

        return notificationBuilder.build();
    }

    private Class getMainActivityClass(Context context) {
        // temp
        return MainReactActivity.class;
//        String packageName = context.getPackageName();
//        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
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
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        if (resourceId == 0) {
            resourceId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
        }
        return resourceId;
    }
}