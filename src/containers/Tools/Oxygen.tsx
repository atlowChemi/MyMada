import { useEffect } from 'react';
import { injectable } from 'inversify';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { Dropdown } from 'react-native-element-dropdown';
import { action, computed, makeObservable, observable } from 'mobx';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { TextInput } from '../../Components/TextInput';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';

const LITER_PER_MINUTE = 10;
type OxygenTankSize = 2.4 | 20;

@injectable()
class OxygenCalculatorLocalStore {
    public tankSize: OxygenTankSize = 2.4;
    private _pressureATM: number | undefined = 100;
    private _pressurePSI: number | undefined = 1500;

    constructor() {
        makeObservable<OxygenCalculatorLocalStore, '_pressureATM' | '_pressurePSI'>(this, {
            minutesLeft: computed,
            pressureATM: computed,
            pressurePSI: computed,
            tankSize: observable.ref,
            _pressureATM: observable.ref,
            _pressurePSI: observable.ref,
            setAtmPressure: action.bound,
            setPsiPressure: action.bound,
            setOxygenTankType: action.bound,
        });
    }

    public get oxygenTankOptions(): { label: string; value: OxygenTankSize }[] {
        return [
            { label: i18n.t('tools:oxygen:sizes:twoDot4'), value: 2.4 },
            { label: i18n.t('tools:oxygen:sizes:twenty'), value: 20 },
        ];
    }

    public get pressureATM() {
        if (!this._pressureATM || Number.isNaN(this._pressureATM)) {
            return '';
        }
        return `${this._pressureATM}`;
    }

    public get pressurePSI() {
        if (!this._pressurePSI || Number.isNaN(this._pressurePSI)) {
            return '';
        }
        return `${this._pressurePSI}`;
    }

    public get minutesLeft() {
        if (!this._pressureATM || Number.isNaN(this._pressureATM)) {
            return '0';
        }
        const { _pressureATM: pressure, tankSize } = this;
        return `${Math.floor((pressure * tankSize) / LITER_PER_MINUTE)}`;
    }

    public setAtmPressure(value: string) {
        const numeralValue = value.replace(/\D/g, '');
        const pulse = parseInt(numeralValue, 10);
        this._pressureATM = pulse;
        this._pressurePSI = pulse * 15;
    }

    public setPsiPressure(value: string) {
        const numeralValue = value.replace(/\D/g, '');
        const pulse = parseInt(numeralValue, 10);
        this._pressureATM = Math.round(pulse / 15);
        this._pressurePSI = pulse;
    }

    public setOxygenTankType({ value }: (typeof this)['oxygenTankOptions'][number]) {
        this.tankSize = value;
    }
}

export const OxygenCalculator = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalStoreWithInjectedDependencies(OxygenCalculatorLocalStore);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    return (
        <BasePageView
            postScroll={
                <View style={styles.bottomSection}>
                    <Text style={styles.time}>{localStore.minutesLeft}</Text>
                    <Text style={styles.timeUnit}>{t('tools:oxygen:minLeft')}</Text>
                    <Text style={styles.lpmMsg}>{t('tools:oxygen:clarify')}</Text>
                </View>
            }>
            <Dropdown
                data={localStore.oxygenTankOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.tankSize as unknown as string}
                onChange={localStore.setOxygenTankType}
            />
            <TextInput
                maxLength={3}
                wrapperStyle={styles.field}
                label={t('tools:oxygen:atm')}
                inputMode="numeric"
                keyboardType="numeric"
                onChangeText={localStore.setAtmPressure}
                value={localStore.pressureATM}
            />
            <TextInput
                maxLength={5}
                wrapperStyle={styles.field}
                label={t('tools:oxygen:psi')}
                inputMode="numeric"
                keyboardType="numeric"
                onChangeText={localStore.setPsiPressure}
                value={localStore.pressurePSI}
            />
        </BasePageView>
    );
});
OxygenCalculator.displayName = 'OxygenCalculator';

const styles = StyleSheet.create({
    dropdown: {
        marginHorizontal: 10,
        borderBottomWidth: 0.5,
        borderColor: colors.buttonMaterialDark,
    },
    field: {
        marginBottom: 0,
    },
    bottomSection: {
        height: 165,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: colors.buttonMaterialDark,
    },
    time: {
        color: colors.white,
        fontSize: 50,
        paddingBottom: 10,
    },
    timeUnit: {
        color: colors.white,
        fontSize: 25,
    },
    lpmMsg: {
        color: '#cfcfcf',
        fontSize: 15,
    },
});
