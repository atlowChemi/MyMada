import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { NativeModules } from 'react-native';
import { makeAutoObservable, observable, runInAction } from 'mobx';

const { SharedPreferencesModule } = NativeModules;

type PreferenceChannel = 'Settings' | 'permissionStatus' | 'rate_app' | 'notifications';

@injectable()
export class SharedPreferencesStore {
    private readonly cache = observable.map<PreferenceChannel, { [key: string]: string }>();

    constructor() {
        makeAutoObservable(this, {});
    }

    public get settingsChannel() {
        return fromPromise(this.loadChannel());
    }

    public async loadChannel(name: PreferenceChannel = 'Settings') {
        if (this.cache.has(name)) {
            return this.cache.get(name)!;
        }
        return await this.getAll(name);
    }

    public async getItemIgnoreCache(key: string, channel: PreferenceChannel) {
        const value = (await SharedPreferencesModule.getItem(channel, key)) as string | null;
        return value;
    }

    private async getAll(channel: PreferenceChannel) {
        const allPreferences = (await SharedPreferencesModule.getAll(channel)) as [string, string][];
        const entries = Object.fromEntries(allPreferences ?? []);
        runInAction(() => this.cache.set(channel, entries));
        return entries;
    }

    public async setItem(key: string, value: string, channel: PreferenceChannel = 'Settings') {
        await SharedPreferencesModule.setItem(channel, key, value);
        if (!this.cache.has(channel)) {
            return;
        }
        const channelData = this.cache.get(channel)!;
        runInAction(() => (channelData[key] = value));
    }

    public async setItems(values: { [key: string]: string }, channel: PreferenceChannel = 'Settings') {
        await SharedPreferencesModule.setItems(channel, values);
        if (!this.cache.has(channel)) {
            return;
        }
        const channelData = this.cache.get(channel)!;
        runInAction(() => Object.assign(channelData, values));
    }

    public makePrefBoolean(value: string | undefined, defaultValue: boolean) {
        if (value === undefined) {
            return defaultValue;
        }
        return value === 'true';
    }
}
