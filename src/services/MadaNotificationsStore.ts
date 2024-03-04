import { inject, injectable } from 'inversify';
import { Linking, NativeModules, NativeEventEmitter } from 'react-native';
import i18n from '../localization';
import { colors } from '../consts';
import { DialogStore } from './DialogStore';
import { BaseDialog } from '../containers/dialogs/BaseDialog';
import { PREF_KEYS, SharedPreferencesStore } from './SharedPreferencesStore';
import { Priority, type NotificationConfig, NOTIFICATION_MAP } from './NotificationService';

const { MadaNotificationsModule } = NativeModules;
type PermissionStatus = 'unknown' | 'authorized' | 'denied';

export const enum CallType {
    OnCall = 1,
    Group = 2,
    Shift = 3,
    Administrative = 4,
}

@injectable()
export class MadaNotificationsStore {
    private readonly eventEmitter = new NativeEventEmitter(MadaNotificationsModule);
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;

    constructor() {
        this.eventEmitter.addListener('CALL_DETECTION_STATE', () => this.sharedPreferencesStore.loadChannel('Settings', true));
    }

    public hasMadaApp(): Promise<boolean> {
        return MadaNotificationsModule.hasMadaApp();
    }

    public getAppData(): Promise<{ packageName: string; uid: number }> {
        return MadaNotificationsModule.getAppData();
    }

    public triggerTestCall(config: { callType: CallType; callText: string }): Promise<void> {
        return MadaNotificationsModule.triggerTestCall(config);
    }

    public hasOverlayPermission(): Promise<boolean> {
        return MadaNotificationsModule.hasOverlayPermission();
    }

    public requestOverlayPermission(): Promise<PermissionStatus> {
        return MadaNotificationsModule.requestOverlayPermission();
    }

    public getDefaultRingtone(): Promise<string> {
        return MadaNotificationsModule.getDefaultRingtone();
    }

    public getDefaultAppRingtone(): Promise<string> {
        return MadaNotificationsModule.getDefaultAppRingtone();
    }

    public selectRingtone(title: string, picked: string): Promise<string> {
        return MadaNotificationsModule.selectRingtone(title, picked);
    }

    public restartAndRebindNotificationListener(): Promise<void> {
        return MadaNotificationsModule.restartAndRebindNotificationListener();
    }

    public startCallRecognitionForegroundService(): Promise<void> {
        return MadaNotificationsModule.startCallRecognitionForegroundService(this.notificationConfig);
    }

    public toggleDetection = async (): Promise<void> => {
        const {
            callDetection: { isCallRecognitionOn },
        } = await this.sharedPreferencesStore.applicationSettings;
        if (isCallRecognitionOn) {
            return this.stopCallDetection();
        }
        return this.startCallDetection();
    };

    private async startCallDetection() {
        const status = await this.getNotificationListenerPermissionStatus();
        if (status !== 'authorized') {
            const shouldOpenSettings = await this.dialogStore.openDialog(BaseDialog, {
                blockBackBtn: true,
                blockBackdropPress: true,
                negativeBtnColor: colors.oldPrimary,
                cancelText: i18n.t('permissions:deny'),
                title: i18n.t('permissions:alertTitle'),
                okText: i18n.t('permissions:allowNotificationListener'),
                description: i18n.t('permissions:notificationListenerDescription'),
            });
            if (shouldOpenSettings) {
                await Linking.sendIntent('android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS');
            }
            return;
        }
        if (!(await this.hasOverlayPermission())) {
            const shouldRequestPermission = await this.dialogStore.openDialog(BaseDialog, {
                blockBackBtn: true,
                blockBackdropPress: true,
                negativeBtnColor: colors.oldPrimary,
                okText: i18n.t('permissions:approve'),
                cancelText: i18n.t('permissions:deny'),
                title: i18n.t('permissions:alertTitle'),
                description: i18n.t('permissions:overlayDescription'),
            });
            if (!shouldRequestPermission) {
                return;
            }
            const overlayStatue = await this.requestOverlayPermission();
            if (overlayStatue !== 'authorized') {
                await this.dialogStore.openDialog(BaseDialog, {
                    title: i18n.t('permissions:alertTitle'),
                    description: i18n.t('permissions:blocked'),
                    cancelText: i18n.t('common:cancel'),
                });
                return;
            }
        }
        await Promise.all([this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection.isCallRecognitionOn, 'true'), this.startCallRecognitionForegroundService()]);
    }

    private async stopCallDetection() {
        await Promise.all([this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection.isCallRecognitionOn, 'false'), this.stopCallRecognitionForegroundService()]);
    }

    private getNotificationListenerPermissionStatus(): Promise<PermissionStatus> {
        return MadaNotificationsModule.getNotificationListenerPermissionStatus();
    }

    private stopCallRecognitionForegroundService(): Promise<boolean> {
        return MadaNotificationsModule.stopCallRecognitionForegroundService();
    }

    private get notificationConfig(): NotificationConfig {
        return {
            id: 78,
            color: 'blue',
            target: 'Home',
            hideWhen: true,
            icon: 'ic_noti',
            isOnGoing: true,
            priority: Priority.PRIORITY_MIN,
            button: i18n.t('common:notificationBtn'),
            channelId: NOTIFICATION_MAP.CHANNELS.BACKGROUND,
            text: i18n.t('callDetection:foregroundNotificationText'),
            title: i18n.t('callDetection:foregroundNotificationTitle'),
            ticker: i18n.t('callDetection:foregroundNotificationTicker'),
        };
    }
}
