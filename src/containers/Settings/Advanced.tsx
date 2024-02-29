import { injectable } from 'inversify';
import { StyleSheet } from 'react-native';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { fromPromise, computedFn } from 'mobx-utils';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { PageLoader } from '../../Components/Loader';
import { SettingsItem } from '../../Components/SettingsItem';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { TextInputDialog } from '../dialogs/TextInputDialog';
import { audioGenerator } from '../../services/AudioGenerator';
import { AlertVolumeDialog } from '../dialogs/AlertVolumeDialog';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SettingsCardLink } from '../../Components/SettingsCardLink';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { SharedPreferencesStore, PREF_KEYS } from '../../services/SharedPreferencesStore';
import type { DeepValueOf } from '../../utils';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';

@injectable()
class AdvancedLocalStore {
    private navigation?: NavigatableSettingsScreenProps['navigation'];
    public advancedSettings = fromPromise(this.getAdvancedSettings());

    constructor(private readonly dialogStore: DialogStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, undefined, { autoBind: true });
    }

    public get updateValue() {
        return computedFn((key: DeepValueOf<typeof PREF_KEYS>) => async (value: boolean) => await this.sharedPreferencesStore.setItem(key, value.toString()));
    }

    private async getAdvancedSettings() {
        const [
            {
                callDetection: {
                    sendNotificationOnCall,
                    openPopUpOnCall,
                    advancedSettings: { overridePreviousCalls, infiniteAlert, autoStopAlert, autoStopAlertDuration, overrideVolume, volume },
                },
            },
            maxVolume,
        ] = await Promise.all([this.sharedPreferencesStore.applicationSettings, audioGenerator.getMaxVolumeForAlarmStream()]);
        return {
            sendNotificationOnCall,
            openPopUpOnCall,
            overridePreviousCalls,
            infiniteAlert,
            autoStopAlert,
            autoStopAlertDuration,
            overrideVolume,
            volume: volume || maxVolume,
            maxVolume,
        };
    }

    public init(props: NavigatableSettingsScreenProps) {
        this.navigation = props.navigation;
    }

    public navigateToAmPm() {
        this.navigation?.navigate('AmPm');
    }

    public async openAutoStopAlertDurationDialog() {
        const { autoStopAlertDuration } = await this.advancedSettings;
        const result = await this.dialogStore.openDialog(TextInputDialog, {
            placeholder: '',
            autoComplete: 'off',
            keyboardType: 'number-pad',
            initialValue: autoStopAlertDuration || '6',
            title: i18n.t('settings:advanced:autoStopAlert:duration'),
            isValid(value) {
                if (!value.trim().length) {
                    return i18n.t('settings:advanced:autoStopAlert:durationEmpty');
                }
                const intValue = Number.parseFloat(value);
                if (Number.isNaN(intValue) || intValue % 1 !== 0 || intValue < 1) {
                    return i18n.t('settings:advanced:autoStopAlert:durationInvalid');
                }
                return undefined;
            },
        });
        if (!result) {
            return;
        }
        await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection.advancedSettings.autoStopAlertDuration, Number.parseInt(result, 10).toString());
    }

    public async openAlertVolumeDialog() {
        const { overrideVolume, volume, maxVolume } = await this.advancedSettings;
        const result = await this.dialogStore.openDialog(AlertVolumeDialog, { initialVolume: Math.min(volume, maxVolume), initialOverrideVolume: overrideVolume, maxVolume });
        if (!result) {
            return;
        }
        await this.sharedPreferencesStore.setItems({
            [PREF_KEYS.callDetection.advancedSettings.overrideVolume]: result.overrideVolume.toString(),
            [PREF_KEYS.callDetection.advancedSettings.volume]: result.volume.toString(),
        });
    }
}

export const Advanced = observer<NavigatableSettingsScreenProps>(props => {
    useInnerNavigator('Settings', props);
    const { t } = useTranslation();
    const localStore = useLocalStoreWithInjectedDependencies(AdvancedLocalStore, props);

    if (localStore.advancedSettings.state !== 'fulfilled') {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollContentStyle={styles.content}>
            <Text style={styles.pageTitle}>{t('settings:advanced:notificationTitle')}</Text>
            <SettingsItem
                type="switch"
                label={t('settings:advanced:sendNotificationOnCall:title')}
                value={localStore.advancedSettings.value.sendNotificationOnCall}
                onChange={localStore.updateValue(PREF_KEYS.callDetection.sendNotificationOnCall)}
                subLabel={t(`settings:advanced:sendNotificationOnCall:subTitle${localStore.advancedSettings.value.sendNotificationOnCall ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:advanced:openPopUpOnCall:title')}
                value={localStore.advancedSettings.value.openPopUpOnCall}
                onChange={localStore.updateValue(PREF_KEYS.callDetection.openPopUpOnCall)}
                subLabel={t(`settings:advanced:openPopUpOnCall:subTitle${localStore.advancedSettings.value.openPopUpOnCall ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:advanced:overridePreviousCalls:title')}
                value={localStore.advancedSettings.value.overridePreviousCalls}
                onChange={localStore.updateValue(PREF_KEYS.callDetection.advancedSettings.overridePreviousCalls)}
                subLabel={t(`settings:advanced:overridePreviousCalls:subTitle${localStore.advancedSettings.value.overridePreviousCalls ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:advanced:infiniteAlert:title')}
                value={localStore.advancedSettings.value.infiniteAlert}
                onChange={localStore.updateValue(PREF_KEYS.callDetection.advancedSettings.infiniteAlert)}
                subLabel={t(`settings:advanced:infiniteAlert:subTitle${localStore.advancedSettings.value.infiniteAlert ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="switch"
                label={t('settings:advanced:autoStopAlert:title')}
                value={localStore.advancedSettings.value.autoStopAlert}
                onChange={localStore.updateValue(PREF_KEYS.callDetection.advancedSettings.autoStopAlert)}
                subLabel={t(`settings:advanced:autoStopAlert:subTitle${localStore.advancedSettings.value.autoStopAlert ? 'Enabled' : 'Disabled'}`)}
            />
            <SettingsItem
                type="row"
                onPress={localStore.openAutoStopAlertDurationDialog}
                label={t('settings:advanced:autoStopAlert:duration')}
                disabled={!localStore.advancedSettings.value.autoStopAlert}
            />
            <SettingsItem type="row" onPress={localStore.openAlertVolumeDialog} label={t('settings:advanced:alertVolume:link')} />
            <Text style={styles.pageTitle}>{t('settings:advanced:moreTitle')}</Text>
            <SettingsCardLink label={'settings:ampm:menuName'} onPress={localStore.navigateToAmPm} />
        </BasePageView>
    );
});
Advanced.displayName = 'Settings.Advanced';

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
