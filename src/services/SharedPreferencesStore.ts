import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { action, computed, makeObservable, observable, runInAction } from 'mobx';
import SharedPreferences from 'react-native-shared-preferences';

type PreferenceChannel = 'Settings' | 'permissionStatus' | 'rate_app' | 'notifications';

@injectable()
export class SharedPreferencesStore {
    private _name: PreferenceChannel = 'Settings';
    private readonly cache = observable.map<PreferenceChannel, { [key: string]: string }>();

    constructor() {
        this.name = 'Settings';
        makeObservable<SharedPreferencesStore, '_name'>(this, {
            name: computed,
            setItem: action,
            _name: observable.ref,
            activeChannel: computed,
        });
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
        if (this.cache.has(name)) {
            return this.cache.get(name)!;
        }
        return await this.getAll();
    }

    public async getItem(key: string) {
        return new Promise<string | null>(resolve => {
            SharedPreferences.getItem(key, resolve);
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
}
