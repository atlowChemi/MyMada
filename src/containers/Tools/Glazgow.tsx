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

const GLAZGOW_PARAMS = ['eyes', 'verbal', 'motor'] as const;
type GlazgowParam = (typeof GLAZGOW_PARAMS)[number];

@injectable()
class GlazgowCalculatorLocalStore {
    public eyes = 1;
    public verbal = 1;
    public motor = 1;

    constructor() {
        makeObservable(this, {
            score: computed,
            scoreColor: computed,
            eyes: observable.ref,
            motor: observable.ref,
            verbal: observable.ref,
            setEyesValue: action.bound,
            setMotorValue: action.bound,
            setVerbalValue: action.bound,
        });
    }

    public get score() {
        return this.eyes + this.verbal + this.motor;
    }

    public get scoreColor() {
        if (this.score > 12) {
            return colors.calcG;
        }
        if (this.score > 8) {
            return colors.calcM;
        }
        return colors.calcB;
    }

    public get scoreDesc() {
        if (this.score > 12) {
            return i18n.t('tools:glazgow:scoreDesc:2');
        }
        if (this.score > 8) {
            return i18n.t('tools:glazgow:scoreDesc:1');
        }
        return i18n.t('tools:glazgow:scoreDesc:0');
    }

    public get options() {
        return computedFn((kind: GlazgowParam) => {
            let length = 4;
            if (kind === 'verbal') {
                length = 5;
            }
            if (kind === 'motor') {
                length = 6;
            }
            return Array.from({ length }, (_, i) => ({ label: i18n.t(`tools:glazgow:${kind}:options:${i}`) as string, value: i + 1 }));
        });
    }

    public setEyesValue({ value }: ReturnType<this['options']>[number]) {
        this.eyes = value;
    }

    public setVerbalValue({ value }: ReturnType<this['options']>[number]) {
        this.verbal = value;
    }

    public setMotorValue({ value }: ReturnType<this['options']>[number]) {
        this.motor = value;
    }
}

export const GlazgowCalculator = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalStoreWithInjectedDependencies(GlazgowCalculatorLocalStore);
    const [backgroundColor] = useColorAnimation(localStore.scoreColor);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    return (
        <BasePageView
            postScroll={
                <>
                    <Animated.View style={[styles.score, { backgroundColor }]}>
                        <Text style={styles.scoreTxt}>{t('tools:glazgow:score', { score: localStore.score, scoreDesc: localStore.scoreDesc })}</Text>
                    </Animated.View>
                    <View style={styles.info}>
                        <Image style={styles.infoIcon} source={require('../../assets/icons/info.webp')} />
                        <View style={styles.border} />
                        <Text style={styles.infoTxt}>{t('tools:glazgow:info')}</Text>
                    </View>
                </>
            }>
            {GLAZGOW_PARAMS.map(kind => {
                const upperCased = (kind[0].toUpperCase() + kind.slice(1)) as 'Eyes' | 'Verbal' | 'Motor';
                const onChange = localStore[`set${upperCased}Value`];
                return <ToolsDropDown key={kind} title={t(`tools:glazgow:${kind}:title`)} options={localStore.options(kind)} value={localStore[kind]} onChange={onChange} />;
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
