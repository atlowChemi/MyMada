import { inject, injectable } from 'inversify';
import { Linking, NativeModules, NativeEventEmitter, PermissionsAndroid } from 'react-native';
import i18n from '../localization';
import { DialogStore } from './DialogStore';
import { NOTIFICATION_LEVEL_VALUES, RingerMode, colors } from '../consts';
import { audioGenerator } from './AudioGenerator';
import { BaseDialog } from '../containers/dialogs/BaseDialog';
import { PREF_KEYS, SharedPreferencesStore } from './SharedPreferencesStore';
import { Priority, type NotificationConfig, NOTIFICATION_MAP, NotificationService } from './NotificationService';

const { MadaNotificationsModule } = NativeModules;
type PermissionStatus = 'unknown' | 'authorized' | 'denied';

export const enum CallType {
    OnCall = 1,
    Group = 2,
    Shift = 3,
    Administrative = 4,
    OldMessageSyntax = 5,
    Service = 6,
}

@injectable()
export class MadaNotificationsStore {
    private readonly eventEmitter = new NativeEventEmitter(MadaNotificationsModule);

    constructor(
        @inject(DialogStore) private readonly dialogStore: DialogStore,
        @inject(NotificationService) private readonly notificationService: NotificationService,
        @inject(SharedPreferencesStore) private readonly sharedPreferencesStore: SharedPreferencesStore,
    ) {
        this.eventEmitter.addListener('CALL_DETECTION_STATE', () => this.sharedPreferencesStore.loadChannel('Settings', true));
    }

    public hasMadaApp(): Promise<boolean> {
        return MadaNotificationsModule.hasMadaApp();
    }

    public get appData(): {
        readonly uid: number;
        readonly VERSION_NAME?: string;
        readonly APPLICATION_ID: string;
        readonly DEBUG?: boolean;
        readonly BUILD_TYPE?: string;
    } {
        return MadaNotificationsModule.getConstants();
    }

    public buildPendingIntentForTestCall(): Promise<string> {
        return MadaNotificationsModule.buildPendingIntentForTestCall();
    }

    public broadcastCallData(config: NonNullable<Awaited<ReturnType<MadaNotificationsStore['calculateCallData']>>>): Promise<void> {
        return MadaNotificationsModule.broadcastCallData(config);
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

    public async calculateCallData(callDetection: Awaited<SharedPreferencesStore['applicationSettings']>['callDetection'], { text, title, uuid }: { text: string; title: string; uuid: string }) {
        const { sendNotificationOnCall, openPopUpOnCall } = callDetection;
        if (['מגן דוד אדום', 'מערך הכוננים הלאומי'].includes(title) || (!sendNotificationOnCall && !openPopUpOnCall)) {
            return undefined;
        }

        // const time = title.replace(/[*](אירוע חדש|New Event|הודעה מנהלתית חדשה)[*]/, '');
        const callType = this.getCallType(text, title);

        const notificationSettings = this.getNotificationSettingsByCallType(callDetection, callType);

        if (!notificationSettings.shouldNotify) {
            return undefined;
        }

        const ringerMode = await audioGenerator.getRingerMode();
        const silent = ringerMode !== RingerMode.NORMAL;
        const silentVib = ringerMode !== RingerMode.VIBRATE;
        const hour = new Date().getHours();
        const { nightStart, nightEnd, nightNotificationLevel, dayNotificationLevel } = callDetection.dayAndNightSettings;
        if (hour <= nightEnd || hour >= nightStart) {
            notificationSettings.sound ||= nightNotificationLevel === NOTIFICATION_LEVEL_VALUES.sound;
            notificationSettings.vibrate ||= nightNotificationLevel !== NOTIFICATION_LEVEL_VALUES.silent;
        } else {
            notificationSettings.sound ||= dayNotificationLevel === NOTIFICATION_LEVEL_VALUES.sound;
            notificationSettings.vibrate ||= dayNotificationLevel !== NOTIFICATION_LEVEL_VALUES.silent;
        }

        const isServiceCall = [CallType.Administrative, CallType.Service].includes(callType);
        const notification: NotificationConfig = {
            id: 100, // TODO: calculate based on override setting.
            page: 'Form',
            bigText: text,
            icon: 'ic_siren',
            target: 'Dispatch',
            isAutoCancelable: true,
            largeIcon: 'ic_launcher',
            text: 'יש ללחוץ כדי להכתיב טפסים',
            channelId: NOTIFICATION_MAP.CHANNELS.CALLS,
            title: isServiceCall ? 'הודעה חדשה באפליקציה!' : 'אירוע חדש באפליקציה!',
            ticker: isServiceCall ? 'התקבלה הודעה חדש באפליקציה!' : 'התקבל אירוע חדש באפליקציה!',
        };
        return {
            uuid,
            text,
            callType,
            popup: openPopUpOnCall,
            uri: notificationSettings.uri,
            volume: callDetection.advancedSettings.volume,
            autoStopAlert: callDetection.advancedSettings.autoStopAlert,
            infiniteAlert: callDetection.advancedSettings.infiniteAlert,
            overrideVolume: callDetection.advancedSettings.overrideVolume,
            notification: sendNotificationOnCall ? notification : undefined,
            overridePreviousCalls: callDetection.advancedSettings.overridePreviousCalls,
            autoStopAlertDuration: callDetection.advancedSettings.autoStopAlertDuration,
            sound: notificationSettings.sound && (!callDetection.notifySoundByDeviceStatus || !silent),
            vibrate: notificationSettings.vibrate && (!callDetection.notifyVibrationByDeviceStatus || !silentVib),
        };
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
        const notificationListenerPermissionStatus = await this.getNotificationListenerPermissionStatus();
        if (notificationListenerPermissionStatus !== 'authorized') {
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
        const { status: notificationPermissionStatus } = await this.notificationService.requestNotificationPermission(this.appData);
        if (notificationPermissionStatus !== PermissionsAndroid.RESULTS.GRANTED) {
            return;
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

    private getCallType(text: string, title: string): CallType {
        if (text.includes('על מידע זה חל חיסיון רפואי')) {
            return CallType.OldMessageSyntax;
        }
        if (['שירות', 'כלכלה', 'תפילה'].some(v => text.includes(v))) {
            return CallType.Service;
        }
        if (title.includes('*הודעה מנהלתית חדשה*') || ['משמרת בנה', 'מינהלתי', 'מנהלתי'].some(v => text.includes(v))) {
            return CallType.Administrative;
        }
        if (['*New Event*', '*אירוע חדש*'].some(v => title.includes(v))) {
            if (text.includes('הודעת קבוצת')) {
                return CallType.Group;
            }
            if (text.includes('הודעת צוות')) {
                return CallType.Shift;
            }
        }
        return CallType.OnCall;
    }

    private getNotificationSettingsByCallType(callDetection: Awaited<SharedPreferencesStore['applicationSettings']>['callDetection'], callType: CallType) {
        if ([CallType.Administrative, CallType.Service].includes(callType)) {
            return {
                shouldNotify: callType === CallType.Administrative ? callDetection.detectAdministrativeCalls : callDetection.detectServiceCalls,
                vibrate: callDetection.administrative.vibrate,
                sound: callDetection.administrative.sound,
                overrideSound: callDetection.administrative.overrideSound,
                uri: callDetection.administrative.ringtone,
            };
        }
        if (!callDetection.splitNotificationByCallType) {
            return {
                shouldNotify: true,
                vibrate: callDetection.grouped.vibrate,
                sound: callDetection.grouped.sound,
                overrideSound: callDetection.grouped.overrideSound,
                uri: callDetection.grouped.ringtone,
            };
        }
        const callDetectionKey = callType === CallType.Shift ? 'shift' : CallType.Group ? 'group' : 'onCall';
        return {
            shouldNotify: callDetection[callDetectionKey].detectCalls,
            vibrate: callDetection[callDetectionKey].vibrate,
            sound: callDetection[callDetectionKey].sound,
            overrideSound: callDetection[callDetectionKey].overrideSound,
            uri: callDetection[callDetectionKey].ringtone,
        };
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
