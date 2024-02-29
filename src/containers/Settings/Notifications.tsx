import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { injectable } from 'inversify';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { computedFn, fromPromise } from 'mobx-utils';
import { Linking, PermissionsAndroid, StyleSheet } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BaseDialog } from '../dialogs/BaseDialog';
import { PageLoader } from '../../Components/Loader';
import { DialogStore } from '../../services/DialogStore';
import { SettingsItem } from '../../Components/SettingsItem';
import { BasePageView } from '../../Components/BasePageView';
import { ERROR_PICKER_CANCELLED } from '../../utils/errorCodes';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { PermissionsStore } from '../../services/PermissionsStore';
import { MadaNotificationsStore } from '../../services/MadaNotificationsStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { SharedPreferencesStore, PREF_KEYS } from '../../services/SharedPreferencesStore';
import type { DeepValueOf } from '../../utils';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';

type Group = 'grouped' | 'onCall' | 'group' | 'shift' | 'administrative';
type DetectionToggleableGroup = Exclude<Group, 'grouped' | 'administrative'>;
const isToggleableGroup = (group: Group): group is DetectionToggleableGroup => !['grouped', 'administrative'].includes(group);

@injectable()
class NotificationsLocalStore {
    public notificationSettings = fromPromise(this.getNotificationSettings());

    constructor(
        private readonly dialogStore: DialogStore,
        private readonly madaNotificationStore: MadaNotificationsStore,
        private readonly permissionsStore: PermissionsStore,
        private readonly sharedPreferencesStore: SharedPreferencesStore,
    ) {
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
        });
    }

    public get updateSoundOverride() {
        return computedFn((group: Group) => async (value: boolean) => {
            if (!(await this.ensurePermissions())) {
                return;
            }
            await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection[group].overrideSound, value.toString());
        });
    }

    public get pickGroupRingtone() {
        return computedFn((group: Group) => async () => {
            if (!(await this.ensurePermissions())) {
                await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection[group].overrideSound, 'false');
                return;
            }
            try {
                crashlytics().log('Picking ringtone');
                const settings = await this.notificationSettings;
                const defaultRingtone = settings[group].ringtone || (await this.madaNotificationStore.getDefaultRingtone());
                const selected = await this.madaNotificationStore.selectRingtone(i18n.t('settings:notification:overrideSound:pickSound'), defaultRingtone);
                await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection[group].ringtone, selected);
            } catch (e) {
                if (!(e instanceof Error) || (e as any).code === ERROR_PICKER_CANCELLED) {
                    return;
                }
                crashlytics().recordError(e);
            }
        });
    }

    private async getNotificationSettings() {
        const { callDetection } = await this.sharedPreferencesStore.applicationSettings;
        return callDetection;
    }

    private async ensurePermissions() {
        const permissionStatus = await this.permissionsStore.requestPermission('android.permission.READ_MEDIA_AUDIO', {
            title: i18n.t('permissions:alertTitle'),
            message: i18n.t('permissions:memoryDescription'),
            buttonPositive: i18n.t('permissions:approve'),
            buttonNegative: i18n.t('permissions:deny'),
        });
        if (permissionStatus !== PermissionsAndroid.RESULTS.GRANTED) {
            const goToSettings = await this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('permissions:alertTitle'),
                description: i18n.t('permissions:blocked'),
                cancelText: i18n.t('common:cancel'),
                okText: i18n.t('common:goToSettings'),
            });
            if (goToSettings) {
                await Linking.openSettings();
            }
            return false;
        }
        return true;
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
                    onChange={localStore.updateValue(PREF_KEYS.callDetection[group].detectCalls)}
                    value={localStore.notificationSettings.value[group].detectCalls}
                    label={t('settings:notification:detect:title', { callType: t(`settings:notification:${group}:titleCallType`, callType) })}
                    subLabel={t(`settings:notification:detect:subTitle${localStore.notificationSettings.value[group].detectCalls ? 'Enabled' : 'Disabled'}`, { callType })}
                />
            )}
            {(!isToggleableGroup(group) || localStore.notificationSettings.value[group].detectCalls) && (
                <>
                    <SettingsItem
                        type="switch"
                        label={t('settings:notification:vibrate:title')}
                        onChange={localStore.updateValue(PREF_KEYS.callDetection[group].vibrate)}
                        value={localStore.notificationSettings.value[group].vibrate}
                        subLabel={t(`settings:notification:vibrate:subTitle${localStore.notificationSettings.value[group].vibrate ? 'Enabled' : 'Disabled'}`)}
                    />
                    <SettingsItem
                        type="switch"
                        label={t('settings:notification:sound:title')}
                        onChange={localStore.updateValue(PREF_KEYS.callDetection[group].sound)}
                        value={localStore.notificationSettings.value[group].sound}
                        subLabel={t(`settings:notification:sound:subTitle${localStore.notificationSettings.value[group].sound ? 'Enabled' : 'Disabled'}`)}
                    />
                    <SettingsItem
                        type="switch"
                        onChange={localStore.updateSoundOverride(group)}
                        label={t('settings:notification:overrideSound:title')}
                        disabled={!localStore.notificationSettings.value[group].sound}
                        value={localStore.notificationSettings.value[group].overrideSound}
                        subLabel={t(`settings:notification:overrideSound:subTitle${localStore.notificationSettings.value[group].overrideSound ? 'Enabled' : 'Disabled'}`)}
                    />
                    <SettingsItem
                        type="row"
                        onPress={localStore.pickGroupRingtone(group)}
                        label={t('settings:notification:sound:title')}
                        disabled={!localStore.notificationSettings.value[group].sound || !localStore.notificationSettings.value[group].overrideSound}
                    />
                </>
            )}
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
