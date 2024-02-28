import { injectable } from 'inversify';
import { StyleSheet } from 'react-native';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { fromPromise, computedFn } from 'mobx-utils';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { PageLoader } from '../../Components/Loader';
import { SettingsItem } from '../../Components/SettingsItem';
import { BasePageView } from '../../Components/BasePageView';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SettingsCardLink } from '../../Components/SettingsCardLink';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { ValueOf } from '../../utils';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';

const PREF_KEYS = {
    splitNotificationByCallType: 'cut',
    notifySoundByDeviceStatus: 'stat',
    notifyVibrationByDeviceStatus: 'statVib',
    detectServiceCalls: 'callT6',
    detectAdministrativeCalls: 'callT4',
} as const;

@injectable()
class DetectionLocalStore {
    private navigation?: NavigatableSettingsScreenProps['navigation'];
    public detectionSettings = fromPromise(this.getDetectionSettings());

    constructor(private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, undefined, { autoBind: true });
    }

    public get updateValue() {
        return computedFn((key: ValueOf<typeof PREF_KEYS>) => async (value: boolean) => {
            await this.sharedPreferencesStore.setItem(key, value.toString());
        });
    }

    private async getDetectionSettings() {
        const {
            callDetection: { splitNotificationByCallType, notifySoundByDeviceStatus, notifyVibrationByDeviceStatus, detectServiceCalls, detectAdministrativeCalls },
        } = await this.sharedPreferencesStore.applicationSettings;
        return { splitNotificationByCallType, notifySoundByDeviceStatus, notifyVibrationByDeviceStatus, detectServiceCalls, detectAdministrativeCalls };
    }

    public init(props: NavigatableSettingsScreenProps) {
        this.navigation = props.navigation;
    }

    public navigateToNotifications() {
        this.navigation?.navigate('Notification');
    }
}

export const Detection = observer<NavigatableSettingsScreenProps>(props => {
    useInnerNavigator('Settings', props);
    const { t } = useTranslation();
    const localStore = useLocalStoreWithInjectedDependencies(DetectionLocalStore, props);

    if (localStore.detectionSettings.state !== 'fulfilled') {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollContentStyle={styles.content}>
            <SettingsItem
                type="switch"
                label={t('settings:detection:splitNotificationByCallType:title')}
                value={localStore.detectionSettings.value.splitNotificationByCallType}
                onChange={localStore.updateValue(PREF_KEYS.splitNotificationByCallType)}
                subLabel={t(`settings:detection:splitNotificationByCallType:subTitle${localStore.detectionSettings.value.splitNotificationByCallType ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:detection:notifySoundByDeviceStatus:title')}
                value={localStore.detectionSettings.value.notifySoundByDeviceStatus}
                onChange={localStore.updateValue(PREF_KEYS.notifySoundByDeviceStatus)}
                subLabel={t(`settings:detection:notifySoundByDeviceStatus:subTitle${localStore.detectionSettings.value.notifySoundByDeviceStatus ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:detection:notifyVibrationByDeviceStatus:title')}
                value={localStore.detectionSettings.value.notifyVibrationByDeviceStatus}
                onChange={localStore.updateValue(PREF_KEYS.notifyVibrationByDeviceStatus)}
                subLabel={t(`settings:detection:notifyVibrationByDeviceStatus:subTitle${localStore.detectionSettings.value.notifyVibrationByDeviceStatus ? 'Enabled' : 'Disabled'}`)}
            />
            <Text style={styles.pageTitle}>{t('settings:notification:administrative:title')}</Text>
            <SettingsItem
                type="switch"
                label={t('settings:detection:detectServiceCalls:title')}
                value={localStore.detectionSettings.value.detectServiceCalls}
                onChange={localStore.updateValue(PREF_KEYS.detectServiceCalls)}
                subLabel={t(`settings:detection:detectServiceCalls:subTitle${localStore.detectionSettings.value.detectServiceCalls ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:detection:detectAdministrativeCalls:title')}
                value={localStore.detectionSettings.value.detectAdministrativeCalls}
                onChange={localStore.updateValue(PREF_KEYS.detectAdministrativeCalls)}
                subLabel={t(`settings:detection:detectAdministrativeCalls:subTitle${localStore.detectionSettings.value.detectAdministrativeCalls ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsCardLink label={'settings:notification:menuName'} onPress={localStore.navigateToNotifications} />
        </BasePageView>
    );
});
Detection.displayName = 'Settings.Detection';

const styles = StyleSheet.create({
    content: {
        rowGap: 0,
        paddingTop: 0,
        paddingHorizontal: 0,
    },
    pageTitle: {
        fontSize: 16,
        marginVertical: 10,
        color: colors.ocean,
        paddingHorizontal: 15,
    },
});
