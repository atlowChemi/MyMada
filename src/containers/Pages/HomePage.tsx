import { useTranslation } from 'react-i18next';
import { StyleSheet, View, ToastAndroid } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { BannerAd, BannerAdSize } from 'react-native-google-mobile-ads';
import crashlytics from '@react-native-firebase/crashlytics';
import { colors } from '../../consts';
import { HomeBtn } from '../HomeBtn';
import { RoundBG } from '../RoundBG';
import { BaseDialog } from '../dialogs/BaseDialog';
import { BasePageView } from '../../Components/BasePageView';
import { AdsStore } from '../../services/AdsStore';
import { DialogStore } from '../../services/DialogStore';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { MadaNotificationsStore, CallType } from '../../services/MadaNotificationsStore';
import type { NavigatableScreenProps } from '../../App';

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
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);

    const localStore = useLocalObservable(() => ({
        async getNotificationPreferences() {
            sharedPreferencesStore.name = 'Settings';
            const { callT1, callT3, cut, Mapp, not, win } = await sharedPreferencesStore.activeChannel;
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
    }));

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
