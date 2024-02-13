import Dialog from 'react-native-dialog';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import type { IDialogInstance } from '../../services/DialogStore';

interface RequestCodeApprovalProps extends IDialogInstance<boolean> {
    region: string;
    msg: string;
}

export const SmsApproval = observer<RequestCodeApprovalProps>(({ region, msg, closeDialog }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        onCancel() {
            closeDialog(false);
        },
        onApprove() {
            closeDialog(true);
        },
    }));

    return (
        <Dialog.Container visible>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('dispatch:smsReviewTitle')}</Dialog.Title>
            <View style={styles.contentView}>
                <Text style={styles.targetDispatch}>{t('dispatch:smsDispatchTarget', { region })}</Text>
                <Text style={[styles.font, styles.dialogColorDark]}>{msg}</Text>
                <Text style={styles.msg}>{t('dispatch:smsCounter', { length: msg.length, msgCount: 1 + Math.floor(msg.length / 70) })}</Text>
            </View>
            <Dialog.Button style={[styles.font, styles.negativeColor]} label={t('common:cancel')} onPress={localStore.onCancel} />
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});
SmsApproval.displayName = 'SmsApproval';

const styles = StyleSheet.create({
    contentView: {
        gap: 10,
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    targetDispatch: {
        fontSize: 16,
        color: colors.reviewTitles,
    },
    msg: {
        fontSize: 16,
        color: colors.materialGrey600,
    },
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
    negativeColor: {
        color: colors.oldPrimary,
    },
});
