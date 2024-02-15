import Color from 'color';
import i18n from '../../localization';
import { injectable } from 'inversify';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { makeAutoObservable, observable, runInAction } from 'mobx';
import { FlatList, StyleSheet, View, useWindowDimensions } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon } from '../../Components/Icon';
import { Menu } from '../../Components/Menu';
import { BaseDialog } from '../dialogs/BaseDialog';
import { MenuItem } from '../../Components/MenuItem';
import { TextInput } from '../../Components/TextInput';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { DispatchService } from '../../services/DispatchService';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { TeamMemberDialog, TeamMember, MedicalRank } from '../dialogs/TeamMemberDialog';
import type { NavigatableDispatchScreenProps } from '../../containers/Pages/Dispatch';

@injectable()
class TeamReportLocalStore {
    private validateErrors = false;
    public ambulanceNumber = '';
    public readonly teamMembers = observable.map<string, TeamMember>(undefined, { deep: false });

    constructor(private readonly dialogStore: DialogStore, private readonly dispatchService: DispatchService) {
        makeAutoObservable(this, undefined, { autoBind: true });
    }

    public get ambulanceError() {
        if (!this.validateErrors || this.ambulanceNumber.trim().length) {
            return undefined;
        }
        return i18n.t('dispatch:form:error:ambulanceNumber');
    }

    public onAmbulanceChange(value: string) {
        this.ambulanceNumber = value;
    }

    public async onSend() {
        this.validateErrors = true;
        if (this.ambulanceError) {
            return;
        }
        if (!this.teamMembers.size) {
            return this.dialogStore.openDialog(BaseDialog, {
                okText: i18n.t('common:ok'),
                title: i18n.t('common:error'),
                description: i18n.t('dispatch:team:noMembers'),
            });
        }
        const region = await this.dispatchService.getRegion();
        const regionName = i18n.t(`dispatch:regions:${region}`);
        let sms = `צוות אמבולנס ${regionName} ${this.ambulanceNumber}`;
        for (const member of this.teamMembers.values()) {
            sms += `\n${i18n.t(`dispatch:team:ranks:${member.rank}`, { lng: 'he' })} - ${member.name}`;
        }
        return this.dispatchService.sendSms(sms);
    }

    public async onAdd() {
        const member = await this.dialogStore.openDialog(TeamMemberDialog);
        if (!member) {
            return;
        }
        runInAction(() => this.teamMembers.set(member.id, member));
    }

    public async onEdit(member: TeamMember) {
        const edited = await this.dialogStore.openDialog(TeamMemberDialog, { member });
        if (!edited) {
            return;
        }
        runInAction(() => this.teamMembers.set(member.id, edited));
    }

    public onRemove(member: TeamMember) {
        this.teamMembers.delete(member.id);
    }
}
const ripple = Color(colors.colorPrimary).lighten(0.2).fade(0.5).toString();
const ROLE_COLOR_MAP: Record<MedicalRank, string> = {
    Driver: colors.hovesh,
    EMT: colors.hovesh,
    Paramedic: colors.para,
    Doctor: colors.para,
    Volunteer: colors.noar,
    Intern: colors.hovesh,
    NationalService: colors.hovesh,
};
const TeamMemberItem = observer<{ member: TeamMember; localStore: TeamReportLocalStore }>(({ member, localStore }) => {
    const { width } = useWindowDimensions();
    const maxWidth = width / 2 - 30;
    const { menuVisible, openMenu, dismissMenu, edit, remove } = useLocalObservable(() => ({
        menuVisible: false,
        openMenu() {
            this.menuVisible = true;
        },
        dismissMenu() {
            this.menuVisible = false;
        },
        edit() {
            this.dismissMenu();
            return localStore.onEdit(member);
        },
        remove() {
            this.dismissMenu();
            return localStore.onRemove(member);
        },
    }));
    return (
        <View style={[styles.memberCard, { maxWidth }]}>
            <Icon size={100} name="person" color={ROLE_COLOR_MAP[member.rank]} style={styles.memberIcon} />
            <View style={styles.memberBottomSection}>
                <View>
                    <Text style={styles.memberName}>{member.name}</Text>
                    <Text style={styles.memberRank}>{i18n.t(`dispatch:team:ranks:${member.rank}`)}</Text>
                </View>
                <Menu
                    visible={menuVisible}
                    onDismiss={dismissMenu}
                    anchor={
                        <PressableRippleWrapper wrapperStyle={styles.memberBtn} android_ripple={{ color: ripple, borderless: false }} onPress={openMenu}>
                            <Icon size={25} name="more" />
                        </PressableRippleWrapper>
                    }>
                    <MenuItem leadingIcon="edit" title={i18n.t('dispatch:team:edit')} onPress={edit} />
                    <MenuItem leadingIcon="trash" title={i18n.t('dispatch:team:delete')} onPress={remove} />
                </Menu>
            </View>
        </View>
    );
});
TeamMemberItem.displayName = 'TeamMemberItem';

export const TeamReport = observer<NavigatableDispatchScreenProps>(() => {
    const localStore = useLocalStoreWithInjectedDependencies(TeamReportLocalStore);

    return (
        <BasePageView scrollType="none">
            <View style={styles.scrollContent}>
                <TextInput
                    maxLength={4}
                    keyboardType="number-pad"
                    error={localStore.ambulanceError}
                    value={localStore.ambulanceNumber}
                    wrapperStyle={styles.inputWrapper}
                    label={i18n.t('dispatch:form:ambulanceNumber')}
                    onChangeText={localStore.onAmbulanceChange}
                />
                {!localStore.teamMembers.size && <Text style={styles.note}>{i18n.t('dispatch:team:note')}</Text>}
                <FlatList
                    numColumns={2}
                    keyExtractor={({ id }) => id}
                    data={Array.from(localStore.teamMembers.values())}
                    renderItem={({ item }) => <TeamMemberItem member={item} localStore={localStore} />}
                />
                <View style={styles.pageFooter}>
                    {localStore.teamMembers.size <= 3 && (
                        <PressableRippleWrapper android_ripple={{ color: ripple, borderless: false }} style={styles.addBtn} wrapperStyle={styles.addBtnWrapper} onPress={localStore.onAdd}>
                            <Text style={styles.addText}>{i18n.t('dispatch:team:addMember')}</Text>
                        </PressableRippleWrapper>
                    )}
                    <PressableRippleWrapper android_ripple={{ color: ripple, borderless: false }} style={styles.sendBtn} wrapperStyle={styles.sendBtnWrapper} onPress={localStore.onSend}>
                        <Text style={styles.sendText}>{i18n.t('common:send')}</Text>
                        <Icon size={24} color={colors.white} name="send" />
                    </PressableRippleWrapper>
                </View>
            </View>
        </BasePageView>
    );
});
TeamReport.displayName = 'TeamReport';

const styles = StyleSheet.create({
    scrollContent: {
        flex: 1,
        paddingVertical: 10,
        paddingHorizontal: 15,
    },
    inputWrapper: {
        marginBottom: 0,
    },
    note: {
        fontSize: 16,
        color: colors.darkGrey,
    },
    pageFooter: {
        gap: 10,
        flexShrink: 0,
        marginBottom: 10,
        flexDirection: 'row',
        paddingHorizontal: 15,
    },
    addBtnWrapper: {
        flexGrow: 1,
        elevation: 4,
        borderRadius: 15,
    },
    addBtn: {
        height: 60,
        paddingHorizontal: 15,
        backgroundColor: colors.white,
    },
    addText: {
        flexGrow: 1,
        fontSize: 20,
        textAlign: 'center',
        verticalAlign: 'middle',
        color: colors.colorNice,
    },
    sendBtnWrapper: {
        flexGrow: 1,
        flexShrink: 0,
        elevation: 4,
        borderRadius: 15,
    },
    sendBtn: {
        gap: 10,
        height: 60,
        alignItems: 'center',
        flexDirection: 'row',
        paddingHorizontal: 15,
        justifyContent: 'center',
        backgroundColor: colors.colorNice,
    },
    sendText: {
        flexGrow: 1,
        fontSize: 20,
        textAlign: 'center',
        color: colors.white,
    },
    memberCard: {
        margin: 5,
        flex: 0.5,
        elevation: 10,
        flexShrink: 0,
        borderRadius: 6,
        paddingVertical: 5,
        paddingHorizontal: 10,
        backgroundColor: colors.white,
    },
    memberIcon: {
        alignSelf: 'center',
    },
    memberBottomSection: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    memberBtn: {
        borderRadius: 20,
    },
    memberName: {
        fontSize: 15,
        color: colors.albumTitle,
    },
    memberRank: {
        fontSize: 12,
    },
});
