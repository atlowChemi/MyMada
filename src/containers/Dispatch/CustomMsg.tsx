import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon } from '../../Components/Icon';
import { TextInput } from '../../Components/TextInput';
import { BasePageView } from '../../Components/BasePageView';
import { DispatchService } from '../../services/DispatchService';
import { useDependencyInjector } from '../../services/container';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import type { NavigatableDispatchScreenProps } from '../../containers/Pages/Dispatch';

const CustomMsgFooter = observer<{ onSend(): void }>(({ onSend }) => (
    <View style={styles.pageFooter}>
        <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.sendBtn} wrapperStyle={styles.sendBtnWrapper} onPress={onSend}>
            <Text style={styles.sendText}>{i18n.t('common:send')}</Text>
            <Icon size={24} color={colors.white} name="send" />
        </PressableRippleWrapper>
    </View>
));
CustomMsgFooter.displayName = 'CustomMsgFooter';

export const CustomMsg = observer<NavigatableDispatchScreenProps>(() => {
    const dispatchService = useDependencyInjector(DispatchService);
    const localStore = useLocalObservable(() => ({
        msg: '',
        msgError: '',
        onMsgChange(msg: string) {
            this.msg = msg;
        },
        onSend() {
            if (!this.msg.trim()) {
                this.msgError = i18n.t('dispatch:general:msgErr');
                return;
            }
            this.msgError = '';
            return dispatchService.sendSms(this.msg);
        },
    }));
    return (
        <BasePageView scrollContentStyle={styles.scrollContent} postScroll={<CustomMsgFooter onSend={localStore.onSend} />}>
            <TextInput numberOfLines={6} returnKeyType="done" error={localStore.msgError} onChangeText={localStore.onMsgChange} label={i18n.t('dispatch:general:msgHint')} />
        </BasePageView>
    );
});
CustomMsg.displayName = 'CustomMsg';

const styles = StyleSheet.create({
    scrollContent: {
        flex: undefined,
        paddingBottom: 10,
        rowGap: 10,
    },
    pageFooter: {
        flexShrink: 0,
        marginBottom: 10,
        paddingHorizontal: 15,
    },
    sendText: {
        flex: 1,
        fontSize: 20,
        textAlign: 'center',
        color: colors.white,
    },
    sendBtnWrapper: {
        elevation: 4,
        borderRadius: 15,
    },
    sendBtn: {
        width: '100%',
        height: 60,
        overflow: 'hidden',
        alignItems: 'center',
        flexDirection: 'row',
        paddingHorizontal: 15,
        justifyContent: 'center',
        backgroundColor: colors.colorNice,
    },
});
