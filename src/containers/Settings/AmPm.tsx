import Color from 'color';
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
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { RadioSelectDialog, RadioSelectDialogProps } from '../dialogs/RadioSelectDialog';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';

const rippleColor = Color(colors.white).darken(0.5).string();
const PREF_KEYS = {
    nightStart: 'nightStart',
    nightEnd: 'nightEnd',
    dayNotificationLevel: 'sleepDay',
    nightNotificationLevel: 'sleepNight',
    overallNotificationLevel: 'sleep',
} as const;
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
        makeAutoObservable(this, undefined, { autoBind: true });
    }

    public get notificationLevels() {
        return [i18n.t('settings:ampm:notificationLevel:soundVibrate'), i18n.t('settings:ampm:notificationLevel:vibrate'), i18n.t('settings:ampm:notificationLevel:silent')] as string[];
    }
    public async selectNightStart() {
        const response = await this.openTimePicker(PREF_KEYS.nightStart);
        if (response === null) {
            return;
        }
        await this.sharedPreferencesStore.setItem(PREF_KEYS.nightStart, response.toString());
        this.updateAmPmObservable();
    }

    public async selectNightEnd() {
        const response = await this.openTimePicker(PREF_KEYS.nightEnd);
        if (response === null) {
            return;
        }
        await this.sharedPreferencesStore.setItem(PREF_KEYS.nightEnd, response.toString());
        this.updateAmPmObservable();
    }

    public async selectDayNotificationLevel() {
        await this.openNotificationLevelPicker('dayNotificationLevel');
        this.updateAmPmObservable();
    }

    public async selectNightNotificationLevel() {
        await this.openNotificationLevelPicker('nightNotificationLevel');
        this.updateAmPmObservable();
    }

    private async getAmPmSettings() {
        const {
            [PREF_KEYS.nightStart]: nightStart,
            [PREF_KEYS.nightEnd]: nightEnd,
            [PREF_KEYS.dayNotificationLevel]: dayNotificationLevel,
            [PREF_KEYS.nightNotificationLevel]: nightNotificationLevel,
        } = await this.sharedPreferencesStore.settingsChannel;
        return {
            nightStart: Number.parseInt(nightStart, 10) || 23,
            nightEnd: Number.parseInt(nightEnd, 10) || 7,
            dayNotificationLevel: Number.parseInt(dayNotificationLevel, 10) || 0,
            nightNotificationLevel: Number.parseInt(nightNotificationLevel, 10) || 0,
        };
    }

    private async openTimePicker(time: Extract<keyof typeof PREF_KEYS, 'nightStart' | 'nightEnd'>): Promise<number | null> {
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

    private updateAmPmObservable() {
        this.amPmSettings = fromPromise(this.getAmPmSettings(), this.amPmSettings);
    }

    private async openNotificationLevelPicker(time: Extract<keyof typeof PREF_KEYS, 'dayNotificationLevel' | 'nightNotificationLevel'>) {
        const settings = await this.amPmSettings;
        const selectedValue = await this.dialogStore.openDialog<RadioSelectDialogProps<NOTIFICATION_LEVEL_VALUES>>(RadioSelectDialog, {
            title: i18n.t(`settings:ampm:${time}`),
            options: Object.fromEntries(this.notificationLevels.map((val, index) => [val, index] as const)),
            initialValue: settings[time],
        });
        if (typeof selectedValue !== 'number') {
            return;
        }
        // TODO: remove `overallNotificationLevel`? seems to be redundant (should be computed when used, not stored in shared prefs)
        const newTimes = {
            dayNotificationLevel: time === 'dayNotificationLevel' ? selectedValue : settings.dayNotificationLevel,
            nightNotificationLevel: time === 'nightNotificationLevel' ? selectedValue : settings.nightNotificationLevel,
        };
        const overallNotificationLevel = newTimes.dayNotificationLevel * 3 + newTimes.nightNotificationLevel;
        await this.sharedPreferencesStore.setItems({ [PREF_KEYS[time]]: selectedValue.toString(), [PREF_KEYS.overallNotificationLevel]: overallNotificationLevel.toString() });
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
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }} onPress={localStore.selectNightStart}>
                <Text style={styles.rowTitle}>{t('settings:ampm:nightStart')}</Text>
                <Text style={styles.rowDesc}>{t('settings:ampm:nightStartDesc')}</Text>
            </PressableRippleWrapper>
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }} onPress={localStore.selectNightEnd}>
                <Text style={styles.rowTitle}>{t('settings:ampm:nightEnd')}</Text>
                <Text style={styles.rowDesc}>{t('settings:ampm:nightEndDesc')}</Text>
            </PressableRippleWrapper>
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }} onPress={localStore.selectDayNotificationLevel}>
                <Text style={styles.rowTitle}>{t('settings:ampm:dayNotificationLevel')}</Text>
                <Text style={styles.rowDesc}>{localStore.notificationLevels[localStore.amPmSettings.value.dayNotificationLevel]}</Text>
            </PressableRippleWrapper>
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }} onPress={localStore.selectNightNotificationLevel}>
                <Text style={styles.rowTitle}>{t('settings:ampm:nightNotificationLevel')}</Text>
                <Text style={styles.rowDesc}>{localStore.notificationLevels[localStore.amPmSettings.value.nightNotificationLevel]}</Text>
            </PressableRippleWrapper>
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
