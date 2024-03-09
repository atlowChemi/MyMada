import Color from 'color';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StatusBar, StyleSheet, View, BackHandler } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon } from '../../Components/Icon';
import { DependencyInjectionProvider } from '../../services/container';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import type { FC } from 'react';

const rippleColor = Color(colors.colorNice).lighten(0.7).alpha(0.6).string();

const MadaCallPageWithDI = observer(() => {
    const { t } = useTranslation();

    return (
        <>
            <StatusBar barStyle="light-content" backgroundColor={colors.oldPrimary} />
            <View style={styles.page}>
                <Text style={styles.header}>{t('callDetection:newCallTitle')}</Text>
                <Text style={styles.msgType}>MSG_TYPE</Text>
                <Text style={styles.address} numberOfLines={3} ellipsizeMode="clip">
                    ADDRESS
                </Text>
                <Text style={styles.msgContent}>MSG_CONTENT</Text>
                <Text style={styles.msgTime}>MSG_TIME</Text>
                <Icon name="mada" size={60} color={colors.oldPrimary} style={styles.logo} />
                <Text style={styles.appName}>{t('common:appName')}</Text>
                <View style={styles.buttonsRow}>
                    <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: true }} wrapperStyle={styles.buttonRipple} onPress={BackHandler.exitApp}>
                        <Text style={styles.buttonTxt}>{t('callDetection:closeBtn')}</Text>
                    </PressableRippleWrapper>
                    <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: true }} wrapperStyle={styles.buttonRipple} onPress={() => {}}>
                        <Text style={styles.buttonTxt}>{t('callDetection:callInfo')}</Text>
                    </PressableRippleWrapper>
                </View>
            </View>
        </>
    );
});
MadaCallPageWithDI.displayName = 'MadaCallPageWithDI';

export const MadaCallPage: FC = (props: any) => (
    <DependencyInjectionProvider>
        <MadaCallPageWithDI {...props} />
    </DependencyInjectionProvider>
);
MadaCallPage.displayName = 'MadaCallPage';

const styles = StyleSheet.create({
    page: {
        gap: 15,
        flex: 1,
    },
    header: {
        fontSize: 30,
        fontWeight: 'bold',
        color: colors.white,
        paddingVertical: 15,
        textAlign: 'center',
        textAlignVertical: 'center',
        backgroundColor: colors.oldPrimary,
    },
    msgType: {
        fontSize: 19,
        textAlign: 'center',
        paddingVertical: 15,
        color: colors.colorAqua,
    },
    address: {
        fontSize: 35,
        flexWrap: 'wrap',
        fontWeight: 'bold',
        textAlign: 'center',
        color: colors.black,
        paddingVertical: 0,
    },
    msgContent: {
        flex: 1,
        fontSize: 27,
        marginTop: 5,
        textAlign: 'center',
        textAlignVertical: 'center',
    },
    msgTime: {
        fontSize: 22,
        marginBottom: 20,
        textAlign: 'center',
    },
    logo: {
        alignSelf: 'center',
    },
    appName: {
        fontSize: 18,
        marginBottom: 5,
        textAlign: 'center',
        color: colors.materialGrey600,
    },
    buttonsRow: {
        gap: 4,
        marginBottom: 14,
        height: 50,
        flexDirection: 'row',
    },
    buttonRipple: {
        flex: 1,
        margin: 4,
        height: 50,
        borderRadius: 5,
        backgroundColor: colors.colorNice,
    },
    buttonTxt: {
        fontSize: 20,
        height: '100%',
        textAlign: 'center',
        color: colors.white,
        textAlignVertical: 'center',
    },
});
