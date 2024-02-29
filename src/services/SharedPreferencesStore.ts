import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { NativeModules } from 'react-native';
import { makeAutoObservable, observable, runInAction, when } from 'mobx';
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
export const PREF_KEYS = {
    general: {
        tools: 'Tools',
        isMarkedAsVolunteer: 'realM',
        firebaseGeneratedKey: 'firebaseGenratedKey',
        hasVisitedPrivacy: 'privacy',
        hasVisitedSettings: 'oldU',
    },
    region: {
        selectedRegionIndex: 'merhav',
        hasUserSelectedDefaultDispatch: 'pickedMoked',
    },
    chat: {
        userName: 'UserName',
    },
    contractions: {
        savedContractionsJSON: 'contractList',
        contractionPageExitTime: 'contractLong',
    },
    callDetection: {
        splitNotificationByCallType: 'cut',
        isCallRecognitionOn: 'Mapp',
        sendNotificationOnCall: 'not',
        openPopUpOnCall: 'win',
        detectServiceCalls: 'callT6',
        detectAdministrativeCalls: 'callT4',
        notifySoundByDeviceStatus: 'stat',
        notifyVibrationByDeviceStatus: 'statVib',
        grouped: {
            vibrate: 'Nvib',
            sound: 'Nsou',
            overrideSound: 'Napp',
        },
        onCall: {
            detectCalls: 'callT1',
            vibrate: 'NvibT1',
            sound: 'NsouT1',
            overrideSound: 'NappT1',
        },
        group: {
            detectCalls: 'callT2',
            vibrate: 'NvibT2',
            sound: 'NsouT2',
            overrideSound: 'NappT2',
        },
        shift: {
            detectCalls: 'callT3',
            vibrate: 'NvibT3',
            sound: 'NsouT3',
            overrideSound: 'NappT3',
        },
        administrative: {
            vibrate: 'NvibT4',
            sound: 'NsouT4',
            overrideSound: 'NappT4',
        },

        dayAndNightSettings: {
            nightStart: 'nightStart',
            nightEnd: 'nightEnd',
            dayNotificationLevel: 'sleepDay',
            nightNotificationLevel: 'sleepNight',
            overallNotificationLevel: 'sleep',
        },
        advancedSettings: {
            overridePreviousCalls: 'ride',
            infiniteAlert: 'loop',
            autoStopAlert: 'killS',
            autoStopAlertDuration: 'timeK',
            overrideVolume: 'volumeApp',
            volume: 'volumeAppNum',
        },
    },
} as const;

@injectable()
export class SharedPreferencesStore {
    private readonly cache = observable.map<PreferenceChannel, { [key: string]: string | undefined }>(undefined);

    constructor() {
        makeAutoObservable<SharedPreferencesStore, 'cache'>(this, { cache: false });
        this.getAll('Settings');
    }

    public get applicationSettings() {
        const explicitUpdate = this.cache.get('Settings'); // eslint-disable-line @typescript-eslint/no-unused-vars
        return fromPromise(this.getApplicationSettings());
    }

    public async loadChannel(name: PreferenceChannel = 'Settings') {
        return this.cache.get(name) || (await this.getAll(name));
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
        runInAction(() => this.cache.set(channel, { ...channelData, [key]: value }));
    }

    public async setItems(values: { [key: string]: string }, channel: PreferenceChannel = 'Settings') {
        await SharedPreferencesModule.setItems(channel, values);
        if (!this.cache.has(channel)) {
            return;
        }
        const channelData = this.cache.get(channel)!;
        runInAction(() => this.cache.set(channel, { ...channelData, ...values }));
    }

    public makePrefBoolean(value: string | undefined, defaultValue: boolean) {
        if (value === undefined) {
            return defaultValue;
        }
        return value === 'true';
    }

    private async getAll(channel: PreferenceChannel) {
        const allPreferences = (await SharedPreferencesModule.getAll(channel)) as [string, string][];
        const entries = observable(Object.fromEntries(allPreferences ?? []));
        runInAction(() => this.cache.set(channel, entries));
        return this.cache.get(channel)!;
    }

    private async getApplicationSettings() {
        if (!this.cache.has('Settings')) {
            await when(() => this.cache.has('Settings'));
        }
        const settings = this.cache.get('Settings')!;
        const toolsString = settings[PREF_KEYS.general.tools] ?? `[${Array.from(TOOL_SHARED_PREF_MAP.values()).join(', ')}]`;
        return {
            general: {
                toolsToShow: new Set(toolsString.split(/[[\],\s]/).filter(Boolean)),
                isMarkedAsVolunteer: this.makePrefBoolean(settings[PREF_KEYS.general.isMarkedAsVolunteer], false),
                firebaseGeneratedKey: settings[PREF_KEYS.general.firebaseGeneratedKey] ?? '',
                hasVisitedPrivacy: this.makePrefBoolean(settings[PREF_KEYS.general.hasVisitedPrivacy], false),
                hasVisitedSettings: this.makePrefBoolean(settings[PREF_KEYS.general.hasVisitedSettings], false),
            },
            region: {
                selectedRegionIndex: Number.parseInt(settings[PREF_KEYS.region.selectedRegionIndex] || '0', 10),
                hasUserSelectedDefaultDispatch: this.makePrefBoolean(settings[PREF_KEYS.region.hasUserSelectedDefaultDispatch], false),
            },
            chat: {
                userName: settings[PREF_KEYS.chat.userName] ?? '',
            },
            contractions: {
                savedContractionsJSON: settings[PREF_KEYS.contractions.savedContractionsJSON],
                contractionPageExitTime: settings[PREF_KEYS.contractions.contractionPageExitTime],
            },
            callDetection: {
                splitNotificationByCallType: this.makePrefBoolean(settings[PREF_KEYS.callDetection.splitNotificationByCallType], false),
                isCallRecognitionOn: this.makePrefBoolean(settings[PREF_KEYS.callDetection.isCallRecognitionOn], false),
                sendNotificationOnCall: this.makePrefBoolean(settings[PREF_KEYS.callDetection.sendNotificationOnCall], true),
                openPopUpOnCall: this.makePrefBoolean(settings[PREF_KEYS.callDetection.openPopUpOnCall], true),
                detectServiceCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.detectServiceCalls], true),
                detectAdministrativeCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.detectAdministrativeCalls], true),
                notifySoundByDeviceStatus: this.makePrefBoolean(settings[PREF_KEYS.callDetection.notifySoundByDeviceStatus], false),
                notifyVibrationByDeviceStatus: this.makePrefBoolean(settings[PREF_KEYS.callDetection.notifyVibrationByDeviceStatus], false),
                grouped: {
                    vibrate: this.makePrefBoolean(settings[PREF_KEYS.callDetection.grouped.vibrate], true),
                    sound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.grouped.sound], true),
                    overrideSound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.grouped.overrideSound], false),
                },
                onCall: {
                    detectCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.onCall.detectCalls], true),
                    vibrate: this.makePrefBoolean(settings[PREF_KEYS.callDetection.onCall.vibrate], true),
                    sound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.onCall.sound], true),
                    overrideSound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.onCall.overrideSound], false),
                },
                group: {
                    detectCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.group.detectCalls], true),
                    vibrate: this.makePrefBoolean(settings[PREF_KEYS.callDetection.group.vibrate], true),
                    sound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.group.sound], true),
                    overrideSound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.group.overrideSound], false),
                },
                shift: {
                    detectCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.shift.detectCalls], true),
                    vibrate: this.makePrefBoolean(settings[PREF_KEYS.callDetection.shift.vibrate], true),
                    sound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.shift.sound], true),
                    overrideSound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.shift.overrideSound], false),
                },
                administrative: {
                    vibrate: this.makePrefBoolean(settings[PREF_KEYS.callDetection.administrative.vibrate], true),
                    sound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.administrative.sound], true),
                    overrideSound: this.makePrefBoolean(settings[PREF_KEYS.callDetection.administrative.overrideSound], false),
                },
                dayAndNightSettings: {
                    nightStart: Number.parseInt(settings[PREF_KEYS.callDetection.dayAndNightSettings.nightStart] || '23', 10),
                    nightEnd: Number.parseInt(settings[PREF_KEYS.callDetection.dayAndNightSettings.nightEnd] || '7', 10),
                    dayNotificationLevel: Number.parseInt(settings[PREF_KEYS.callDetection.dayAndNightSettings.dayNotificationLevel] || '0', 10),
                    nightNotificationLevel: Number.parseInt(settings[PREF_KEYS.callDetection.dayAndNightSettings.nightNotificationLevel] || '0', 10),
                    overallNotificationLevel: Number.parseInt(settings[PREF_KEYS.callDetection.dayAndNightSettings.overallNotificationLevel] || '0', 10),
                },
                advancedSettings: {
                    overridePreviousCalls: this.makePrefBoolean(settings[PREF_KEYS.callDetection.advancedSettings.overridePreviousCalls], false),
                    infiniteAlert: this.makePrefBoolean(settings[PREF_KEYS.callDetection.advancedSettings.infiniteAlert], false),
                    autoStopAlert: this.makePrefBoolean(settings[PREF_KEYS.callDetection.advancedSettings.autoStopAlert], false),
                    autoStopAlertDuration: settings[PREF_KEYS.callDetection.advancedSettings.autoStopAlertDuration] ?? '6',
                    overrideVolume: this.makePrefBoolean(settings[PREF_KEYS.callDetection.advancedSettings.overrideVolume], true),
                    volume: Number.parseInt(settings[PREF_KEYS.callDetection.advancedSettings.volume] || '0', 10),
                },
            },
        };
    }
}
