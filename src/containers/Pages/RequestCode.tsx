import { injectable } from 'inversify';
import { computedFn } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { makeAutoObservable, runInAction } from 'mobx';
import database from '@react-native-firebase/database';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { colors } from '../../consts';
import { randomString } from '../../utils';
import { Icon } from '../../Components/Icon';
import { Text } from '../../Components/Text';
import { Loader } from '../../Components/Loader';
import { BaseDialog } from '../dialogs/BaseDialog';
import { TextInput } from '../../Components/TextInput';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { MessagingStore } from '../../services/MessagingStore';
import { RequestCodeApproval } from '../dialogs/RequestCodeApproval';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NativeSyntheticEvent, TextInput as NativeTextInput, TextInputSubmitEditingEventData } from 'react-native';
import type { NavigatableScreenProps } from '../../App';

type RequestCodeKey = 'email' | 'name' | 'info';

@injectable()
class RequestCodeLocalStore {
    private isPerformingAction = false;
    private email?: string = undefined;
    private name?: string = undefined;
    private info?: string = undefined;
    private infoRef?: NativeTextInput;
    private nameRef?: NativeTextInput;
    private emailRef?: NativeTextInput;

    constructor(private readonly dialogStore: DialogStore, private readonly messagingStore: MessagingStore, private readonly sharedPreferences: SharedPreferencesStore) {
        makeAutoObservable(this, undefined, { autoBind: true });
        this.openDisclaimerDialog();
    }

    public get setValue() {
        return computedFn((inputType: RequestCodeKey) => (value: string) => (this[inputType] = value));
    }

    public get setInputRef() {
        return computedFn((inputType: RequestCodeKey) => (ref: NativeTextInput) => (this[`${inputType}Ref`] = ref));
    }

    public get emailError() {
        if (typeof this.email === 'undefined') {
            return undefined;
        }
        if (!this.email.length) {
            return i18n.t('activationCode:emptyEmail') as string;
        }
        if (!/^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(this.email)) {
            return i18n.t('activationCode:invalidEmail') as string;
        }
        return undefined;
    }

    public get nameError() {
        if (typeof this.name === 'undefined') {
            return undefined;
        }
        if (!this.name.trim().length) {
            return i18n.t('contact:emptyName') as string;
        }
        return undefined;
    }

    public get infoError() {
        if (typeof this.info === 'undefined') {
            return undefined;
        }
        if (!this.info.trim().length) {
            return i18n.t('contact:emptyMsg') as string;
        }
        return undefined;
    }

    public get isNotFulfilled() {
        return this.isPerformingAction || this.messagingStore.token.state !== 'fulfilled';
    }

    public async askForSendingApproval() {
        const { email, emailError, name, nameError, info, infoError } = this;
        if (!email || emailError || !name || nameError || !info || infoError) {
            return;
        }
        const trimmedEmail = email.trim();
        const trimmedName = name.trim();
        const trimmedInfo = info.trim();

        const shouldSend = await this.dialogStore.openDialog(RequestCodeApproval, { email: trimmedEmail, info: trimmedInfo, name: trimmedName });
        if (!shouldSend) {
            return;
        }
        runInAction(() => (this.isPerformingAction = true));
        try {
            const token = await this.messagingStore.token;
            const root = database().ref('codes');
            const newRef = root.push();
            const codeLeaf = {
                token,
                used: false,
                allowed: false,
                time: Date.now(),
                name: trimmedName,
                email: trimmedEmail,
                reason: trimmedInfo,
                code: randomString(4, '0123456789'),
            };
            await newRef.update(codeLeaf);
            const dialogPromise = this.dialogStore.openDialog(BaseDialog, {
                blockBackBtn: true,
                blockBackdropPress: true,
                okText: i18n.t('common:ok'),
                description: i18n.t('activationCode:requestCodeSent'),
            });
            await Promise.all([dialogPromise, this.sharedPreferences.setItemOnChannel('Settings', 'firebaseGenratedKey', newRef.key!)]);
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
            await this.dialogStore.openDialog(BaseDialog, {
                blockBackBtn: true,
                blockBackdropPress: true,
                okText: i18n.t('common:ok'),
                title: i18n.t('common:error'),
                description: i18n.t('activationCode:requestCodeSent'),
            });
        } finally {
            runInAction(() => (this.isPerformingAction = false));
        }
    }

    public onSubmitEditing(_: NativeSyntheticEvent<TextInputSubmitEditingEventData>) {
        const { infoRef, nameRef, emailRef } = this;
        if (emailRef?.isFocused()) {
            nameRef?.focus();
            return;
        }
        if (nameRef?.isFocused()) {
            infoRef?.focus();
            return;
        }
        if (infoRef?.isFocused()) {
            infoRef.blur();
        }
    }

    private openDisclaimerDialog() {
        return this.dialogStore.openDialog(BaseDialog, {
            title: i18n.t('contact:plsNote'),
            description: i18n.t('activationCode:requestCodeExplanation'),
            okText: i18n.t('common:ok'),
            blockBackBtn: true,
            blockBackdropPress: true,
        });
    }
}

const RequestCodeFooter = observer<{ localStore: RequestCodeLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <View style={styles.pageFooter}>
            <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.sendBtn} wrapperStyle={styles.sendBtnWrapper} onPress={localStore.askForSendingApproval}>
                <Text style={styles.sendText}>{t('common:send')}</Text>
                <Icon size={24} color={colors.white} name="send" />
            </PressableRippleWrapper>
        </View>
    );
});
RequestCodeFooter.displayName = 'RequestCodeFooter';

export const RequestCode = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const localStore = useLocalStoreWithInjectedDependencies(RequestCodeLocalStore);

    if (localStore.isNotFulfilled) {
        return (
            <BasePageView scrollType="none">
                {/* {messagingStore.token.state === 'rejected' ? (
                    <ConnectionError />
                ) : ( */}
                <View style={styles.loadingWrapper}>
                    <Loader />
                </View>
                {/* )} */}
            </BasePageView>
        );
    }

    return (
        <BasePageView scrollContentStyle={styles.scrollContent} postScroll={<RequestCodeFooter localStore={localStore} />}>
            <Text style={styles.text}>{t('activationCode:requestCodeExplanation')}</Text>
            <TextInput
                returnKeyType="next"
                error={localStore.emailError}
                ref={localStore.setInputRef('email')}
                onChangeText={localStore.setValue('email')}
                onSubmitEditing={localStore.onSubmitEditing}
                label={t('contact:from')}
                keyboardType="email-address"
                textContentType="emailAddress"
            />
            <TextInput
                returnKeyType="next"
                error={localStore.nameError}
                ref={localStore.setInputRef('name')}
                onChangeText={localStore.setValue('name')}
                onSubmitEditing={localStore.onSubmitEditing}
                label={t('contact:name')}
                textContentType="name"
            />
            <TextInput
                returnKeyType="done"
                error={localStore.infoError}
                ref={localStore.setInputRef('info')}
                onChangeText={localStore.setValue('info')}
                onSubmitEditing={localStore.onSubmitEditing}
                label={t('activationCode:requestCodeInfoHint')}
                numberOfLines={11}
            />
        </BasePageView>
    );
});
RequestCode.displayName = 'RequestCode';

const styles = StyleSheet.create({
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
        fontSize: 18,
        marginBottom: 10,
        textAlign: 'center',
        color: colors.materialGrey600,
    },
    pageFooter: {
        flexShrink: 0,
        marginBottom: 10,
        paddingHorizontal: 15,
    },
    sendText: {
        flex: 1,
        fontSize: 20,
        textAlign: 'center',
        color: colors.white,
    },
    sendBtnWrapper: {
        elevation: 4,
        borderRadius: 15,
    },
    sendBtn: {
        width: '100%',
        height: 60,
        overflow: 'hidden',
        alignItems: 'center',
        flexDirection: 'row',
        paddingHorizontal: 15,
        justifyContent: 'center',
        backgroundColor: colors.colorNice,
    },
});
