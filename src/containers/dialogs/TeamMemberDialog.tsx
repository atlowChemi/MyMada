import Dialog from 'react-native-dialog';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Dropdown } from 'react-native-element-dropdown';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { randomString } from '../../utils';
import { TextInput } from '../../Components/TextInput';
import type { IDialogInstance } from '../../services/DialogStore';

export type MedicalRank = 'Driver' | 'EMT' | 'Paramedic' | 'Doctor' | 'Volunteer' | 'Intern' | 'NationalService';
export interface TeamMember {
    readonly id: string;
    name: string;
    rank: MedicalRank;
}

interface TeamMemberDialogProps extends IDialogInstance<TeamMember> {
    member?: TeamMember;
}

export const TeamMemberDialog = observer<TeamMemberDialogProps>(({ member, closeDialog }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        validateErrors: false,
        member: { ...(member ?? ({ id: randomString(), name: '', rank: 'Driver' } as TeamMember)) },
        get ranks(): { label: string; value: MedicalRank }[] {
            return [
                { label: t('dispatch:team:ranks:Driver') as string, value: 'Driver' },
                { label: t('dispatch:team:ranks:EMT') as string, value: 'EMT' },
                { label: t('dispatch:team:ranks:Paramedic') as string, value: 'Paramedic' },
                { label: t('dispatch:team:ranks:Doctor') as string, value: 'Doctor' },
                { label: t('dispatch:team:ranks:Volunteer') as string, value: 'Volunteer' },
                { label: t('dispatch:team:ranks:Intern') as string, value: 'Intern' },
                { label: t('dispatch:team:ranks:NationalService') as string, value: 'NationalService' },
            ];
        },
        get error() {
            if (!this.validateErrors || this.member.name.trim().length) {
                return undefined;
            }
            return t('contact:emptyName');
        },
        onCancel() {
            closeDialog(undefined);
        },
        onApprove() {
            this.validateErrors = true;
            if (this.error) {
                return;
            }
            closeDialog(this.member);
        },
        onRankChange({ value }: { value: MedicalRank }) {
            this.member.rank = value;
        },
        onNameChange(value: string) {
            this.member.name = value;
        },
    }));

    return (
        <Dialog.Container visible>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t(`dispatch:team:${member ? 'edit' : 'add'}Member`)}</Dialog.Title>
            <View style={styles.contentView}>
                <Dropdown
                    data={localStore.ranks}
                    labelField="label"
                    valueField="value"
                    fontFamily="opensanshebrew"
                    style={styles.dropdown}
                    value={localStore.member.rank}
                    onChange={localStore.onRankChange}
                />
                <TextInput label={t('dispatch:team:memberName')} value={localStore.member.name} onChangeText={localStore.onNameChange} error={localStore.error} textContentType="name" />
            </View>
            <Dialog.Button style={[styles.font, styles.negativeColor]} label={t('common:cancel')} onPress={localStore.onCancel} />
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});
TeamMemberDialog.displayName = 'TeamMemberDialog';

const styles = StyleSheet.create({
    contentView: {
        gap: 10,
    },
    font: {
        fontFamily: 'opensanshebrew',
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
    dropdown: {
        marginHorizontal: 10,
        borderBottomWidth: 0.5,
        borderColor: colors.buttonMaterialDark,
    },
});
