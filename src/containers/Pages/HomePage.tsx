import { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View, ToastAndroid, Platform, Linking } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { BannerAd, BannerAdSize } from 'react-native-google-mobile-ads';
import BuildConfig from 'react-native-build-config';
import crashlytics from '@react-native-firebase/crashlytics';
import { HomeBtn } from '../HomeBtn';
import { RoundBG } from '../RoundBG';
import { BaseDialog } from '../dialogs/BaseDialog';
import { AdsStore } from '../../services/AdsStore';
import { colors, ANDROID_VERSION } from '../../consts';
import { DialogStore } from '../../services/DialogStore';
import { RateUsStore } from '../../services/RateUsStore';
import { BasePageView } from '../../Components/BasePageView';
import { useDependencyInjector } from '../../services/container';
import { NavigationStore, Page } from '../../services/NavigationStore';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { MadaNotificationsStore, CallType } from '../../services/MadaNotificationsStore';
import { Importance, NOTIFICATION_MAP, NotificationService } from '../../services/NotificationService';
import type { NavigatableScreenProps } from '../../App';
import type { TranslationKeys } from '../../localization';

const BannerAdFooter = observer(() => {
    const adsStore = useDependencyInjector(AdsStore);

    if (adsStore.initialization.state !== 'fulfilled') {
        return null;
    }
    return (
        <View style={styles.banner}>
            <BannerAd unitId={adsStore.bannerAdUnitId} onAdFailedToLoad={console.error} size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER} />
        </View>
    );
});
BannerAdFooter.displayName = 'BannerAdFooter';

export const HomePage = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const dialogStore = useDependencyInjector(DialogStore);
    const madaNotificationsStore = useDependencyInjector(MadaNotificationsStore);
    const navigationStore = useDependencyInjector(NavigationStore);
    const notificationService = useDependencyInjector(NotificationService);
    const rateUsStore = useDependencyInjector(RateUsStore);
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);

    const localStore = useLocalObservable(() => ({
        async getNotificationPreferences() {
            const { callT1, callT3, cut, Mapp, not, win } = await sharedPreferencesStore.settingsChannel;
            return {
                callRecognition: sharedPreferencesStore.makePrefBoolean(Mapp, false),
                notifyOnCall: sharedPreferencesStore.makePrefBoolean(not, true),
                windowOnCall: sharedPreferencesStore.makePrefBoolean(win, true),
                detectShiftCalls: sharedPreferencesStore.makePrefBoolean(callT3, true),
                detectOnCallCalls: sharedPreferencesStore.makePrefBoolean(callT1, true),
                splitNotificationByCallType: sharedPreferencesStore.makePrefBoolean(cut, false),
            };
        },
        async onRequestTest() {
            const isTestingShiftCall = await dialogStore.openDialog(BaseDialog, {
                description: t('testCall:msg'),
                okText: t('testCall:shift'),
                cancelText: t('testCall:onCall'),
                negativeBtnColor: colors.oldPrimary,
            });
            if (typeof isTestingShiftCall === 'undefined') {
                return;
            }
            const pref = await this.getNotificationPreferences();
            if (!pref.callRecognition || (!pref.detectShiftCalls && !pref.detectOnCallCalls)) {
                ToastAndroid.show(t('testCall:recognitionOff'), ToastAndroid.SHORT);
                return;
            }
            const relevantDetectionSetting = isTestingShiftCall ? pref.detectShiftCalls : pref.detectOnCallCalls;
            if (pref.splitNotificationByCallType && !relevantDetectionSetting) {
                ToastAndroid.show(t(`testCall:${isTestingShiftCall ? 'shift' : 'onCall'}RecognitionOff`), ToastAndroid.SHORT);
                return;
            }
            try {
                await madaNotificationsStore.triggerTestCall({
                    callType: isTestingShiftCall ? CallType.Shift : CallType.OnCall,
                    callText: t(`testCall:${isTestingShiftCall ? 'shift' : 'onCall'}Message`),
                });
            } catch (e) {
                crashlytics().log('Failed to send test call');
                e instanceof Error && crashlytics().recordError(e);
            }
        },
        async showRelevantDialogs() {
            const { privacy, oldU, Mapp } = await sharedPreferencesStore.settingsChannel;
            const hasVisitedPrivacy = sharedPreferencesStore.makePrefBoolean(privacy, false);
            const hasVisitedSettings = sharedPreferencesStore.makePrefBoolean(oldU, false);
            const isCallRecognitionOn = sharedPreferencesStore.makePrefBoolean(Mapp, false);
            if (dialogStore.hasDialogs) {
                return;
            }
            if (!hasVisitedPrivacy) {
                return this.showDialog({ description: 'home:privacyMsg', okText: 'privacy:toolbarTitle', page: 'Privacy' });
            }
            if (!hasVisitedSettings) {
                return this.showDialog({ description: 'home:suggestVisitingSettings', okText: 'common:goToSettings', page: 'Settings' });
            }
            if (isCallRecognitionOn && Platform.OS === 'android' && Platform.Version >= ANDROID_VERSION.OREO) {
                const importance = await notificationService.getChannelImportance(NOTIFICATION_MAP.CHANNELS.BACKGROUND);
                if (importance <= Importance.MIN) {
                    return;
                }
                const shouldOpenSettings = await dialogStore.openDialog(BaseDialog, {
                    blockBackBtn: true,
                    blockBackdropPress: true,
                    okText: t('home:lowerImportance'),
                    description: t('home:importanceMsg'),
                    cancelText: t('common:notNow'),
                    negativeBtnColor: colors.oldPrimary,
                });
                if (!shouldOpenSettings) {
                    return;
                }
                await Linking.sendIntent('android.settings.CHANNEL_NOTIFICATION_SETTINGS', [
                    { key: 'android.provider.extra.APP_PACKAGE', value: BuildConfig.APPLICATION_ID },
                    { key: 'android.provider.extra.CHANNEL_ID', value: NOTIFICATION_MAP.CHANNELS.BACKGROUND },
                ]);
            }
        },
        async showDialog({ description, okText, title, page }: { description: TranslationKeys; title?: TranslationKeys; okText: TranslationKeys; page: Page }) {
            const result = await dialogStore.openDialog(BaseDialog, {
                okText: t(okText),
                blockBackBtn: true,
                blockBackdropPress: true,
                description: t(description),
                cancelText: t('common:notNow'),
                ...(title && { title: t(title) }),
                negativeBtnColor: colors.oldPrimary,
            });
            if (result) {
                navigationStore.navigate(page);
            }
        },
    }));

    useEffect(() => {
        rateUsStore.appLaunched();
    }, [rateUsStore]);
    useEffect(() => {
        localStore.showRelevantDialogs();
    }, [localStore]);

    return (
        <BasePageView scrollViewStyle={styles.scroller} scrollContentStyle={styles.scrollContent} postScroll={<BannerAdFooter />} preScroll={<RoundBG />}>
            <View>
                <HomeBtn big icon="tools" onPress={navigationStore.goToTools} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn icon="dispatch" onPress={navigationStore.goToDispatch} />
                <HomeBtn icon="testCall" onPress={localStore.onRequestTest} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn icon="contactUs" onPress={navigationStore.goToContactUs} />
                <HomeBtn icon="info" onPress={navigationStore.goToAboutTheApp} />
            </View>
        </BasePageView>
    );
});
HomePage.displayName = 'HomePage';

const styles = StyleSheet.create({
    scroller: {
        position: 'absolute',
        top: 20,
        bottom: 80,
        right: 0,
        left: 0,
    },
    scrollContent: {
        paddingTop: undefined,
        rowGap: 10,
    },
    buttonRow: {
        flexDirection: 'row',
        columnGap: 10,
    },
    banner: {
        position: 'absolute',
        left: 0,
        right: 0,
        bottom: 0,
        height: 80,
        justifyContent: 'flex-end',
    },
});
