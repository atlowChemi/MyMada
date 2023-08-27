import messaging from '@react-native-firebase/messaging';
import { inject, injectable } from 'inversify';
import { computed, makeObservable } from 'mobx';
import { fromPromise } from 'mobx-utils';
import { SharedPreferencesStore } from './SharedPreferencesStore';

@injectable()
export class MessagingStore {
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;
    private readonly messaging = messaging();
    private registerDeviceForRemoteMessagesPromise = fromPromise(this.messaging.registerDeviceForRemoteMessages());

    constructor() {
        makeObservable(this, {
            token: computed,
        });
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
        await this.registerDeviceForRemoteMessagesPromise;
        const token = await this.messaging.getToken();
        await this.updatePreferences(token);
        return token;
    }
}
