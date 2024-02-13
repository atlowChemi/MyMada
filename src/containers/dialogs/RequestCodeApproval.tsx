import Dialog from 'react-native-dialog';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import type { IDialogInstance } from '../../services/DialogStore';

interface RequestCodeApprovalProps extends IDialogInstance<boolean> {
    email: string;
    name: string;
    info: string;
}

export const RequestCodeApproval = observer<RequestCodeApprovalProps>(({ email, name, info, closeDialog }) => {
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
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('activationCode:review')}</Dialog.Title>
            <View style={styles.contentView}>
                <Text style={styles.warning}>{t('activationCode:reviewAlert')}</Text>
                <View style={styles.titleWrapper}>
                    <Text style={styles.title}>{t('activationCode:reviewSender')}</Text>
                    <Text>
                        {name} - {email}
                    </Text>
                </View>
                <View style={styles.titleWrapper}>
                    <Text style={styles.title}>{t('activationCode:reviewContent')}</Text>
                    <Text>{info}</Text>
                </View>
            </View>
            <Dialog.Button style={[styles.font, styles.negativeColor]} label={t('common:cancel')} onPress={localStore.onCancel} />
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});
RequestCodeApproval.displayName = 'RequestCodeApproval';

const styles = StyleSheet.create({
    contentView: {
        gap: 10,
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    warning: {
        fontSize: 16,
        color: colors.colorRedDark,
    },
    titleWrapper: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    title: {
        fontSize: 16,
        color: colors.reviewTitles,
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
