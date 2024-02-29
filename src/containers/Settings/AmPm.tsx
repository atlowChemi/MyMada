import i18n from '../../localization';
import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, NativeModules } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { PageLoader } from '../../Components/Loader';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { SettingsItem } from '../../Components/SettingsItem';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { RadioSelectDialog, RadioSelectDialogProps } from '../dialogs/RadioSelectDialog';
import { SharedPreferencesStore, PREF_KEYS } from '../../services/SharedPreferencesStore';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';
import { DeepValueOf } from '../../utils';

const dayNotificationLevelKey = PREF_KEYS.callDetection.dayAndNightSettings.dayNotificationLevel;
const nightNotificationLevelKey = PREF_KEYS.callDetection.dayAndNightSettings.nightNotificationLevel;
enum NOTIFICATION_LEVEL_VALUES {
    sound = 0,
    vibrate = 1,
    silent = 2,
}

@injectable()
class AmPmLocalStore {
    private isPickerOpen = false;
    public amPmSettings = fromPromise(this.getAmPmSettings());
    constructor(private readonly dialogStore: DialogStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, { notificationLevels: false }, { autoBind: true });
    }

    public get notificationLevels() {
        return [i18n.t('settings:ampm:notificationLevel:soundVibrate'), i18n.t('settings:ampm:notificationLevel:vibrate'), i18n.t('settings:ampm:notificationLevel:silent')] as string[];
    }
    public async selectNightStart() {
        const response = await this.openTimePicker(PREF_KEYS.callDetection.dayAndNightSettings.nightStart);
        if (response === null) {
            return;
        }
        await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection.dayAndNightSettings.nightStart, response.toString());
    }

    public async selectNightEnd() {
        const response = await this.openTimePicker(PREF_KEYS.callDetection.dayAndNightSettings.nightEnd);
        if (response === null) {
            return;
        }
        await this.sharedPreferencesStore.setItem(PREF_KEYS.callDetection.dayAndNightSettings.nightEnd, response.toString());
    }

    public async selectDayNotificationLevel() {
        await this.openNotificationLevelPicker(dayNotificationLevelKey);
    }

    public async selectNightNotificationLevel() {
        await this.openNotificationLevelPicker(nightNotificationLevelKey);
    }

    private async getAmPmSettings() {
        const {
            callDetection: {
                dayAndNightSettings: { nightStart, nightEnd, dayNotificationLevel, nightNotificationLevel },
            },
        } = await this.sharedPreferencesStore.applicationSettings;
        return { nightStart, nightEnd, [dayNotificationLevelKey]: dayNotificationLevel, [nightNotificationLevelKey]: nightNotificationLevel };
    }

    private async openTimePicker(
        time: Extract<DeepValueOf<typeof PREF_KEYS>, typeof PREF_KEYS.callDetection.dayAndNightSettings.nightStart | typeof PREF_KEYS.callDetection.dayAndNightSettings.nightEnd>,
    ): Promise<number | null> {
        if (this.isPickerOpen) {
            return null;
        }
        this.isPickerOpen = true;
        const title = i18n.t(`settings:ampm:${time}`);
        const settings = await this.amPmSettings;
        const value = await NativeModules.TimePickerModule.openHourPicker(settings[time], title, colors.colorPrimaryDark);
        this.isPickerOpen = false;
        return value;
    }

    private async openNotificationLevelPicker(time: Extract<DeepValueOf<typeof PREF_KEYS>, typeof dayNotificationLevelKey | typeof nightNotificationLevelKey>) {
        const settings = await this.amPmSettings;
        const selectedValue = await this.dialogStore.openDialog<RadioSelectDialogProps<NOTIFICATION_LEVEL_VALUES>>(RadioSelectDialog, {
            title: i18n.t(`settings:ampm:${time === dayNotificationLevelKey ? 'day' : 'night'}NotificationLevel`),
            options: Object.fromEntries(this.notificationLevels.map((val, index) => [val, index] as const)),
            initialValue: settings[time],
        });
        if (typeof selectedValue !== 'number') {
            return;
        }
        // TODO: remove `overallNotificationLevel`? seems to be redundant (should be computed when used, not stored in shared prefs)
        const newTimes = {
            dayNotificationLevel: time === dayNotificationLevelKey ? selectedValue : settings[dayNotificationLevelKey],
            nightNotificationLevel: time === nightNotificationLevelKey ? selectedValue : settings[nightNotificationLevelKey],
        };
        const overallNotificationLevel = newTimes.dayNotificationLevel * 3 + newTimes.nightNotificationLevel;
        await this.sharedPreferencesStore.setItems({
            [time]: selectedValue.toString(),
            [PREF_KEYS.callDetection.dayAndNightSettings.overallNotificationLevel]: overallNotificationLevel.toString(),
        });
    }
}

export const AmPm = observer<NavigatableSettingsScreenProps>(props => {
    const { t } = useTranslation();
    useInnerNavigator('Settings', props);
    const localStore = useLocalStoreWithInjectedDependencies(AmPmLocalStore);

    if (localStore.amPmSettings.state !== 'fulfilled') {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollContentStyle={styles.content}>
            <Text style={styles.pageTitle}>{t('settings:ampm:menuName')}</Text>
            <SettingsItem type="row" onPress={localStore.selectNightStart} label={t('settings:ampm:nightStart')} subLabel={t('settings:ampm:nightStartDesc')} />
            <SettingsItem type="row" onPress={localStore.selectNightEnd} label={t('settings:ampm:nightEnd')} subLabel={t('settings:ampm:nightEndDesc')} />
            <SettingsItem
                type="row"
                onPress={localStore.selectDayNotificationLevel}
                label={t('settings:ampm:dayNotificationLevel')}
                subLabel={localStore.notificationLevels[localStore.amPmSettings.value[dayNotificationLevelKey]]}
            />
            <SettingsItem
                type="row"
                onPress={localStore.selectNightNotificationLevel}
                label={t('settings:ampm:nightNotificationLevel')}
                subLabel={localStore.notificationLevels[localStore.amPmSettings.value[nightNotificationLevelKey]]}
            />
        </BasePageView>
    );
});
AmPm.displayName = 'Settings.AmPm';

const styles = StyleSheet.create({
    content: {
        rowGap: 0,
        paddingHorizontal: 0,
    },
    pageTitle: {
        fontSize: 16,
        marginBottom: 8,
        color: colors.ocean,
        paddingHorizontal: 15,
    },
    rowWrapper: {
        borderBottomWidth: 1,
        borderBottomColor: colors.colorBlueAlpha,
    },
    row: {
        alignItems: 'flex-start',
        justifyContent: 'space-between',
        padding: 15,
    },
    rowTitle: {
        fontSize: 21,
    },
    rowDesc: {
        fontSize: 16,
    },
});
