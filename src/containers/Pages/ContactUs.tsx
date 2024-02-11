import { useMemo } from 'react';
import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { action, makeAutoObservable, observable, reaction, runInAction } from 'mobx';
import { ActivityIndicator, GestureResponderEvent, Pressable, StyleSheet, View } from 'react-native';
import database from '@react-native-firebase/database';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Icon } from '../../Components/Icon';
import { Text } from '../../Components/Text';
import { BaseDialog } from '../dialogs/BaseDialog';
import { TextInput } from '../../Components/TextInput';
import { DialogStore } from '../../services/DialogStore';
import { TextInputDialog } from '../dialogs/TextInputDialog';
import { BasePageView } from '../../Components/BasePageView';
import { MessagingStore } from '../../services/MessagingStore';
import { ConnectionError } from '../ConnectionError';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import type { IReactionDisposer } from 'mobx';
import type { FirebaseDatabaseTypes } from '@react-native-firebase/database';
import type { NavigatableScreenProps } from '../../App';

interface Message {
    key: string;
    msg: string;
    time?: number;
    sender: string;
    WhatsApp?: boolean;
}

@injectable()
class ContactUsLocalStore {
    private message: string = '';
    private name?: string = undefined;
    private showDisclaimer = true;
    private _showNameDialog = false;
    private _nameChangeError?: string = undefined;
    private lastSeenByAdminStatus = false;
    public readonly messages: Message[] = [];
    private readonly disposers: IReactionDisposer[];
    private readonly eventCleanups: (() => void)[] = [];
    private readonly intlFormatter = new Intl.DateTimeFormat('default', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    });

    constructor(private readonly messagingStore: MessagingStore, private readonly sharedPreferencesStore: SharedPreferencesStore, private readonly dialogStore: DialogStore) {
        type PrivateKeys = '_showNameDialog' | '_nameChangeError' | 'name' | 'message' | 'lastSeenByAdminStatus' | 'onChildAdded';
        makeAutoObservable<ContactUsLocalStore, PrivateKeys>(this, {
            name: observable.ref,
            message: observable.ref,
            askForName: action.bound,
            nameChanged: action.bound,
            onChildAdded: action.bound,
            messageChanged: action.bound,
            messages: observable.shallow,
            _showNameDialog: observable.ref,
            _nameChangeError: observable.ref,
            lastSeenByAdminStatus: observable.ref,
        });
        this.disposers = [
            reaction(
                () => this.chatRootDocument.state,
                () => this.setupMessagesAndCleanup(),
                { fireImmediately: true },
            ),
            reaction(
                () => this.sharedPreferences.state,
                () => {
                    if (this.sharedPreferences.state !== 'fulfilled') {
                        return;
                    }
                    this.nameChanged(this.sharedPreferences.value.UserName || '');
                },
                { fireImmediately: true },
            ),
        ];
        this.openDisclaimerDialogAndEnforceName();
        sharedPreferencesStore.name = 'Settings';
    }

    private get sharedPreferences() {
        return this.sharedPreferencesStore.activeChannel;
    }

    private get chatRootDocument() {
        return fromPromise(this.getChatRootDocument());
    }

    public get seenByAdmin() {
        if (!this.messages.length) {
            return '';
        }
        return i18n.t(this.lastSeenByAdminStatus ? 'contact:msgSeen' : 'contact:msgNotSeen');
    }

    public get savedName() {
        return this.name || '';
    }

    public get messageValue() {
        return this.message;
    }

    public get isNotFulfilled() {
        return [this.messagingStore.token.state, this.sharedPreferences.state].every(state => state !== 'fulfilled');
    }

    public get hasError() {
        return this._nameChangeError;
    }

    public get showNameDialog() {
        return !this.showDisclaimer && this._showNameDialog;
    }

    public dispose() {
        this.disposers.forEach(disposer => disposer());
        this.eventCleanups.forEach(cleanup => cleanup());
    }

    private async openDisclaimerDialogAndEnforceName() {
        await this.dialogStore.openDialog(BaseDialog, {
            title: i18n.t('contact:plsNote'),
            description: i18n.t('contact:disclaimer'),
            okText: i18n.t('common:ok'),
            blockBackBtn: true,
            blockBackdropPress: true,
        });
        if (this.sharedPreferences.state !== 'fulfilled') {
            crashlytics().log('Shared preferences not fulfilled in openDisclaimerDialogAndEnforceName');
            return;
        }
        if (!this.sharedPreferences.value.UserName?.length) {
            await this.askForName(false);
        }
    }

    private onChildAdded = (data: FirebaseDatabaseTypes.DataSnapshot) => {
        const key = data.key;
        if (!key || ['Name', 'LastSent'].includes(key)) {
            return;
        }
        if (key === 'SeenByAdmin') {
            const value = Boolean(data.val());
            this.lastSeenByAdminStatus = value;
            return;
        }
        const object = data.val();
        const { sender, msg, WhatsApp, time } = object;
        this.messages.push({ sender, msg, WhatsApp, time, key });
    };

    private async getChatRootDocument() {
        const token = await this.messagingStore.token;
        return database().ref('chats').child(token);
    }

    private async setupMessagesAndCleanup() {
        if (this.chatRootDocument.state !== 'fulfilled') {
            return;
        }
        this.eventCleanups.forEach(cleanup => cleanup());
        this.eventCleanups.length = 0;
        const root = this.chatRootDocument.value;
        root.on('child_added', this.onChildAdded);
        this.eventCleanups.push(() => root.off('child_added', this.onChildAdded));
    }

    public async askForName(cancelable: boolean | GestureResponderEvent = true) {
        const nameResult = await this.dialogStore.openDialog(TextInputDialog, {
            autoComplete: 'name',
            blockBackBtn: !cancelable,
            initialValue: this.savedName,
            blockBackdropPress: !cancelable,
            title: i18n.t('contact:writeName'),
            placeholder: i18n.t('contact:name'),
            isValid(value) {
                const name = value?.trim();
                if (!name) {
                    return i18n.t('contact:emptyName');
                }
                if (/^\d+$/.test(name)) {
                    return i18n.t('contact:numeralName');
                }
            },
        });
        if (!nameResult) {
            return;
        }
        const root = await this.chatRootDocument;
        await Promise.all([this.sharedPreferencesStore.setItem('UserName', nameResult), root.child('Name').set(nameResult)]);
        runInAction(() => (this.name = nameResult));
    }

    public nameChanged(name: string) {
        this.name = name;
    }

    public messageChanged(value: string) {
        this.message = value;
    }

    public sendMessage = async () => {
        if (!this.savedName || this.savedName.trim().length < 1) {
            return this.askForName(false);
        }
        const msg = this.message?.trim() || '';
        if (!msg.length) {
            this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('common:error'),
                description: i18n.t('contact:emptyMsg'),
                okText: i18n.t('common:ok'),
                blockBackdropPress: true,
            });
            return;
        }
        const [root, fcmToken] = await Promise.all([this.chatRootDocument, this.messagingStore.token]);
        const newRef = root.push();
        const time = Date.now();
        await Promise.all([newRef.update({ sender: fcmToken, msg, time }), root.update({ Name: this.name, LastSent: time, SeenByAdmin: false })]);
        runInAction(() => (this.message = ''));
    };

    public formatDate(unix?: number) {
        const date = unix ? new Date(unix) : new Date();
        return this.intlFormatter.format(date);
    }
}

const ChatFooter = observer<{ localStore: ContactUsLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <View style={styles.chatFooter}>
            <Text>{localStore.seenByAdmin}</Text>
            <View style={styles.sendSection}>
                <TextInput wrapperStyle={styles.sendText} value={localStore.messageValue} onChangeText={localStore.messageChanged} numberOfLines={3} label={t('contact:msgHint')} />
                <Pressable android_ripple={{ color: colors.ocean, radius: 36, borderless: false }} style={[styles.sendBtn]} onPress={localStore.sendMessage}>
                    <Icon size={24} color={colors.white} name="send" />
                </Pressable>
            </View>
        </View>
    );
});
ChatFooter.displayName = 'ChatFooter';

const ChatMessage = observer<{ localStore: ContactUsLocalStore; message: Message }>(({ localStore, message }) => {
    const { t } = useTranslation();
    const isAdmin = message.sender === 'admin';
    const time = useMemo(() => localStore.formatDate(message.time), [localStore, message.time]);

    return (
        <View style={[styles.contactChatCard, isAdmin && { backgroundColor: colors.chatAdmin }]}>
            <View style={styles.msgTitle}>
                <Text style={styles.msgSender}>{isAdmin ? t('contact:admin') : `${localStore.savedName}:` || t('contact:you')}</Text>
                <Text style={styles.msgTime}>{time}</Text>
            </View>
            <Text style={styles.msgTxt}>{message.msg}</Text>
        </View>
    );
});
ChatMessage.displayName = 'ChatMessage';

export const ContactUs = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const messagingStore = useDependencyInjector(MessagingStore);
    const localStore = useLocalStoreWithInjectedDependencies(ContactUsLocalStore);

    if (localStore.isNotFulfilled) {
        return (
            <BasePageView scrollType="none">
                {messagingStore.token.state === 'rejected' ? (
                    <ConnectionError />
                ) : (
                    <View style={styles.loadingWrapper}>
                        <ActivityIndicator size={60} color={colors.colorPrimary} />
                    </View>
                )}
            </BasePageView>
        );
    }

    // TODO: navigate to bottom
    return (
        <BasePageView scrollContentStyle={styles.scrollContent} postScroll={<ChatFooter localStore={localStore} />}>
            <Pressable onPress={localStore.askForName} style={styles.contactChatCard}>
                <Text style={styles.text}>{t('contact:changeName')}</Text>
            </Pressable>
            {localStore.messages.map(message => (
                <ChatMessage key={message.key} localStore={localStore} message={message} />
            ))}
        </BasePageView>
    );
});
ContactUs.displayName = 'ContactUs';

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
    errorHint: {
        color: colors.oldPrimaryDark,
        start: 10,
        top: -25,
    },
    loadingWrapper: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    scrollContent: {
        flex: undefined,
        paddingBottom: 10,
        rowGap: 10,
    },
    text: {
        fontSize: 20,
    },
    contactChatCard: {
        padding: 10,
        backgroundColor: colors.white,
        borderRadius: 6,
        elevation: 5,
    },
    chatFooter: {
        paddingHorizontal: 15,
        flexShrink: 0,
    },
    sendSection: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    sendText: {
        marginStart: 0,
        marginBottom: 10,
        flexGrow: 1,
    },
    sendBtn: {
        width: 60,
        height: 60,
        flexShrink: 0,
        elevation: 4,
        borderRadius: 15,
        overflow: 'hidden',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: colors.colorNice,
    },
    msgTitle: {
        flexDirection: 'row',
    },
    msgSender: {
        flexGrow: 1,
        fontSize: 19,
        color: colors.albumTitle,
    },
    msgTime: {
        fontSize: 12,
        flexShrink: 0,
        color: colors.albumTitle,
    },
    msgTxt: {
        fontSize: 16,
        color: colors.black,
    },
});
