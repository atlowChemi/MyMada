import { computedFn } from 'mobx-utils';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { observer, useLocalObservable } from 'mobx-react-lite';
import Dialog from 'react-native-dialog/';
import { colors } from '../../consts';
import type { DictionaryEntry } from '../Tools/Dictionary';
import type { IDialogInstance } from '../../services/DialogStore';

interface DictionaryFormProps extends IDialogInstance<DictionaryEntry> {
    item?: DictionaryEntry;
}

export const DictionaryForm = observer<DictionaryFormProps>(({ item, closeDialog }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        name: item?.name ?? '',
        definition: item?.definition ?? '',
        showErrors: false,
        isActing: false,
        get isEditing() {
            return Boolean(item);
        },
        get nameHasError() {
            return this.showErrors && !this.name;
        },
        get definitionHasError() {
            return this.showErrors && !this.definition;
        },
        get disabled() {
            const empty = !this.name || !this.definition;
            const unchanged = this.name === item?.name && this.definition === item?.definition;
            return empty || unchanged || this.isActing;
        },
        get buttonColor() {
            return computedFn((disabled: boolean) => (disabled ? colors.shadow : colors.colorNice));
        },
        nameChanged(val: string) {
            this.name = val;
        },
        definitionChanged(val: string) {
            this.definition = val;
        },
        onClose() {
            closeDialog();
        },
        *onSave() {
            if (this.disabled) {
                return;
            }
            this.showErrors = true;
            this.isActing = true;
            yield closeDialog({
                name: this.name,
                definition: this.definition,
                key: item?.key ?? '',
                approved: false,
            });
            this.isActing = false;
        },
    }));

    return (
        <Dialog.Container visible onBackdropPress={localStore.onClose} onRequestClose={localStore.onClose}>
            <Dialog.Title>{t(`tools:dictionary:${localStore.isEditing ? 'edit' : 'create'}:title`)}</Dialog.Title>
            <Dialog.Input
                value={localStore.name}
                autoComplete="off"
                cursorColor={colors.colorNice}
                style={[styles.font, styles.dialogColorDark]}
                label={t('tools:dictionary:name')}
                unstableLabelStyle={{ color: localStore.nameHasError ? colors.oldPrimaryDark : colors.colorNice }}
                underlineColorAndroid={localStore.nameHasError ? colors.oldPrimaryDark : colors.colorNice}
                onChangeText={localStore.nameChanged}
            />
            <Dialog.Input
                value={localStore.definition}
                autoComplete="off"
                cursorColor={colors.colorNice}
                multiline
                numberOfLines={5}
                style={[styles.font, styles.dialogColorDark, styles.definition]}
                label={t('tools:dictionary:definition')}
                unstableLabelStyle={{ color: localStore.nameHasError ? colors.oldPrimaryDark : colors.colorNice }}
                underlineColorAndroid={localStore.definitionHasError ? colors.oldPrimaryDark : colors.colorNice}
                onChangeText={localStore.definitionChanged}
            />
            <Dialog.Button style={[styles.font, { color: localStore.buttonColor(localStore.isActing) }]} label={t('common:cancel')} onPress={localStore.onClose} disabled={localStore.isActing} />
            <Dialog.Button style={[styles.font, { color: localStore.buttonColor(localStore.disabled) }]} label={t('common:save')} onPress={localStore.onSave} disabled={localStore.disabled} />
        </Dialog.Container>
    );
});

const styles = StyleSheet.create({
    item: {
        height: 48,
        paddingHorizontal: 8,
        alignItems: 'flex-start',
        justifyContent: 'center',
        backgroundColor: 'white',
    },
    itemTxt: {
        fontSize: 16,
        fontWeight: 'bold',
    },
    definition: {
        textAlignVertical: 'top',
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
});
