import messaging from '@react-native-firebase/messaging';
import crashlytics from '@react-native-firebase/crashlytics';
import { fromPromise } from 'mobx-utils';
import { inject, injectable } from 'inversify';
import { computed, makeObservable } from 'mobx';
import { notificationsService } from './NotificationService';
import { SharedPreferencesStore } from './SharedPreferencesStore';
import type { FirebaseMessagingTypes } from '@react-native-firebase/messaging';

interface NewChat {
    name: string;
    msg: string;
    key: string;
    type: 'newChat';
}
interface CodeApproval {
    code: string;
    key: string;
    type: 'codeApproval';
}
type MessagePayload = NewChat | CodeApproval;

@injectable()
export class MessagingStore {
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;
    private readonly messaging = messaging();

    constructor() {
        makeObservable(this, { token: computed });
        this.subscribeToNews();
        this.messaging.onTokenRefresh(this.updatePreferences);
    }

    private updatePreferences = async (token: string) => {
        this.sharedPreferencesStore.name = 'Settings';
        return await this.sharedPreferencesStore.setItem('fcm', token);
    };

    public get token() {
        return fromPromise(this.getToken());
    }

    private async getToken() {
        const token = await this.messaging.getToken();
        await Promise.all([crashlytics().setUserId(token), this.updatePreferences(token)]);
        return token;
    }

    private async subscribeToNews() {
        try {
            await this.messaging.subscribeToTopic('news');
        } catch (error) {
            crashlytics().log('Failed to subscribe to news topic:');
            error instanceof Error && crashlytics().recordError(error);
        }
    }

    public onMessage = async (message: FirebaseMessagingTypes.RemoteMessage) => {
        if (!message.data) {
            return;
        }
        const msgData = message.data as unknown as MessagePayload;
        const commonConfig = {
            channelId: 'Push',
            icon: 'ic_siren',
            largeIcon: 'ic_launcher',
            id: Date.now(),
            useDefaults: true,
            isAutoCancelable: true,
        };
        if (msgData.type === 'newChat') {
            await notificationsService.publishNotification({
                ...commonConfig,
                text: 'הודעה חדשה מאת ההנהלה', // TODO: i18n
                title: 'הודעה צ׳אט חדשה', // TODO: i18n
                bigText: msgData.msg.toString(),
                ticker: 'הודעה צ׳אט חדשה בעמוד יצירת קשר', // TODO: i18n
                target: 'ContactUs',
            });
            return;
        }
        if (msgData.type === 'codeApproval') {
            const { code, key } = msgData;
            await notificationsService.publishNotification({
                ...commonConfig,
                text: `קוד ההפעלה שלך הינו:  ${code}`, // TODO: i18n
                bigText: `קוד ההפעלה שלך הינו:  ${code}`, // TODO: i18n
                title: 'קוד ההפעלה אושר', // TODO: i18n
                code,
                key,
                target: 'RequestCode', // TODO
            });
            return;
        }
        // if (typeof msg === 'string' && msg.length && typeof title === 'string' && title.length) {
        //     await notificationsService.publishNotification({
        //         ...commonConfig,
        //         text: title,
        //         title: 'הודעה חדשה!',
        //         bigText: msg,
        //     });
        // }
    };

    public setUpMessaging = () => {
        return this.messaging.onMessage(this.onMessage);
    };
}
