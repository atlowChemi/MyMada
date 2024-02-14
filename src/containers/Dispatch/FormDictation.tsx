import { runInAction } from 'mobx';
import { computedFn } from 'mobx-utils';
import { Pressable, StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon } from '../../Components/Icon';
import { TextInput } from '../../Components/TextInput';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { DispatchService } from '../../services/DispatchService';
import { MedicalCodePicker } from '../dialogs/MedicalCodePicker';
import { useDependencyInjector } from '../../services/container';
import { PressableRippleWrapper } from '../../Components/PressableRippleWrapper';
import type { TextInput as NativeTextInput, NativeSyntheticEvent, TextInputSubmitEditingEventData } from 'react-native';
import type { NavigatableDispatchScreenProps } from '../../containers/Pages/Dispatch';

const FormDictationFooter = observer<{ onSend(): void }>(({ onSend }) => (
    <View style={styles.pageFooter}>
        <PressableRippleWrapper android_ripple={{ color: colors.ocean, borderless: false }} style={styles.sendBtn} wrapperStyle={styles.sendBtnWrapper} onPress={onSend}>
            <Text style={styles.sendText}>{i18n.t('common:send')}</Text>
            <Icon size={24} color={colors.white} name="send" />
        </PressableRippleWrapper>
    </View>
));
FormDictationFooter.displayName = 'FormDictationFooter';

type FormKeys = 'ambulanceNumber' | 'visa' | 'address' | 'formNumber' | 'billNumber' | 'paymentSum' | 'firstName' | 'lastName' | 'medicalCode';

export const FormDictation = observer<NavigatableDispatchScreenProps>(() => {
    const dialogStore = useDependencyInjector(DialogStore);
    const dispatchService = useDependencyInjector(DispatchService);
    const localStore = useLocalObservable(() => ({
        selectedCodes: undefined as Set<string> | undefined,
        validateErrors: false,
        values: {} as Record<FormKeys, string | undefined>,
        refs: {} as Record<FormKeys, NativeTextInput | undefined>,
        get medicalCodeValue() {
            return Array.from(this.selectedCodes ?? []).join(', ');
        },
        get medicalCodeError() {
            return this.validateErrors && !this.selectedCodes?.size ? i18n.t('dispatch:form:error:medicalCode') : '';
        },
        get setRef() {
            return computedFn((key: FormKeys) => (ref: NativeTextInput) => {
                this.refs[key] = ref;
            });
        },
        get setValue() {
            return computedFn((key: FormKeys) => (value: string) => {
                this.values[key] = value;
            });
        },
        get errorMsg() {
            return computedFn((key: FormKeys) => {
                if (this.validateErrors && !this.values[key] && this.errorMessageSpecialCases(key)) {
                    return i18n.t(`dispatch:form:error:${key}`);
                }
                return '';
            });
        },
        errorMessageSpecialCases(key: FormKeys) {
            if (key === 'billNumber') {
                return Boolean(this.values.paymentSum);
            }
            if (key === 'paymentSum') {
                return Boolean(this.values.billNumber);
            }
            return true;
        },
        onSend() {
            this.validateErrors = true;
            if (!this.values.ambulanceNumber?.trim() || !this.values.formNumber?.trim() || !this.values.firstName?.trim() || !this.values.lastName?.trim() || !this.selectedCodes?.size) {
                return;
            }
            if ((this.values.billNumber?.trim() && !this.values.paymentSum?.trim()) || (this.values.paymentSum?.trim() && !this.values.billNumber?.trim())) {
                return;
            }
            let msg = `אמב: ${this.values.ambulanceNumber}`;
            if (this.values.visa?.trim()) {
                msg += `\nויזה: ${this.values.visa.trim()}`;
            }
            if (this.values.address?.trim()) {
                msg += `\nכתובת: ${this.values.address.trim()}`;
            }
            msg += `\nחולה נפגע: ${this.values.formNumber.trim()}`;
            if (this.values.billNumber?.trim() && this.values.paymentSum?.trim()) {
                msg += `\nהתחייבות: ${this.values.billNumber.trim()}\nסכום: ${this.values.paymentSum.trim()}`;
            }
            msg += `\nקוד: ${this.medicalCodeValue}`;
            msg += `\nשם: ${this.values.firstName.trim()} ${this.values.lastName.trim()}`;
            return dispatchService.sendSms(msg);
        },
        onSubmitEditing(e: NativeSyntheticEvent<TextInputSubmitEditingEventData>) {
            e.preventDefault();
            const { ambulanceNumber, visa, address, formNumber, billNumber, paymentSum, firstName, lastName } = this.refs;
            if (ambulanceNumber?.isFocused()) {
                return visa?.focus();
            }
            if (visa?.isFocused()) {
                return address?.focus();
            }
            if (address?.isFocused()) {
                return formNumber?.focus();
            }
            if (formNumber?.isFocused()) {
                return billNumber?.focus();
            }
            if (billNumber?.isFocused()) {
                return paymentSum?.focus();
            }
            if (paymentSum?.isFocused()) {
                return firstName?.focus();
            }
            if (firstName?.isFocused()) {
                return lastName?.focus();
            }
            if (lastName?.isFocused()) {
                lastName?.blur();
                return this.pickMedicalCode();
            }
        },
        async pickMedicalCode() {
            const codes = await dialogStore.openDialog(MedicalCodePicker, { selectedCodes: this.selectedCodes });
            if (!codes) {
                return;
            }
            runInAction(() => (this.selectedCodes = codes));
        },
    }));
    return (
        <BasePageView scrollContentStyle={styles.scrollContent} postScroll={<FormDictationFooter onSend={localStore.onSend} />}>
            <TextInput
                maxLength={4}
                returnKeyType="next"
                keyboardType="number-pad"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.ambulanceNumber}
                ref={localStore.setRef('ambulanceNumber')}
                onSubmitEditing={localStore.onSubmitEditing}
                error={localStore.errorMsg('ambulanceNumber')}
                label={i18n.t('dispatch:form:ambulanceNumber')}
                onChangeText={localStore.setValue('ambulanceNumber')}
            />
            <TextInput
                maxLength={8}
                returnKeyType="next"
                keyboardType="number-pad"
                value={localStore.values.visa}
                ref={localStore.setRef('visa')}
                wrapperStyle={styles.inputWrapper}
                label={i18n.t('dispatch:form:visa')}
                onChangeText={localStore.setValue('visa')}
                onSubmitEditing={localStore.onSubmitEditing}
            />
            <TextInput
                returnKeyType="next"
                value={localStore.values.address}
                ref={localStore.setRef('address')}
                wrapperStyle={styles.inputWrapper}
                textContentType="fullStreetAddress"
                label={i18n.t('dispatch:form:address')}
                onChangeText={localStore.setValue('address')}
                onSubmitEditing={localStore.onSubmitEditing}
            />
            <TextInput
                returnKeyType="next"
                keyboardType="number-pad"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.formNumber}
                ref={localStore.setRef('formNumber')}
                error={localStore.errorMsg('formNumber')}
                label={i18n.t('dispatch:form:formNumber')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setValue('formNumber')}
            />
            <TextInput
                returnKeyType="next"
                keyboardType="number-pad"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.billNumber}
                ref={localStore.setRef('billNumber')}
                error={localStore.errorMsg('billNumber')}
                label={i18n.t('dispatch:form:billNumber')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setValue('billNumber')}
            />
            <TextInput
                maxLength={4}
                returnKeyType="next"
                keyboardType="number-pad"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.paymentSum}
                ref={localStore.setRef('paymentSum')}
                error={localStore.errorMsg('paymentSum')}
                label={i18n.t('dispatch:form:paymentSum')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setValue('paymentSum')}
            />
            <TextInput
                returnKeyType="next"
                textContentType="givenName"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.firstName}
                ref={localStore.setRef('firstName')}
                error={localStore.errorMsg('firstName')}
                label={i18n.t('dispatch:form:firstName')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setValue('firstName')}
            />
            <TextInput
                returnKeyType="next"
                textContentType="familyName"
                wrapperStyle={styles.inputWrapper}
                value={localStore.values.lastName}
                ref={localStore.setRef('lastName')}
                error={localStore.errorMsg('lastName')}
                label={i18n.t('dispatch:form:lastName')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setValue('lastName')}
            />
            <Pressable onPress={localStore.pickMedicalCode}>
                <View pointerEvents="none">
                    <TextInput focusable={false} showSoftInputOnFocus={false} value={localStore.medicalCodeValue} error={localStore.medicalCodeError} label={i18n.t('dispatch:form:medicalCode')} />
                </View>
            </Pressable>
        </BasePageView>
    );
});
FormDictation.displayName = 'FormDictation';

const styles = StyleSheet.create({
    scrollContent: {
        flex: undefined,
        paddingBottom: 10,
        rowGap: 10,
    },
    inputWrapper: {
        marginBottom: 0,
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
