import Dialog from './@FontWrappers';
import { KeyboardTypeOptions, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import type { ComponentProps } from 'react';
import type { IDialogInstance } from '../../services/DialogStore';

interface TextInputDialogProps extends IDialogInstance<string> {
    title: string;
    placeholder: string;
    initialValue?: string;
    blockBackBtn?: boolean;
    keyboardType?: KeyboardTypeOptions;
    blockBackdropPress?: boolean;
    isValid?(value: string): string | undefined;
    autoComplete?: ComponentProps<typeof Dialog.Input>['autoComplete'];
}

export const TextInputDialog = observer<TextInputDialogProps>(({ closeDialog, isValid, blockBackBtn, blockBackdropPress, placeholder, title, autoComplete, initialValue, keyboardType }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        value: initialValue ?? '',
        error: '',
        onChange(value: string) {
            this.value = value;
            this.error = isValid?.(value) ?? '';
        },
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
        onApprove() {
            if (this.error) {
                return;
            }
            closeDialog(this.value);
        },
    }));

    return (
        <Dialog.Container visible onRequestClose={localStore.onRequestClose} onBackdropPress={localStore.onBackdropPress}>
            <Dialog.Title style={styles.dialogColorDark}>{title}</Dialog.Title>
            <Dialog.Input
                value={localStore.value}
                autoComplete={autoComplete}
                cursorColor={colors.colorNice}
                style={styles.dialogColorDark}
                keyboardType={keyboardType}
                placeholder={placeholder}
                placeholderTextColor={localStore.error ? colors.oldPrimaryDark : colors.colorNice}
                underlineColorAndroid={localStore.error ? colors.oldPrimaryDark : colors.colorNice}
                onChangeText={localStore.onChange}
            />
            {localStore.error && <Text style={styles.errorHint}>{localStore.error}</Text>}
            <Dialog.Button style={styles.dialogColor} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});
TextInputDialog.displayName = 'TextInputDialog';

const styles = StyleSheet.create({
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
    errorHint: {
        color: colors.oldPrimaryDark,
        start: 10,
        top: -25,
    },
});
