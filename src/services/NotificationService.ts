import { NativeModules } from 'react-native';
import type { Page, Tools } from './NavigationStore';

const { NotificationsModule } = NativeModules;

export const enum Priority {
    /** PRIORITY_MIN */
    PRIORITY_MIN = -2,
    /** PRIORITY_LOW */
    PRIORITY_LOW = -1,
    /** PRIORITY_DEFAULT (by default) */
    PRIORITY_DEFAULT = 0,
    /** PRIORITY_HIGH */
    PRIORITY_HIGH = 1,
    /** PRIORITY_MAX */
    PRIORITY_MAX = 2,
}
export interface NotificationConfig {
    /** Notification channel id to display notification */
    channelId: string;
    /** Unique notification id */
    id: number;
    /** Notification title */
    title: string;
    /** Notification text */
    text: string;
    /** Small icon name */
    icon: string;
    /** Large icon name */
    largeIcon?: string;
    /** Priority of this notification. */
    priority?: Priority;
    /** If this property exists, notification will contain button with given string as button value. */
    button?: string;
    /** Set the notifications color. */
    color?: 'red';
    /** If this property exists, default sound and vibration will be used on Android M and below. */
    useDefaults?: boolean;
    /** If this property exists, notification will be canceled when user taps on it. */
    isAutoCancelable?: boolean;
    /** If this property exists, notification will be persistent and won't be removable. */
    isOnGoing?: boolean;
    /** If this property exists, notification will contain a ticker. */
    ticker?: string;
    /** If this property exists, notification will show the given text when unfolded. */
    bigText?: string;
    /** If this property exists, clicking the notification will navigate to the given page. */
    target?: Tools | Page;
    /** Marks wether ther @see {target} is a tools page */
    navigateToTools?: boolean;
    /** additional data */
    [key: string]: any;
}

interface NotificationChannelGroupConfig {
    /** Unique channel ID. */
    id: string;
    /** Notification channel name. */
    name: string;
    /** Notification channel description. */
    description?: string;
}

export const enum Importance {
    MIN = 1,
    LOW = 2,
    DEFAULT = 3,
    HIGH = 4,
    MAX = 5,
}
interface NotificationChannelConfig extends NotificationChannelGroupConfig {
    /** Notification channel importance. */
    importance?: Importance;
    /** Sets whether notification posted to this channel should vibrate. False by default. */
    enableVibration?: boolean;
    /** Sets the group to which this channel will be related. */
    group?: string;
}

interface NotificationsModuleStatic {
    /** Create notification channel for foreground service */
    createNotificationChannelGroup(channelGroupConfig: NotificationChannelGroupConfig): Promise<void>;
    /** Create notification channel for foreground service */
    createNotificationChannel(channelConfig: NotificationChannelConfig): Promise<void>;
    /** Publish a notification */
    publishNotification(notificationConfig: NotificationConfig): Promise<void>;
}

export const NOTIFICATION_MAP = {
    GROUPS: {
        OTHERS: 'grpOthers',
    },
    CHANNELS: {
        BACKGROUND: 'runInBg',
    },
} as const;

export const notificationsService = NotificationsModule as NotificationsModuleStatic;
