import Dialog from './@FontWrappers';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { RadioButtonGroup, RadioButtonGroupProps } from '../../Components/RadioButtonGroup';
import type { IDialogInstance } from '../../services/DialogStore';

export interface RadioSelectDialogProps<T extends {}> extends IDialogInstance<T> {
    title?: string;
    options: RadioButtonGroupProps<T>['options'];
    initialValue?: T;
}

export const RadioSelectDialog = observer(<T extends {}>({ title, options, initialValue, closeDialog }: RadioSelectDialogProps<T>) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        value: initialValue || Object.values(options)[0],
        setValue(value: T) {
            closeDialog(value);
        },
        onCancel() {
            closeDialog(undefined);
        },
    }));

    return (
        <Dialog.Container visible onBackdropPress={localStore.onCancel} onRequestClose={localStore.onCancel}>
            {title && <Dialog.Title style={styles.dialogColorDark}>{title}</Dialog.Title>}
            <RadioButtonGroup direction="vertical" options={options} value={localStore.value} onChange={localStore.setValue} itemStyle={styles.items} />
            <Dialog.Button style={styles.dialogColor} label={t('common:cancel')} onPress={localStore.onCancel} />
        </Dialog.Container>
    );
});
RadioSelectDialog.displayName = 'RadioSelectDialog';

const styles = StyleSheet.create({
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    dialogColor: {
        color: colors.colorNice,
    },
    items: {
        paddingVertical: 3,
    },
});
