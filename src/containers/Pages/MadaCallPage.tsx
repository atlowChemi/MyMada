import Color from 'color';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StatusBar, StyleSheet, View, BackHandler } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon } from '../../Components/Icon';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import { DependencyInjectionProvider, useDependencyInjector } from '../../services/container';
import { CallData, CallType, MadaNotificationsStore } from '../../services/MadaNotificationsStore';
import { useMemo, type FC, useCallback } from 'react';
import { BasePageView } from '../../Components/BasePageView';

const rippleColor = Color(colors.colorNice).lighten(0.7).alpha(0.6).string();
const isAdministrativeCall = (callType: CallType) => [CallType.Administrative, CallType.Service].includes(callType);
const getTextBetweenStrings = (text: string, start: string, end: string) => {
    let result = text.substring(text.indexOf(start) + start.length);
    result = result.substring(0, result.indexOf(end));
    return result;
};

const MadaCallPageWithDI = observer<CallData>(({ callType, uuid, text, title }) => {
    const { t } = useTranslation();
    const madaNotificationsStore = useDependencyInjector(MadaNotificationsStore);
    const msgType = useMemo(() => {
        let callTypeName = 'onCall';
        if (CallType.Group === callType) {
            callTypeName = 'group';
        }
        if ([CallType.Shift, CallType.OldMessageSyntax].includes(callType)) {
            callTypeName = 'team';
        }
        if (CallType.Administrative === callType) {
            callTypeName = 'administrative';
        }
        if (CallType.Service === callType) {
            callTypeName = 'service';
        }
        return t(`callDetection:callTypeName:${callTypeName}`);
    }, [t, callType]);
    const data = useMemo(() => {
        const fallbackTime =
            title.replace(/[*](אירוע חדש|New Event|הודעה מנהלתית חדשה)[*]/, '') || `${new Date().getHours().toString().padStart(2, '0')}:${new Date().getMinutes().toString().padStart(2, '0')}`;
        const indexOfAtTime = text.indexOf('בשעה ');
        if (callType === CallType.OldMessageSyntax) {
            const indexOfPostfix = text.indexOf('//');
            const txtWithoutPostfix = indexOfPostfix === -1 ? text : text.substring(0, indexOfPostfix);
            const time = (indexOfAtTime !== -1 && getTextBetweenStrings(text, 'בשעה ', ' ')) || fallbackTime;
            return {
                addressLines: 5,
                address: txtWithoutPostfix,
                time: t('callDetection:receivedAt', { time }),
            };
        }
        if (['אירוע חדש', 'הודעת קבוצת התראה צוות', 'הודעת צוות'].every(str => !text.includes(str))) {
            const indexOfVisa = text.indexOf('מונה:');
            const time = text.substring(0, indexOfVisa) || getTextBetweenStrings(text, 'בשעה ', ' ') || fallbackTime;
            return {
                addressLines: 5,
                address: text,
                time: t('callDetection:receivedAt', { time }),
            };
        }
        const useHebrew = text.includes('כתובת:');
        const address = useHebrew ? getTextBetweenStrings(text, 'כתובת: ', 'קוד ') : getTextBetweenStrings(text, 'Address: ', 'Dispatch Code ');
        const callCode = useHebrew ? getTextBetweenStrings(text, 'קוד הזנקה: ', 'קוד רפואי') : getTextBetweenStrings(text, 'Dispatch Code: ', 'Medical Code');
        const medicalCode = useHebrew ? text.substring(text.indexOf('קוד רפואי: ') + 11) : text.substring(text.indexOf('Medical Code: ') + 14);
        const time = useHebrew ? getTextBetweenStrings(text, '. ', 'מונה:') : getTextBetweenStrings(text, '. ', 'Incident') || fallbackTime;
        const details = useHebrew ? text.substring(0, text.indexOf('קוד הזנקה:')) : text.substring(0, text.indexOf('Dispatch Code:')) || text;
        return {
            addressLines: 3,
            address: address || details,
            content: callCode,
            medicalCode,
            time: t('callDetection:receivedAt', { time }),
        };
    }, [t, callType, text, title]);
    const openInfo = useCallback(() => madaNotificationsStore.openCallPendingIntent(uuid), [madaNotificationsStore, uuid]);

    return (
        <>
            <StatusBar barStyle="light-content" backgroundColor={colors.oldPrimary} />
            <BasePageView pageStyle={styles.page} scrollType="none">
                <Text style={styles.header}>{t(`callDetection:${isAdministrativeCall(callType) ? 'newMsgTitle' : 'newCallTitle'}`)}</Text>
                <Text style={styles.msgType}>{msgType}</Text>
                <Text style={[styles.address, data.addressLines === 5 && styles.scrollable]} numberOfLines={data.addressLines} ellipsizeMode="clip">
                    {data.address}
                </Text>
                {data.content && <Text style={[styles.msgContent, !data.medicalCode && styles.msgContentFullPage]}>{data.content}</Text>}
                {data.medicalCode && <Text style={styles.medicalCode}>{data.medicalCode}</Text>}
                <Text style={styles.msgTime}>{data.time}</Text>
                <Icon name="mada" size={60} color={colors.oldPrimary} style={styles.logo} />
                <Text style={styles.appName}>{t('common:appName')}</Text>
                <View style={styles.buttonsRow}>
                    <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: true }} wrapperStyle={styles.buttonRipple} onPress={BackHandler.exitApp}>
                        <Text style={styles.buttonTxt}>{t('callDetection:closeBtn')}</Text>
                    </PressableRippleWrapper>
                    <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: true }} wrapperStyle={styles.buttonRipple} onPress={openInfo}>
                        <Text style={styles.buttonTxt}>{t('callDetection:callInfo')}</Text>
                    </PressableRippleWrapper>
                </View>
            </BasePageView>
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
    scrollable: {
        flex: 1,
        overflow: 'scroll',
    },
    msgContent: {
        fontSize: 27,
        marginTop: 5,
        textAlign: 'center',
        textAlignVertical: 'center',
    },
    msgContentFullPage: {
        flex: 1,
    },
    medicalCode: {
        flex: 1,
        fontSize: 27,
        marginTop: 5,
        fontWeight: 'bold',
        textAlign: 'center',
        color: colors.colorPrimary,
        textAlignVertical: 'top',
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
