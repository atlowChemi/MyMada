import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { RoundBG } from '../RoundBG';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { useDependencyInjector } from '../../services/container';
import { NavigationStore } from '../../services/NavigationStore';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import type { NavigatableScreenProps } from '../../App';

const ButtonsFooter = observer(() => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);

    return (
        <View style={styles.footer}>
            <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.btn} wrapperStyle={styles.btnWrapper}>
                <Text style={styles.btnText}>{t('lock:download')}</Text>
            </PressableRippleWrapper>
            <View style={styles.btnSplitView}>
                <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.btn} wrapperStyle={styles.btnWrapper} onPress={navigationStore.goToRequestCode}>
                    <Text style={styles.btnText}>{t('lock:requestActivationCode')}</Text>
                </PressableRippleWrapper>
                <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.btn} wrapperStyle={styles.btnWrapper}>
                    <Text style={styles.btnText}>{t('lock:enterActivationCode')}</Text>
                </PressableRippleWrapper>
            </View>
        </View>
    );
});

export const LockPage = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();

    return (
        <BasePageView scrollViewStyle={styles.scroller} scrollContentStyle={styles.scrollContent} preScroll={<RoundBG />} postScroll={<ButtonsFooter />}>
            <View style={styles.contentWrapper}>
                <Text style={styles.explanation}>{t('lock:explanationMsg')}</Text>
                <Text style={styles.contactMsg}>{t('lock:contactMsg')}</Text>
            </View>
        </BasePageView>
    );
});
LockPage.displayName = 'LockPage';

const styles = StyleSheet.create({
    scroller: {
        position: 'absolute',
        top: 20,
        right: 0,
        left: 0,
        bottom: 166,
    },
    scrollContent: {
        paddingTop: undefined,
        rowGap: 10,
    },
    contentWrapper: {
        gap: 15,
        padding: 20,
        borderRadius: 40,
        marginHorizontal: 16,
        backgroundColor: colors.white,
    },
    explanation: {
        fontSize: 21,
        textAlign: 'center',
        color: colors.colorRedDark,
    },
    contactMsg: {
        fontSize: 25,
        fontWeight: 'bold',
        textAlign: 'center',
        color: colors.black,
    },
    footer: {
        gap: 16,
        left: 10,
        right: 10,
        bottom: 0,
        marginBottom: 20,
        position: 'absolute',
        justifyContent: 'flex-end',
    },
    btnWrapper: {
        flex: 1,
        elevation: 4,
        borderRadius: 15,
    },
    btn: {
        height: 60,
        overflow: 'hidden',
        backgroundColor: colors.colorNice,
    },
    btnText: {
        fontSize: 20,
        lineHeight: 60,
        textAlign: 'center',
        color: colors.white,
    },
    btnSplitView: {
        flexDirection: 'row',
        gap: 10,
    },
});
