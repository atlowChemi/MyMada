import { injectable } from 'inversify';
import { computedFn, fromPromise } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { makeAutoObservable } from 'mobx';
import i18n from '../../localization';
import database from '@react-native-firebase/database';
import { colors } from '../../consts';
import { inRange } from '../../utils';
import { Text } from '../../Components/Text';
import { InfoCard } from '../../Components/InfoCard';
import { TextInput } from '../../Components/TextInput';
import { BasePageView } from '../../Components/BasePageView';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { RadioButtonGroup } from '../../Components/RadioButtonGroup';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NativeSyntheticEvent, TextInput as NativeTextInput, TextInputSubmitEditingEventData } from 'react-native';
import type { NavigatableToolsScreenProps } from '../Pages/Tools';

const VITAL_CATEGORIES = ['adult', 'child', 'baby'] as const;
type VitalCategory = (typeof VITAL_CATEGORIES)[number];
type Range = [number, number];
interface VitalsRange {
    pulse: Range;
    breath: Range;
    systolic: Range;
    diastolic: Range;
}
const STATIC_VITALS: Record<VitalCategory, VitalsRange> = {
    adult: {
        pulse: [60, 100],
        breath: [12, 20],
        systolic: [90, 140],
        diastolic: [60, 90],
    },
    child: {
        pulse: [90, 120],
        breath: [18, 30],
        systolic: [90, 110],
        diastolic: [50, 80],
    },
    baby: {
        pulse: [126, 130],
        breath: [22, 60],
        systolic: [60, 90],
        diastolic: [40, 60],
    },
};

@injectable()
class VitalsCalculatorLocalStore {
    public category: VitalCategory = 'adult';
    public pulse = '';
    public breath = '';
    public systolic = '';
    public diastolic = '';
    private pulseRef?: NativeTextInput;
    private breathRef?: NativeTextInput;
    private systolicRef?: NativeTextInput;
    private diastolicRef?: NativeTextInput;
    constructor() {
        makeAutoObservable(this, {}, { autoBind: true });
    }

    public get options() {
        return {
            [i18n.t('tools:vitals:ageRange:adult')]: VITAL_CATEGORIES[0],
            [i18n.t('tools:vitals:ageRange:child')]: VITAL_CATEGORIES[1],
            [i18n.t('tools:vitals:ageRange:baby')]: VITAL_CATEGORIES[2],
        } satisfies { [label: string]: VitalCategory };
    }

    public get vitalsRange() {
        const { pulse, breath, systolic, diastolic } = this.relevantVitals.case({
            rejected: () => STATIC_VITALS[this.category],
            pending: () => STATIC_VITALS[this.category],
            fulfilled: vitals => vitals,
        });
        return {
            ageRange: i18n.t(`tools:vitals:ageRange:${this.category}`),
            pulseLow: pulse[0],
            pulseHigh: pulse[1],
            breathLow: breath[0],
            breathHigh: breath[1],
            systolicLow: systolic[0],
            systolicHigh: systolic[1],
            diastolicLow: diastolic[0],
            diastolicHigh: diastolic[1],
        };
    }

    public get setNumeralValue() {
        return computedFn((inputType: keyof VitalsRange) => (value: string) => {
            const numeralStringValue = value.replace(/\D/g, '');
            if (!numeralStringValue.length) {
                this[inputType] = '';
                return;
            }
            const numeralValue = parseInt(numeralStringValue, 10);
            if (Number.isNaN(numeralValue)) {
                return;
            }
            this[inputType] = numeralValue.toString();
            if (inputType === 'systolic' && (value.length === 3 || (value.length === 2 && !value.startsWith('1')))) {
                this.diastolicRef?.focus();
            }
        });
    }

    public get setInputRef() {
        return computedFn((inputType: keyof VitalsRange) => (ref: NativeTextInput) => {
            this[`${inputType}Ref`] = ref;
        });
    }

    public get inputUnderlineColor() {
        return computedFn((inputType: keyof VitalsRange) => {
            const { [inputType]: input, calculatedVitals } = this;
            if (!input) {
                return undefined;
            }
            return calculatedVitals[inputType] ? colors.calcG : colors.oldPrimaryDark;
        });
    }

    private get relevantVitals() {
        return fromPromise(this.getVitalsRootDocument(), fromPromise.resolve(STATIC_VITALS[this.category]));
    }

    private get calculatedVitals(): Record<keyof VitalsRange, boolean> {
        const { pulse: pulseNum, breath: breathNum, systolic: systolicNum, diastolic: diastolicNum } = this;
        const { pulseLow, pulseHigh, breathLow, breathHigh, systolicLow, systolicHigh, diastolicLow, diastolicHigh } = this.vitalsRange;
        const pulse = Boolean(pulseNum) && inRange(+pulseNum, pulseLow, pulseHigh);
        const breath = Boolean(breathNum) && inRange(+breathNum, breathLow, breathHigh);
        const systolic = Boolean(systolicNum) && inRange(+systolicNum, systolicLow, systolicHigh);
        const diastolic = Boolean(diastolicNum) && inRange(+diastolicNum, diastolicLow, diastolicHigh);
        return { pulse, breath, systolic, diastolic };
    }

    public setCategory(category: VitalCategory) {
        this.category = category;
    }

    public onSubmitEditing(_: NativeSyntheticEvent<TextInputSubmitEditingEventData>) {
        const { pulseRef, breathRef, systolicRef, diastolicRef } = this;
        if (pulseRef?.isFocused()) {
            breathRef?.focus();
            return;
        }
        if (breathRef?.isFocused()) {
            systolicRef?.focus();
            return;
        }
        if (systolicRef?.isFocused()) {
            diastolicRef?.focus();
        }
    }

    private async getVitalsRootDocument(): Promise<VitalsRange> {
        const vitals = await database().ref('Vitals').once('value');
        const [systolic, diastolic, breathing, pulse] = [
            vitals.child('BloodPressure').child('Systolic'),
            vitals.child('BloodPressure').child('Diastolic'),
            vitals.child('Breathing'),
            vitals.child('Pulse'),
        ];
        const categoryCapitalized = this.category === 'adult' ? 'Old' : `${this.category[0].toUpperCase()}${this.category.slice(1)}`;
        const categoryHigh = `${categoryCapitalized}_high`;
        const categoryLow = `${categoryCapitalized}_low`;
        return {
            systolic: [systolic.child(categoryLow).val(), systolic.child(categoryHigh).val()],
            diastolic: [diastolic.child(categoryLow).val(), diastolic.child(categoryHigh).val()],
            breath: [breathing.child(categoryLow).val(), breathing.child(categoryHigh).val()],
            pulse: [pulse.child(categoryLow).val(), pulse.child(categoryHigh).val()],
        };
    }
}

export const VitalsCalculator = observer<NavigatableToolsScreenProps>(props => {
    useInnerNavigator('Tools', props);
    const { t } = useTranslation();
    const localStore = useLocalStoreWithInjectedDependencies(VitalsCalculatorLocalStore);

    // Handle landscape orientation
    return (
        <BasePageView
            scrollType="scroll"
            scrollContentStyle={styles.wrapperStyles}
            postScroll={
                <>
                    <InfoCard text={t('tools:vitals:msg', localStore.vitalsRange)} />
                    <InfoCard text={t('tools:vitals:disclaimer')} />
                </>
            }>
            <RadioButtonGroup options={localStore.options} value={localStore.category} onChange={localStore.setCategory} />
            <TextInput
                maxLength={3}
                blurOnSubmit={false}
                returnKeyType="next"
                keyboardType="numeric"
                value={localStore.pulse}
                label={t('tools:vitals:pulse')}
                ref={localStore.setInputRef('pulse')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setNumeralValue('pulse')}
                underlineColorAndroid={localStore.inputUnderlineColor('pulse')}
            />
            <TextInput
                maxLength={3}
                blurOnSubmit={false}
                returnKeyType="next"
                keyboardType="numeric"
                value={localStore.breath}
                label={t('tools:vitals:breathing')}
                ref={localStore.setInputRef('breath')}
                onSubmitEditing={localStore.onSubmitEditing}
                onChangeText={localStore.setNumeralValue('breath')}
                underlineColorAndroid={localStore.inputUnderlineColor('breath')}
            />
            <View style={styles.bloodPressureWrapper}>
                <TextInput
                    maxLength={3}
                    blurOnSubmit={false}
                    returnKeyType="next"
                    keyboardType="numeric"
                    value={localStore.systolic}
                    label={t('tools:vitals:systolic')}
                    ref={localStore.setInputRef('systolic')}
                    wrapperStyle={styles.bloodPressureInput}
                    onSubmitEditing={localStore.onSubmitEditing}
                    onChangeText={localStore.setNumeralValue('systolic')}
                    underlineColorAndroid={localStore.inputUnderlineColor('systolic')}
                />
                <Text>/</Text>
                <TextInput
                    maxLength={3}
                    keyboardType="numeric"
                    value={localStore.diastolic}
                    label={t('tools:vitals:diastolic')}
                    wrapperStyle={styles.bloodPressureInput}
                    ref={localStore.setInputRef('diastolic')}
                    onChangeText={localStore.setNumeralValue('diastolic')}
                    underlineColorAndroid={localStore.inputUnderlineColor('diastolic')}
                />
            </View>
        </BasePageView>
    );
});
VitalsCalculator.displayName = 'Tools.VitalsCalculator';

const styles = StyleSheet.create({
    wrapperStyles: {
        rowGap: 0,
    },
    bloodPressureWrapper: {
        flexDirection: 'row-reverse',
        alignItems: 'center',
    },
    bloodPressureInput: {
        flexGrow: 1,
        flexShrink: 0,
    },
});
