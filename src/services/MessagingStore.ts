import messaging from '@react-native-firebase/messaging';
import { injectable } from 'inversify';
import { computed, makeObservable } from 'mobx';
import { fromPromise } from 'mobx-utils';

@injectable()
export class MessagingStore {
    private readonly messaging = messaging();
    private registerDeviceForRemoteMessagesPromise = fromPromise(this.messaging.registerDeviceForRemoteMessages());

    constructor() {
        makeObservable(this, {
            token: computed,
        });
    }

    public get token() {
        return fromPromise(this.getToken());
    }

    private async getToken() {
        await this.registerDeviceForRemoteMessagesPromise;
        return await this.messaging.getToken();
    }
}
