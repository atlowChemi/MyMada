import Dialog from './@FontWrappers';
import { useCallback, memo } from 'react';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { colors } from '../../consts';
import type { IDialogInstance } from '../../services/DialogStore';

type Response = 'rateNow' | 'later' | 'never';
interface RateUsDialogProps extends IDialogInstance<Response> {}

export const RateUsDialog = memo<RateUsDialogProps>(({ closeDialog }) => {
    const { t } = useTranslation();
    const appName = t('common:appName');
    const close = useCallback((response: Response) => () => closeDialog(response), [closeDialog]);

    return (
        <Dialog.Container visible>
            <Dialog.Title style={styles.dialogColorDark}>{t('rateUs:title', { appName })}</Dialog.Title>
            <Dialog.Description style={styles.dialogColorDark}>{t('rateUs:text', { appName })}</Dialog.Description>
            <Dialog.Button style={styles.dialogColor} label={t('common:notNow')} onPress={close('later')} />
            <Dialog.Button style={styles.dialogColor} label={t('rateUs:never')} onPress={close('never')} />
            <Dialog.Button style={styles.dialogColor} label={t('rateUs:rateNow')} onPress={close('rateNow')} />
        </Dialog.Container>
    );
});
RateUsDialog.displayName = 'RateUsDialog';

const styles = StyleSheet.create({
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
});
