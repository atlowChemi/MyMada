import Color from 'color';
import { computedFn } from 'mobx-utils';
import { StyleSheet, NativeModules } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Snackbar } from '../../Components/Snackbar';
import { PageLoader } from '../../Components/Loader';
import { TOOLS_PAGES } from '../../services/NavigationStore';
import { BasePageView } from '../../Components/BasePageView';
import { SnackbarStore } from '../../services/SnackbarStore';
import { useDependencyInjector } from '../../services/container';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SharedPreferencesStore, TOOL_SHARED_PREF_MAP } from '../../services/SharedPreferencesStore';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';
import type { Tools as ToolsType } from '../../services/NavigationStore';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';

const rippleColor = Color(colors.white).darken(0.5).string();
export const AmPm = observer<NavigatableSettingsScreenProps>(props => {
    const { t } = useTranslation();
    useInnerNavigator('Settings', props);
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);
    const localStore = useLocalObservable(() => ({
        async selectNightStart() {
            const t = await NativeModules.TimePickerModule.openHourPicker(23, 'hi', colors.colorPrimaryDark);
            console.log(t);
        },
        async selectNightEnd() {
            const t = await NativeModules.TimePickerModule.openHourPicker(23, 'bi', colors.colorPrimaryDark);
            console.log(t);
        },
    }));

    // if (sharedPreferencesStore.selectedTools.state === 'pending') {
    //     return <PageLoader />;
    // }

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
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }}>
                <Text style={styles.rowTitle}>{t('settings:ampm:dayNotificationLevel')}</Text>
                <Text style={styles.rowDesc}>Test</Text>
            </PressableRippleWrapper>
            <PressableRippleWrapper wrapperStyle={styles.rowWrapper} style={styles.row} android_ripple={{ color: rippleColor, borderless: true }}>
                <Text style={styles.rowTitle}>{t('settings:ampm:nightNotificationLevel')}</Text>
                <Text style={styles.rowDesc}>Test</Text>
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
