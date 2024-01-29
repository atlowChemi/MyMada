import { useTranslation } from 'react-i18next';
import { LinearGradient } from 'react-native-linear-gradient';
import { StyleSheet, View, ToastAndroid } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { HomeBtn } from '../HomeBtn';
import { BaseDialog } from '../dialogs/BaseDialog';
import { BasePageView } from '../../Components/BasePageView';
import { DialogStore } from '../../services/DialogStore';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { MadaNotificationsStore, CallType } from '../../services/MadaNotificationsStore';
import type { NavigatableScreenProps } from '../../App';

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
            } catch {
                // Crashlytics
            }
        },
    }));

    return (
        <BasePageView
            scrollViewStyle={styles.scroller}
            scrollContentStyle={styles.scrollContent}
            preScroll={<LinearGradient colors={['#1f425f', colors.colorPrimary, colors.colorPrimary]} angle={0} useAngle style={styles.roundBg} />}>
            <View>
                <HomeBtn big img={require('../../assets/homeBtns/new_tol.png')} onPress={navigationStore.goToTools} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_mok.png')} onPress={() => {}} />
                <HomeBtn img={require('../../assets/homeBtns/new_tes.png')} onPress={localStore.onRequestTest} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_con.png')} onPress={navigationStore.goToContactUs} />
                <HomeBtn img={require('../../assets/homeBtns/new_inf.png')} onPress={navigationStore.goToAboutTheApp} />
            </View>
        </BasePageView>
    );
});
HomePage.displayName = 'HomePage';

const styles = StyleSheet.create({
    scroller: {
        position: 'absolute',
        top: 20,
        bottom: 0,
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
    roundBg: {
        position: 'relative',
        top: 0,
        bottom: 0,
        right: '-15%',
        left: '-15%',
        width: '130%',
        height: 100,
        borderTopStartRadius: 0,
        borderTopEndRadius: 0,
        borderBottomRightRadius: 130,
        borderBottomLeftRadius: 130,
    },
});
