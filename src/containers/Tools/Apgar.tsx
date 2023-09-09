import { useEffect } from 'react';
import { injectable } from 'inversify';
import { computedFn } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { Animated, Image, StyleSheet, View } from 'react-native';
import { action, computed, makeObservable, observable } from 'mobx';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import { useColorAnimation } from '../../utils/colorAnimation';
import type { NavigatableScreenProps } from '../../App';
import { ToolsDropDown } from '../../Components/ToolsDropdown';

const APGAR_PARAMS = ['appearance', 'pulse', 'grimace', 'activity', 'respiration'] as const;
type ApgarParam = (typeof APGAR_PARAMS)[number];

@injectable()
class ApgarCalculatorLocalStore {
    public appearance = 0;
    public pulse = 0;
    public grimace = 0;
    public activity = 0;
    public respiration = 0;

    constructor() {
        makeObservable(this, {
            score: computed,
            scoreColor: computed,
            pulse: observable.ref,
            grimace: observable.ref,
            activity: observable.ref,
            appearance: observable.ref,
            respiration: observable.ref,
            setPulseValue: action.bound,
            setGrimaceValue: action.bound,
            setActivityValue: action.bound,
            setAppearanceValue: action.bound,
            setRespirationValue: action.bound,
        });
    }

    public get score() {
        return this.appearance + this.pulse + this.grimace + this.activity + this.respiration;
    }

    public get scoreColor() {
        if (this.score > 6) {
            return colors.calcG;
        }
        if (this.score > 3) {
            return colors.calcM;
        }
        return colors.calcB;
    }

    public get options() {
        return computedFn((kind: ApgarParam) => {
            return Array.from({ length: 3 }, (_, i) => ({ label: i18n.t(`tools:apgar:${kind}:options:${i}`) as string, value: i }));
        });
    }

    public setAppearanceValue({ value }: ReturnType<this['options']>[number]) {
        this.appearance = value;
    }

    public setPulseValue({ value }: ReturnType<this['options']>[number]) {
        this.pulse = value;
    }

    public setGrimaceValue({ value }: ReturnType<this['options']>[number]) {
        this.grimace = value;
    }

    public setActivityValue({ value }: ReturnType<this['options']>[number]) {
        this.activity = value;
    }

    public setRespirationValue({ value }: ReturnType<this['options']>[number]) {
        this.respiration = value;
    }
}

export const ApgarCalculator = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalStoreWithInjectedDependencies(ApgarCalculatorLocalStore);
    const [backgroundColor] = useColorAnimation(localStore.scoreColor);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    return (
        <BasePageView
            postScroll={
                <>
                    <Animated.View style={[styles.score, { backgroundColor }]}>
                        <Text style={styles.scoreTxt}>{t('tools:apgar:score', { score: localStore.score })}</Text>
                    </Animated.View>
                    <View style={styles.info}>
                        <Image style={styles.infoIcon} source={require('../../assets/icons/info.webp')} />
                        <View style={styles.border} />
                        <Text style={styles.infoTxt}>{t('tools:apgar:info')}</Text>
                    </View>
                </>
            }>
            {APGAR_PARAMS.map(kind => {
                const upperCased = (kind[0].toUpperCase() + kind.slice(1)) as 'Appearance' | 'Pulse' | 'Grimace' | 'Activity' | 'Respiration';
                const onChange = localStore[`set${upperCased}Value`];
                return <ToolsDropDown key={kind} title={t(`tools:apgar:${kind}:title`)} options={localStore.options(kind)} value={localStore[kind]} onChange={onChange} />;
            })}
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    dropdownTitle: {
        fontSize: 21,
        fontWeight: 'bold',
        textAlign: 'center',
        color: colors.colorAqua,
    },
    dropdown: {
        marginHorizontal: 10,
        borderBottomWidth: 0.5,
        borderColor: colors.buttonMaterialDark,
    },
    score: {
        padding: 14,
        elevation: 1,
        marginBottom: 8,
        borderRadius: 15,
        marginHorizontal: 10,
        alignItems: 'center',
    },
    scoreTxt: {
        fontSize: 17,
    },
    info: {
        padding: 14,
        elevation: 1,
        paddingStart: 7,
        marginBottom: 8,
        borderRadius: 15,
        marginHorizontal: 10,
        flexDirection: 'row',
        backgroundColor: colors.white,
    },
    infoIcon: {
        height: 24,
        width: 24,
    },
    border: {
        width: 1,
        height: '100%',
        backgroundColor: colors.buttonMaterialDark,
        marginStart: 2,
        marginEnd: 10,
    },
    infoTxt: {
        flexWrap: 'wrap',
        fontSize: 16,
    },
});
