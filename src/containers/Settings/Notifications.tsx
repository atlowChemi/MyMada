import { injectable } from 'inversify';
import { computedFn, fromPromise } from 'mobx-utils';
import { StyleSheet } from 'react-native';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { PageLoader } from '../../Components/Loader';
import { SettingsItem } from '../../Components/SettingsItem';
import { BasePageView } from '../../Components/BasePageView';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { DeepValueOf } from '../../utils';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';

type Group = 'grouped' | 'onCall' | 'group' | 'shift' | 'administrative';
type DetectionToggleableGroup = Exclude<Group, 'grouped' | 'administrative'>;
const isToggleableGroup = (group: Group): group is DetectionToggleableGroup => !['grouped', 'administrative'].includes(group);
const PREF_KEYS = {
    splitNotificationByCallType: 'cut',
    detectServiceCalls: 'callT6',
    detectAdministrativeCalls: 'callT4',

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
} as const;

@injectable()
class NotificationsLocalStore {
    public notificationSettings = fromPromise(this.getNotificationSettings());

    constructor(private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this);
    }

    public get groupsToRender(): Group[] {
        if (this.notificationSettings.state !== 'fulfilled') {
            return [];
        }
        const { splitNotificationByCallType, detectServiceCalls, detectAdministrativeCalls } = this.notificationSettings.value;
        const result = [
            ...(splitNotificationByCallType ? (['onCall', 'group', 'shift'] as const) : (['grouped'] as const)),
            ...(!detectServiceCalls && !detectAdministrativeCalls ? [] : (['administrative'] as const)),
        ];
        return result;
    }

    public get updateValue() {
        return computedFn((key: DeepValueOf<typeof PREF_KEYS>) => async (value: boolean) => {
            await this.sharedPreferencesStore.setItem(key, value.toString());
            this.updateNotificationObservable();
        });
    }

    public async pickGroupRingtone() {
        // TODO: validate required permissions are available, open picker and set relevant ringtone
    }

    private async getNotificationSettings() {
        const {
            [PREF_KEYS.splitNotificationByCallType]: splitNotificationByCallType,
            [PREF_KEYS.detectServiceCalls]: detectServiceCalls,
            [PREF_KEYS.detectAdministrativeCalls]: detectAdministrativeCalls,

            [PREF_KEYS.grouped.vibrate]: groupedVibrate,
            [PREF_KEYS.grouped.sound]: groupedSound,
            [PREF_KEYS.grouped.overrideSound]: groupedOverrideSound,

            [PREF_KEYS.onCall.detectCalls]: onCallDetectCalls,
            [PREF_KEYS.onCall.vibrate]: onCallVibrate,
            [PREF_KEYS.onCall.sound]: onCallSound,
            [PREF_KEYS.onCall.overrideSound]: onCallOverrideSound,

            [PREF_KEYS.group.detectCalls]: groupDetectCalls,
            [PREF_KEYS.group.vibrate]: groupVibrate,
            [PREF_KEYS.group.sound]: groupSound,
            [PREF_KEYS.group.overrideSound]: groupOverrideSound,

            [PREF_KEYS.shift.detectCalls]: shiftDetectCalls,
            [PREF_KEYS.shift.vibrate]: shiftVibrate,
            [PREF_KEYS.shift.sound]: shiftSound,
            [PREF_KEYS.shift.overrideSound]: shiftOverrideSound,

            [PREF_KEYS.administrative.vibrate]: administrativeVibrate,
            [PREF_KEYS.administrative.sound]: administrativeSound,
            [PREF_KEYS.administrative.overrideSound]: administrativeOverrideSound,
        } = await this.sharedPreferencesStore.settingsChannel;
        return {
            splitNotificationByCallType: this.sharedPreferencesStore.makePrefBoolean(splitNotificationByCallType, false),
            detectServiceCalls: this.sharedPreferencesStore.makePrefBoolean(detectServiceCalls, true),
            detectAdministrativeCalls: this.sharedPreferencesStore.makePrefBoolean(detectAdministrativeCalls, true),

            grouped: {
                vibrate: this.sharedPreferencesStore.makePrefBoolean(groupedVibrate, true),
                sound: this.sharedPreferencesStore.makePrefBoolean(groupedSound, true),
                overrideSound: this.sharedPreferencesStore.makePrefBoolean(groupedOverrideSound, false),
            },
            onCall: {
                detectCalls: this.sharedPreferencesStore.makePrefBoolean(onCallDetectCalls, true),
                vibrate: this.sharedPreferencesStore.makePrefBoolean(onCallVibrate, true),
                sound: this.sharedPreferencesStore.makePrefBoolean(onCallSound, true),
                overrideSound: this.sharedPreferencesStore.makePrefBoolean(onCallOverrideSound, false),
            },
            group: {
                detectCalls: this.sharedPreferencesStore.makePrefBoolean(groupDetectCalls, true),
                vibrate: this.sharedPreferencesStore.makePrefBoolean(groupVibrate, true),
                sound: this.sharedPreferencesStore.makePrefBoolean(groupSound, true),
                overrideSound: this.sharedPreferencesStore.makePrefBoolean(groupOverrideSound, false),
            },
            shift: {
                detectCalls: this.sharedPreferencesStore.makePrefBoolean(shiftDetectCalls, true),
                vibrate: this.sharedPreferencesStore.makePrefBoolean(shiftVibrate, true),
                sound: this.sharedPreferencesStore.makePrefBoolean(shiftSound, true),
                overrideSound: this.sharedPreferencesStore.makePrefBoolean(shiftOverrideSound, false),
            },
            administrative: {
                vibrate: this.sharedPreferencesStore.makePrefBoolean(administrativeVibrate, true),
                sound: this.sharedPreferencesStore.makePrefBoolean(administrativeSound, true),
                overrideSound: this.sharedPreferencesStore.makePrefBoolean(administrativeOverrideSound, false),
            },
        };
    }

    private updateNotificationObservable() {
        this.notificationSettings = fromPromise(this.getNotificationSettings(), this.notificationSettings);
    }
}

const NotificationGroup = observer<{ group: Group; localStore: NotificationsLocalStore }>(({ group, localStore }) => {
    const { t } = useTranslation();

    if (localStore.notificationSettings.state !== 'fulfilled') {
        return null;
    }

    const callType = t(`settings:notification:${group}:callType`) as string;

    return (
        <>
            <Text style={styles.pageTitle}>{t(`settings:notification:${group}:title`)}</Text>
            {isToggleableGroup(group) && (
                <SettingsItem
                    type="switch"
                    onChange={localStore.updateValue(PREF_KEYS[group].detectCalls)}
                    value={localStore.notificationSettings.value[group].detectCalls}
                    label={t('settings:notification:detect:title', { callType: t(`settings:notification:${group}:titleCallType`, callType) })}
                    subLabel={t(`settings:notification:detect:subTitle${localStore.notificationSettings.value[group].detectCalls ? 'Enabled' : 'Disabled'}`, { callType })}
                />
            )}
            {!isToggleableGroup(group) ||
                (localStore.notificationSettings.value[group].detectCalls && (
                    <>
                        <SettingsItem
                            type="switch"
                            label={t('settings:notification:vibrate:title')}
                            onChange={localStore.updateValue(PREF_KEYS[group].vibrate)}
                            value={localStore.notificationSettings.value[group].vibrate}
                            subLabel={t(`settings:notification:vibrate:subTitle${localStore.notificationSettings.value[group].vibrate ? 'Enabled' : 'Disabled'}`)}
                        />
                        <SettingsItem
                            type="switch"
                            label={t('settings:notification:sound:title')}
                            onChange={localStore.updateValue(PREF_KEYS[group].sound)}
                            value={localStore.notificationSettings.value[group].sound}
                            subLabel={t(`settings:notification:sound:subTitle${localStore.notificationSettings.value[group].sound ? 'Enabled' : 'Disabled'}`)}
                        />
                        <SettingsItem
                            type="switch"
                            label={t('settings:notification:overrideSound:title')}
                            disabled={!localStore.notificationSettings.value[group].sound}
                            onChange={localStore.updateValue(PREF_KEYS[group].overrideSound)}
                            value={localStore.notificationSettings.value[group].overrideSound}
                            subLabel={t(`settings:notification:overrideSound:subTitle${localStore.notificationSettings.value[group].overrideSound ? 'Enabled' : 'Disabled'}`)}
                        />
                        <SettingsItem
                            type="row"
                            onPress={localStore.pickGroupRingtone}
                            label={t('settings:notification:sound:title')}
                            disabled={!localStore.notificationSettings.value[group].sound || !localStore.notificationSettings.value[group].overrideSound}
                        />
                    </>
                ))}
        </>
    );
});
NotificationGroup.displayName = 'NotificationGroup';

export const Notifications = observer<NavigatableSettingsScreenProps>(props => {
    useInnerNavigator('Settings', props);
    const localStore = useLocalStoreWithInjectedDependencies(NotificationsLocalStore);

    if (localStore.notificationSettings.state !== 'fulfilled') {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollContentStyle={styles.content} scrollType="scroll">
            {localStore.groupsToRender.map(group => (
                <NotificationGroup key={group} group={group} localStore={localStore} />
            ))}
        </BasePageView>
    );
});
Notifications.displayName = 'Settings.Notifications';

const styles = StyleSheet.create({
    content: {
        rowGap: 0,
        flex: undefined,
        paddingHorizontal: 0,
    },
    pageTitle: {
        fontSize: 16,
        marginVertical: 8,
        color: colors.ocean,
        paddingHorizontal: 15,
    },
});
