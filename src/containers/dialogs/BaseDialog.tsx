import Dialog from './@FontWrappers';
import { StyleSheet } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import type { IDialogInstance } from '../../services/DialogStore';

interface BaseDialogProps extends IDialogInstance<boolean> {
    title?: string;
    description: string;
    okText?: string;
    cancelText?: string;
    blockBackdropPress?: boolean;
    blockBackBtn?: boolean;
    negativeBtnColor?: string;
}

export const BaseDialog = observer<BaseDialogProps>(({ title, description, okText, cancelText, blockBackdropPress, blockBackBtn, negativeBtnColor, closeDialog }) => {
    const localStore = useLocalObservable(() => ({
        onBackdropPress() {
            if (blockBackdropPress) {
                return;
            }
            closeDialog(undefined);
        },
        onRequestClose() {
            if (blockBackBtn) {
                return;
            }
            closeDialog(undefined);
        },
        onCancel() {
            closeDialog(false);
        },
        onApprove() {
            closeDialog(true);
        },
    }));

    return (
        <Dialog.Container visible onBackdropPress={localStore.onBackdropPress} onRequestClose={localStore.onRequestClose}>
            {title && <Dialog.Title style={styles.dialogColorDark}>{title}</Dialog.Title>}
            <Dialog.Description style={styles.dialogColorDark}>{description}</Dialog.Description>
            {cancelText && <Dialog.Button style={[negativeBtnColor ? { color: negativeBtnColor } : styles.dialogColor]} label={cancelText} onPress={localStore.onCancel} />}
            {okText && <Dialog.Button style={[styles.dialogColor]} label={okText} onPress={localStore.onApprove} />}
        </Dialog.Container>
    );
});
BaseDialog.displayName = 'BaseDialog';

const styles = StyleSheet.create({
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
});
