import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StatusBar, StyleSheet, View } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { DependencyInjectionProvider } from '../../services/container';
import type { FC } from 'react';
import { Icon } from '../../Components/Icon';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';

const MadaCallPageWithDI = observer(() => {
    const { t } = useTranslation();

    return (
        <>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <View style={styles.page}>
                <Text style={styles.header}>{t('callDetection:newCallTitle')}</Text>
                <Text style={styles.msgType}>MSG_TYPE</Text>
                <Text style={styles.address} numberOfLines={3} ellipsizeMode="clip">
                    ADDRESS
                </Text>
                <Text style={styles.medicalCode}>MEDICAL_CODE</Text>
                <Text style={styles.msgContent}>MSG_CONTENT</Text>
                <Icon name="mada" size={60} />
                <Text style={styles.appName}>{t('common:appName')}</Text>
                <View style={styles.buttonsRow}>
                    <PressableRippleWrapper android_ripple={{ color: colors.white, borderless: true }} wrapperStyle={styles.button} onPress={() => {}}>
                        <Text>{t('callDetection:closeBtn')}</Text>
                    </PressableRippleWrapper>
                    <PressableRippleWrapper android_ripple={{ color: colors.white, borderless: true }} wrapperStyle={styles.button} onPress={() => {}}>
                        <Text>{t('callDetection:closeBtn')}</Text>
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
        backgroundColor: colors.colorPrimary,
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
        paddingVertical: 15,
        color: colors.colorAqua,
    },
    address: {
        fontSize: 35,
        flexWrap: 'wrap',
        fontWeight: 'bold',
        color: colors.black,
        paddingVertical: 15,
    },
    medicalCode: {
        marginTop: 5,
        fontSize: 27,
    },
    msgContent: {
        flex: 1,
        fontSize: 22,
        marginBottom: 20,
    },
    logo: {},
    appName: {
        fontSize: 18,
        marginBottom: 5,
        color: colors.materialGrey600,
    },
    buttonsRow: {
        gap: 4,
        marginEnd: 8,
    },
    button: {
        flex: 1,
        margin: 4,
        height: 50,
        fontSize: 20,
        borderRadius: 5,
        textAlign: 'center',
        textAlignVertical: 'center',
        backgroundColor: colors.colorNice,
    },
});
