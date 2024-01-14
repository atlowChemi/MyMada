import { NativeModules } from 'react-native';

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
    /** Priority of this notification. */
    priority?: Priority;
    /** If this property exists, notification will be contain button with text as button value. */
    button?: string;
    /** If this property exists, notification will be colored in red. */
    color?: true;
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

interface AudioGenerator {
    /** Create notification channel for foreground service */
    createNotificationChannelGroup(channelGroupConfig: NotificationChannelGroupConfig): Promise<void>;
    /** Create notification channel for foreground service */
    createNotificationChannel(channelConfig: NotificationChannelConfig): Promise<void>;
}

export const NOTIFICATION_MAP = {
    GROUPS: {
        OTHERS: 'grpOthers',
    },
    CHANNELS: {
        BACKGROUND: 'runInBg',
    },
} as const;

export const notificationsService = NotificationsModule as AudioGenerator;
