import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { NativeModules } from 'react-native';
import { makeAutoObservable, observable, runInAction } from 'mobx';
import type { Tools } from './NavigationStore';

const { SharedPreferencesModule } = NativeModules;

type PreferenceChannel = 'Settings' | 'permissionStatus' | 'rate_app' | 'notifications';

export const TOOL_SHARED_PREF_MAP = new Map<Tools, string>([
    ['ContractionTimer', 'contrac'],
    ['PulseCalc', 'pulse'],
    ['MetronomeActivity', 'metro'],
    ['VitalsChecker', 'vital'],
    ['OxygenCalculator', 'oxy'],
    ['ApgarCalculator', 'apgar'],
    ['GlazgowCalculator', 'glaz'],
    ['Dictionary', 'dict'],
    ['BlsProtocoles', 'prot'],
    ['AlsProtocoles', 'prot'],
    ['Kuntres', 'prot'],
]);

@injectable()
export class SharedPreferencesStore {
    private explicitUpdate = 0;
    private readonly cache = observable.map<PreferenceChannel, { [key: string]: string }>();

    constructor() {
        makeAutoObservable(this, {});
    }

    public get settingsChannel() {
        return fromPromise(this.loadChannel());
    }

    public get selectedTools() {
        this.explicitUpdate;
        return fromPromise(this.getSelectedTools());
    }

    public async loadChannel(name: PreferenceChannel = 'Settings', force: boolean = false) {
        if (!force && this.cache.has(name)) {
            return this.cache.get(name)!;
        }
        return await this.getAll(name);
    }

    public async getItemIgnoreCache(key: string, channel: PreferenceChannel) {
        const value = (await SharedPreferencesModule.getItem(channel, key)) as string | null;
        return value;
    }

    public async setItem(key: string, value: string, channel: PreferenceChannel = 'Settings') {
        await SharedPreferencesModule.setItem(channel, key, value);
        if (!this.cache.has(channel)) {
            return;
        }
        const channelData = this.cache.get(channel)!;
        runInAction(() => {
            channelData[key] = value;
            this.explicitUpdate++;
        });
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

    private async getSelectedTools() {
        const { Tools: toolsArrString = `[${Array.from(TOOL_SHARED_PREF_MAP.values()).join(', ')}]` } = await this.settingsChannel;
        const tools = toolsArrString.split(/[[\],\s]/).filter(Boolean);
        return new Set(tools);
    }

    private async getAll(channel: PreferenceChannel) {
        const allPreferences = (await SharedPreferencesModule.getAll(channel)) as [string, string][];
        const entries = observable(Object.fromEntries(allPreferences ?? []));
        runInAction(() => this.cache.set(channel, entries));
        return this.cache.get(channel)!;
    }
}
