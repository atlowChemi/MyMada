import { useEffect } from 'react';
import { injectable } from 'inversify';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { Animated, Image, StyleSheet, View } from 'react-native';
import { Dropdown } from 'react-native-element-dropdown';
import { action, computed, makeObservable, observable } from 'mobx';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';
import { useColorAnimation } from '../../utils/colorAnimation';

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

    public get appearanceOptions() {
        return this.optionsBuilder('appearance');
    }

    public get pulseOptions() {
        return this.optionsBuilder('pulse');
    }

    public get grimaceOptions() {
        return this.optionsBuilder('grimace');
    }

    public get activityOptions() {
        return this.optionsBuilder('activity');
    }

    public get respirationOptions() {
        return this.optionsBuilder('respiration');
    }

    private optionsBuilder(kind: string) {
        return Array.from({ length: 3 }, (_, i) => ({ label: i18n.t(`tools:apgar:${kind}:options:${i}`) as string, value: i }));
    }

    public setAppearanceValue({ value }: (typeof this)['appearanceOptions'][number]) {
        this.appearance = value;
    }

    public setPulseValue({ value }: (typeof this)['pulseOptions'][number]) {
        this.pulse = value;
    }

    public setGrimaceValue({ value }: (typeof this)['grimaceOptions'][number]) {
        this.grimace = value;
    }

    public setActivityValue({ value }: (typeof this)['activityOptions'][number]) {
        this.activity = value;
    }

    public setRespirationValue({ value }: (typeof this)['respirationOptions'][number]) {
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
            <Text style={styles.dropdownTitle}>{t('tools:apgar:appearance:title')}</Text>
            <Dropdown
                data={localStore.appearanceOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.appearance as unknown as string}
                onChange={localStore.setAppearanceValue}
            />
            <Text style={styles.dropdownTitle}>{t('tools:apgar:pulse:title')}</Text>
            <Dropdown
                data={localStore.pulseOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.pulse as unknown as string}
                onChange={localStore.setPulseValue}
            />
            <Text style={styles.dropdownTitle}>{t('tools:apgar:grimace:title')}</Text>
            <Dropdown
                data={localStore.grimaceOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.grimace as unknown as string}
                onChange={localStore.setGrimaceValue}
            />
            <Text style={styles.dropdownTitle}>{t('tools:apgar:activity:title')}</Text>
            <Dropdown
                data={localStore.activityOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.activity as unknown as string}
                onChange={localStore.setActivityValue}
            />
            <Text style={styles.dropdownTitle}>{t('tools:apgar:respiration:title')}</Text>
            <Dropdown
                data={localStore.respirationOptions}
                labelField="label"
                valueField="value"
                fontFamily="opensanshebrew"
                style={styles.dropdown}
                value={localStore.respiration as unknown as string}
                onChange={localStore.setRespirationValue}
            />
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
