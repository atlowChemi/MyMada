import database from '@react-native-firebase/database';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { injectable } from 'inversify';
import { computedFn } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { makeObservable, observable, runInAction } from 'mobx';
import { GestureResponderEvent, Pressable, StyleSheet, TextInput, View } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BaseDialog } from '../dialogs/BaseDialog';
import { PageLoader } from '../../Components/Loader';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';
import { AppPropsStore } from '../../services/AppPropsStore';

@injectable()
class CodeActivationLocalStore {
    public isRunningAction = false;
    public readonly chars: string[] = [];
    private readonly inputRefs: TextInput[] = [];

    constructor(
        private readonly appPropsStore: AppPropsStore,
        private readonly dialogStore: DialogStore,
        private readonly navigationStore: NavigationStore,
        private readonly sharedPreferences: SharedPreferencesStore,
    ) {
        makeObservable(this, { chars: observable.shallow, isRunningAction: observable });
        const { code, firebaseKey: key } = this.appPropsStore.props ?? {};
        if (typeof code === 'string' && code) {
            code.split('').forEach((v, i) => (this.chars[i] = v));
            this.apply(key);
        }
    }

    public get setInputRef() {
        return computedFn((index: number) => (ref: TextInput) => (this.inputRefs[index] = ref));
    }
    public get setValue() {
        return computedFn((index: number) => (value: string) => {
            runInAction(() => (this.chars[index] = value.at(-1) || ''));
            const nextIndex = value.length ? index + 1 : index - 1;
            if (nextIndex < 0) {
                return;
            }
            if (nextIndex >= this.inputRefs.length) {
                return;
            }
            this.inputRefs[nextIndex].focus();
        });
    }
    public get onKeyPress() {
        return computedFn((index: number) => ({ nativeEvent }: { nativeEvent: { key: string } }) => {
            if (nativeEvent.key !== 'Backspace' || this.chars[index]) {
                return;
            }
            const prevIndex = index - 1;
            if (prevIndex < 0) {
                return;
            }
            this.inputRefs[prevIndex].focus();
        });
    }

    public apply = async (key: string | GestureResponderEvent = '') => {
        if (this.chars.length !== 4 || !this.chars.every(Boolean)) {
            return this.dialogStore.openDialog(BaseDialog, { title: i18n.t('common:error'), description: i18n.t('activationCode:partialCode'), okText: i18n.t('common:ok') });
        }
        if (typeof key !== 'string' || !key) {
            const {
                general: { firebaseGeneratedKey },
            } = await this.sharedPreferences.applicationSettings;
            key = firebaseGeneratedKey;
            if (!key) {
                return this.dialogStore.openDialog(BaseDialog, { title: i18n.t('common:error'), description: i18n.t('activationCode:nonRequested'), okText: i18n.t('common:ok') });
            }
        }
        runInAction(() => (this.isRunningAction = true));
        const insertedCode = this.chars.join('');
        try {
            const element = database().ref('codes').child(key);
            crashlytics().log(`CodeActivationLocalStore: getting element with key ${key}`);
            const snapshot = await element.once('value');
            const { code, allowed = false, used = true } = snapshot.val() ?? {};
            crashlytics().log(`CodeActivationLocalStore: got element with key ${key} and code ${code}`);
            if (insertedCode !== code) {
                return this.dialogStore.openDialog(BaseDialog, { title: i18n.t('common:error'), description: i18n.t('activationCode:wrongCode'), okText: i18n.t('common:ok') });
            }
            if (used) {
                return this.dialogStore.openDialog(BaseDialog, { title: i18n.t('common:error'), description: i18n.t('activationCode:usedCode', { code }), okText: i18n.t('common:ok') });
            }
            if (!allowed) {
                return this.dialogStore.openDialog(BaseDialog, { title: i18n.t('common:error'), description: i18n.t('activationCode:notAllowed', { code }), okText: i18n.t('common:ok') });
            }
            await Promise.all([element.update({ used: true }), this.sharedPreferences.setItem('realM', 'true')]);
            crashlytics().log('CodeActivationLocalStore: code activated');
            await this.dialogStore.openDialog(BaseDialog, { description: i18n.t('activationCode:codeActivated'), okText: i18n.t('common:ok'), blockBackBtn: true, blockBackdropPress: true });
            this.navigationStore.navigateWithoutHistory('Home');
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
        } finally {
            runInAction(() => (this.isRunningAction = false));
        }
    };
}

export const CodeActivation = observer<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const localStore = useLocalStoreWithInjectedDependencies(CodeActivationLocalStore);

    if (localStore.isRunningAction) {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollType="none">
            <View style={styles.wrapper}>
                <View style={[styles.inputWrapper, i18n.language === 'he' && styles.reversed]}>
                    {Array.from({ length: 4 }).map((_, index) => (
                        <TextInput
                            key={index}
                            style={styles.input}
                            keyboardType="number-pad"
                            value={localStore.chars[index]}
                            ref={localStore.setInputRef(index)}
                            onChangeText={localStore.setValue(index)}
                            onKeyPress={localStore.onKeyPress(index)}
                            underlineColorAndroid={colors.buttonMaterialDark}
                        />
                    ))}
                </View>
                <Pressable style={styles.applyBtn} android_ripple={{ color: colors.colorBlueAlpha, borderless: false, foreground: true }} onPress={localStore.apply}>
                    <Text style={styles.applyBtnTxt}>{t('common:ok')}</Text>
                </Pressable>
            </View>
        </BasePageView>
    );
});
CodeActivation.displayName = 'CodeActivation';

const styles = StyleSheet.create({
    wrapper: {
        flex: 1,
        margin: 20,
        alignItems: 'center',
        justifyContent: 'center',
    },
    inputWrapper: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
    },
    reversed: {
        flexDirection: 'row-reverse',
    },
    input: {
        flex: 1,
        fontSize: 14,
        textAlign: 'center',
        textAlignVertical: 'top',
        fontFamily: 'opensanshebrew',
    },
    applyBtn: {
        height: 40,
        width: '100%',
        borderRadius: 15,
        overflow: 'hidden',
        alignItems: 'center',
        justifyContent: 'center',
    },
    applyBtnTxt: {
        fontSize: 20,
        color: colors.colorAqua,
    },
});
