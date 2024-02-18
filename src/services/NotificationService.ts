import { NativeModules, Platform } from 'react-native';
import type { Page, Tools } from './NavigationStore';
import { injectable } from 'inversify';
import { ANDROID_VERSION } from '../utils/constants';

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

export const enum NotificationLightColor {
    BLACK = -16777216,
    BLUE = -16776961,
    CYAN = -16711681,
    DKGRAY = -12303292,
    GRAY = -7829368,
    GREEN = -16711936,
    LTGRAY = -3355444,
    MAGENTA = -65281,
    RED = -65536,
    TRANSPARENT = 0,
    WHITE = -1,
    YELLOW = -256,
}

interface NotificationChannelConfig extends NotificationChannelGroupConfig {
    /** Notification channel importance. */
    importance?: Importance;
    /** Sets whether notification posted to this channel should vibrate. False by default. */
    enableVibration?: boolean;
    /** Sets whether notification posted to this channel should use lights. False by default. */
    enableLights?: boolean;
    /** Sets what color lights notification posted to this channel should use. RED by default. */
    lightsColor?: NotificationLightColor;
    /** Sets whether notification posted to this channel should show a badge. False by default. */
    showBadge?: boolean;
    /** Sets the group to which this channel will be related. */
    group?: string;
}

export const NOTIFICATION_MAP = {
    GROUPS: {
        CALLS: 'grpTog',
        OTHERS: 'grpOthers',
    },
    CHANNELS: {
        PUSH: 'Push',
        CALLS: 'notWithWin',
        BACKGROUND: 'runInBg',
    },
} as const;

@injectable()
export class NotificationService {
    constructor() {
        this.setupChannels();
    }

    public async createNotificationChannelGroup(channelGroupConfig: NotificationChannelGroupConfig) {
        return await NotificationsModule.createNotificationChannelGroup(channelGroupConfig);
    }

    public async createNotificationChannel(channelConfig: NotificationChannelConfig) {
        return await NotificationsModule.createNotificationChannel(channelConfig);
    }

    public async publishNotification(notificationConfig: NotificationConfig) {
        return await NotificationsModule.publishNotification(notificationConfig);
    }

    private async setupChannels() {
        if (Platform.OS !== 'android' || Platform.Version < ANDROID_VERSION.OREO) {
            return;
        }
        await Promise.all([
            this.createNotificationChannelGroup({ id: NOTIFICATION_MAP.GROUPS.CALLS, name: 'התראות מקרים', description: 'התראות עבור מקרים שזוהו' }),
            this.createNotificationChannelGroup({ id: NOTIFICATION_MAP.GROUPS.OTHERS, name: 'התראות שונות', description: 'התראות מהאפליקציה שאינן קשורות לאירועי רפואה' }),
        ]);
        await Promise.all([
            this.createNotificationChannel({
                name: 'פעילות ברקע',
                importance: Importance.MIN,
                group: NOTIFICATION_MAP.GROUPS.OTHERS,
                id: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
                description: 'חיוני כדי לאפשר זיהוי מקרים',
            }),
            this.createNotificationChannel({
                showBadge: true,
                name: 'הודעות פוש',
                enableLights: true,
                enableVibration: true,
                importance: Importance.HIGH,
                group: NOTIFICATION_MAP.GROUPS.OTHERS,
                lightsColor: NotificationLightColor.RED,
                id: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
                description: "התראות להודעות פוש שישלחו כגון בקשות קוד וכדו'",
            }),
            this.createNotificationChannel({
                showBadge: true,
                name: 'זיהוי הודעות',
                importance: Importance.LOW,
                group: NOTIFICATION_MAP.GROUPS.CALLS,
                id: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
                description: 'התראות עבור הודעות כאשר מוגדר להקפיץ גם חלון',
            }),
        ]);
    }
}
