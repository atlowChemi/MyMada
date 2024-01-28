import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { makeAutoObservable, observable, runInAction, when } from 'mobx';
import SharedPreferences from 'react-native-shared-preferences';

type PreferenceChannel = 'Settings' | 'permissionStatus' | 'rate_app' | 'notifications';

@injectable()
export class SharedPreferencesStore {
    private isDoingBackgroundChanges = false;
    private _name: PreferenceChannel = 'Settings';
    private readonly cache = observable.map<PreferenceChannel, { [key: string]: string }>();

    constructor() {
        this.name = 'Settings';
        makeAutoObservable(this, {});
    }

    public get name() {
        return this._name;
    }
    public set name(name: PreferenceChannel) {
        this._name = name;
        SharedPreferences.setName(name);
    }

    public get activeChannel() {
        const activeChannel = this.name;
        return fromPromise(this.loadChannel(activeChannel));
    }

    private async loadChannel(name: PreferenceChannel) {
        await when(() => this.isDoingBackgroundChanges === false);
        if (this.cache.has(name)) {
            return this.cache.get(name)!;
        }
        return await this.getAll();
    }

    private useTempChannel(channel?: PreferenceChannel) {
        if (!channel) {
            return undefined;
        }
        this.isDoingBackgroundChanges = true;
        SharedPreferences.setName(channel);

        return () => {
            SharedPreferences.setName(this._name);
            this.isDoingBackgroundChanges = false;
        };
    }

    public async getItemIgnoreCache(key: string, channel?: PreferenceChannel) {
        return new Promise<string | null>(resolve => {
            let tempChannelCleanup: undefined | (() => void);
            try {
                tempChannelCleanup = this.useTempChannel(channel);
                SharedPreferences.getItem(key, resolve);
            } finally {
                tempChannelCleanup?.();
            }
        });
    }

    private async getAll() {
        const allPreferences = await new Promise<[string, string][] | null>(resolve => {
            SharedPreferences.getAll(resolve as any);
        });
        const entries = Object.fromEntries(allPreferences ?? []);
        runInAction(() => this.cache.set(this.name, entries));
        return entries;
    }

    public async setItem(key: string, value: string) {
        SharedPreferences.setItem(key, value);
        const channel = await this.activeChannel;
        runInAction(() => {
            channel[key] = value;
        });
    }

    public async setItemOnChannel(channel: PreferenceChannel, key: string, value: string) {
        if (channel === this._name) {
            return await this.setItem(key, value);
        }
        let tempChannelCleanup: undefined | (() => void);
        try {
            tempChannelCleanup = this.useTempChannel(channel);
            SharedPreferences.setItem(key, value);
        } finally {
            tempChannelCleanup?.();
        }
    }

    public makePrefBoolean(value: string | undefined, defaultValue: boolean) {
        if (value === undefined) {
            return defaultValue;
        }
        return value === 'true';
    }
}
