import Dialog from 'react-native-dialog';
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
            closeDialog(false);
        },
        onRequestClose() {
            if (blockBackBtn) {
                return;
            }
            closeDialog(false);
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
            {title && <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{title}</Dialog.Title>}
            <Dialog.Description style={[styles.font, styles.dialogColorDark]}>{description}</Dialog.Description>
            {cancelText && <Dialog.Button style={[styles.font, negativeBtnColor ? { color: negativeBtnColor } : styles.dialogColor]} label={cancelText} onPress={localStore.onCancel} />}
            {okText && <Dialog.Button style={[styles.font, styles.dialogColor]} label={okText} onPress={localStore.onApprove} />}
        </Dialog.Container>
    );
});
BaseDialog.displayName = 'BaseDialog';

const styles = StyleSheet.create({
    font: {
        fontFamily: 'opensanshebrew',
    },
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
});
