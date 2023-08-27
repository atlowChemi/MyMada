import { useMemo } from 'react';
import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { action, computed, makeObservable, observable, reaction, runInAction } from 'mobx';
import { ActivityIndicator, Image, Pressable, StyleSheet, View } from 'react-native';
import database from '@react-native-firebase/database';
import Dialog from 'react-native-dialog';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { TextInput } from '../../Components/TextInput';
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
    #cancelable = true;
    private message: string = '';
    private name?: string = undefined;
    private _showDisclaimer = true;
    private _showNameDialog = false;
    private _showEmptyMsgError = false;
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

    constructor(private readonly messagingStore: MessagingStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        type PrivateKeys =
            | 'sharedPreferences'
            | '_showDisclaimer'
            | '_showNameDialog'
            | '_showEmptyMsgError'
            | '_nameChangeError'
            | 'name'
            | 'message'
            | 'chatRootDocument'
            | 'lastSeenByAdminStatus'
            | 'onChildAdded';
        makeObservable<ContactUsLocalStore, PrivateKeys>(this, {
            hasError: computed,
            savedName: computed,
            name: observable.ref,
            seenByAdmin: computed,
            messageValue: computed,
            message: observable.ref,
            showDisclaimer: computed,
            showNameDialog: computed,
            isNotFulfilled: computed,
            askForName: action.bound,
            nameChanged: action.bound,
            chatRootDocument: computed,
            onChildAdded: action.bound,
            sharedPreferences: computed,
            messageChanged: action.bound,
            messages: observable.shallow,
            showEmptyMsgDialog: computed,
            closeDisclaimer: action.bound,
            closeNameDialog: action.bound,
            acceptNameDialog: action.bound,
            _showDisclaimer: observable.ref,
            _showNameDialog: observable.ref,
            _nameChangeError: observable.ref,
            closeEmptyMsgDialog: action.bound,
            _showEmptyMsgError: observable.ref,
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

    public get showDisclaimer() {
        return this._showDisclaimer;
    }

    public get showNameDialog() {
        return !this._showDisclaimer && this._showNameDialog;
    }

    public get showEmptyMsgDialog() {
        return !this._showDisclaimer && this._showEmptyMsgError;
    }

    public dispose() {
        this.disposers.forEach(disposer => disposer());
        this.eventCleanups.forEach(cleanup => cleanup());
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

    public askForName() {
        this._showNameDialog = true;
    }

    public closeDisclaimer() {
        this._showDisclaimer = false;
        if (this.sharedPreferences.state !== 'fulfilled') {
            // Log error
            return;
        }
        if (!this.sharedPreferences.value.UserName?.length) {
            this.#cancelable = false;
            this.askForName();
        }
    }

    public closeNameDialog() {
        if (!this.#cancelable) {
            return;
        }
        this._showNameDialog = false;
    }

    public closeEmptyMsgDialog() {
        this._showEmptyMsgError = false;
    }

    public async acceptNameDialog() {
        const name = this.name?.trim();
        if (!name) {
            this._nameChangeError = i18n.t('contact:emptyName');
            return;
        }
        if (/^\d+$/.test(name)) {
            this._nameChangeError = i18n.t('contact:numeralName');
            return;
        }
        this._nameChangeError = undefined;
        this._showNameDialog = false;
        this.#cancelable = true;
        const root = await this.chatRootDocument;
        await Promise.all([this.sharedPreferencesStore.setItem('UserName', name), root.child('Name').set(name)]);
    }

    public nameChanged(name: string) {
        this.name = name;
    }

    public messageChanged(value: string) {
        this.message = value;
    }

    public sendMessage = async () => {
        if (!this.savedName || this.savedName.trim().length < 1) {
            runInAction(() => {
                this.#cancelable = false;
                this.askForName();
            });
            return;
        }
        const msg = this.message?.trim() || '';
        if (!msg.length) {
            runInAction(() => (this._showEmptyMsgError = true));
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

const AskForNameDialog = observer<{ localStore: ContactUsLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <Dialog.Container visible={localStore.showNameDialog} onRequestClose={localStore.closeNameDialog} onBackdropPress={localStore.closeNameDialog}>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('contact:writeName')}</Dialog.Title>
            <Dialog.Input
                value={localStore.savedName}
                autoComplete="name"
                cursorColor={colors.colorNice}
                style={[styles.font, styles.dialogColorDark]}
                placeholder={t('contact:name')}
                placeholderTextColor={localStore.hasError ? colors.oldPrimaryDark : colors.colorNice}
                underlineColorAndroid={localStore.hasError ? colors.oldPrimaryDark : colors.colorNice}
                onChangeText={localStore.nameChanged}
            />
            {localStore.hasError && <Text style={styles.errorHint}>{localStore.hasError}</Text>}
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.acceptNameDialog} />
        </Dialog.Container>
    );
});

const AnnounceWeAreNotMada = observer<{ localStore: ContactUsLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <Dialog.Container visible={localStore.showDisclaimer}>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('contact:plsNote')}</Dialog.Title>
            <Dialog.Description style={[styles.font, styles.dialogColorDark]}>{t('contact:disclaimer')}</Dialog.Description>
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.closeDisclaimer} />
        </Dialog.Container>
    );
});

const EmptyMessageSendFailure = observer<{ localStore: ContactUsLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <Dialog.Container visible={localStore.showEmptyMsgDialog}>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('common:error')}</Dialog.Title>
            <Dialog.Description style={[styles.font, styles.dialogColorDark]}>{t('contact:emptyMsg')}</Dialog.Description>
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.closeEmptyMsgDialog} />
        </Dialog.Container>
    );
});

const ChatFooter = observer<{ localStore: ContactUsLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <View style={styles.chatFooter}>
            <Text style={styles.font}>{localStore.seenByAdmin}</Text>
            <View style={styles.sendSection}>
                <TextInput wrapperStyle={styles.sendText} value={localStore.messageValue} onChangeText={localStore.messageChanged} numberOfLines={3} label={t('contact:msgHint')} />
                <Pressable android_ripple={{ color: colors.ocean, radius: 36, borderless: false }} style={[styles.sendBtn]} onPress={localStore.sendMessage}>
                    <Image source={require('../../assets/icons/send.webp')} style={[styles.sendIcon, i18n.language === 'he' && styles.rotated]} />
                </Pressable>
            </View>
        </View>
    );
});

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

export const ContactUs = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const messagingStore = useDependencyInjector(MessagingStore);
    const localStore = useLocalStoreWithInjectedDependencies(ContactUsLocalStore);

    if (localStore.isNotFulfilled) {
        return (
            <BasePageView noScroll>
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

    return (
        <>
            <AnnounceWeAreNotMada localStore={localStore} />
            <AskForNameDialog localStore={localStore} />
            <EmptyMessageSendFailure localStore={localStore} />
            <BasePageView scrollContentStyle={styles.scrollContent} postScroll={<ChatFooter localStore={localStore} />}>
                <Pressable onPress={localStore.askForName} style={styles.contactChatCard}>
                    <Text style={styles.text}>{t('contact:changeName')}</Text>
                </Pressable>
                {localStore.messages.map(message => (
                    <ChatMessage key={message.key} localStore={localStore} message={message} />
                ))}
            </BasePageView>
        </>
    );
});

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
    sendIcon: {
        height: 20,
        width: 20,
    },
    rotated: {
        transform: [{ rotate: '180deg' }],
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
