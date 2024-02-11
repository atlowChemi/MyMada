import { useEffect } from 'react';
import { injectable } from 'inversify';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet } from 'react-native';
import { action, computed, makeObservable, observable } from 'mobx';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { TextInput } from '../../Components/TextInput';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import { audioGenerator } from '../../services/AudioGenerator';
import type { NavigatableToolsScreenProps } from '../Pages/Tools';

@injectable()
class PulseCalculatorLocalStore {
    private isPulseInputInvalid = false;
    private countdownValue = 15;
    private pulseInQuarterMin = 0;
    private deadline?: number;
    private intervalRef?: NodeJS.Timeout;

    constructor() {
        makeObservable<PulseCalculatorLocalStore, 'pulseInQuarterMin' | 'countdownValue' | 'clearTimer' | 'intervalTick' | 'isPulseInputInvalid'>(this, {
            error: computed,
            pulse1m: computed,
            pulse15s: computed,
            clearTimer: action,
            countdown: computed,
            toggleTimer: action,
            intervalTick: action,
            setPulseInQuarterMin: action,
            countdownValue: observable.ref,
            pulseInQuarterMin: observable.ref,
            isPulseInputInvalid: observable.ref,
        });
    }

    public get pulse15s() {
        if (Number.isNaN(this.pulseInQuarterMin)) {
            return '';
        }
        return `${this.pulseInQuarterMin ?? ''}`;
    }

    public get pulse1m() {
        if (Number.isNaN(this.pulseInQuarterMin)) {
            return '';
        }
        return `${this.pulseInQuarterMin * 4}`;
    }

    public get countdown() {
        return this.countdownValue;
    }

    public get error() {
        if (this.isPulseInputInvalid) {
            return i18n.t('tools:pulse:pulseInputErr');
        }
        return undefined;
    }

    public setPulseInQuarterMin = (pulseInQuarterMin: string) => {
        const numeralValue = pulseInQuarterMin.replace(/\D/g, '');
        this.isPulseInputInvalid = !numeralValue.length;
        const pulse = parseInt(numeralValue, 10);
        this.pulseInQuarterMin = pulse;
    };

    public toggleTimer = () => {
        if (this.intervalRef) {
            this.clearTimer();
            return;
        }
        this.deadline = Date.now() + 15_000;
        this.intervalRef = setInterval(this.intervalTick, 230);
    };

    private clearTimer = (value?: number) => {
        if (!this.intervalRef) {
            return;
        }
        clearInterval(this.intervalRef);
        this.deadline = undefined;
        this.intervalRef = undefined;
        this.countdownValue = value ?? 15;
    };

    private beep = async () => {
        const playerId = await audioGenerator.createPlayer();
        const waves = await Promise.all([audioGenerator.getSineWave(3000, 0), audioGenerator.getSineWave(3000, 9000.25)]);
        // Maximize volume.
        const originalVolume = await audioGenerator.maximizeVolume();
        await Promise.all(waves.map(wave => audioGenerator.writeSound(playerId, wave)));
        await audioGenerator.destroyAudioTrack(playerId);
        waves.forEach(w => audioGenerator.destroyWave(w));
        await audioGenerator.setVolume(originalVolume);
        // Lower volume.
    };

    private intervalTick = () => {
        if (!this.deadline) {
            this.clearTimer();
            return;
        }
        const now = Date.now();
        const diff = this.deadline - now;
        if (diff <= 0) {
            this.beep();
            this.clearTimer(0);
            return;
        }
        const countdownValue = Math.ceil(diff / 1_000);
        if (countdownValue === this.countdownValue) {
            return;
        }
        this.countdownValue = countdownValue;
    };
}

export const PulseCalculator = observer<NavigatableToolsScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalStoreWithInjectedDependencies(PulseCalculatorLocalStore);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation);
    }, [navigationStore, navigation]);

    // Handle landscape orientation
    return (
        <BasePageView
            postScroll={
                <Pressable style={styles.bottomSection} onPress={localStore.toggleTimer}>
                    <Text style={styles.time}>{localStore.countdown}</Text>
                    <Text style={styles.timeUnit}>{t('tools:pulse:seconds')}</Text>
                    <Text style={styles.timerMsg}>{t('tools:pulse:timerHint')}</Text>
                </Pressable>
            }>
            <TextInput
                wrapperStyle={styles.field}
                label={t('tools:pulse:pulse')}
                error={localStore.error}
                keyboardType="numeric"
                onChangeText={localStore.setPulseInQuarterMin}
                value={localStore.pulse15s}
            />
            <TextInput wrapperStyle={styles.field} label={t('tools:pulse:fullPulse')} editable={false} value={localStore.pulse1m} />
        </BasePageView>
    );
});
PulseCalculator.displayName = 'PulseCalculator';

const styles = StyleSheet.create({
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
    timerMsg: {
        color: '#cfcfcf',
        fontSize: 15,
    },
});
