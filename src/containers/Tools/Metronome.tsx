import { now } from 'mobx-utils';
import { useEffect } from 'react';
import { injectable } from 'inversify';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { Circle, Svg, Text as SvgText } from 'react-native-svg';
import { StyleSheet, Dimensions, View, Pressable } from 'react-native';
import Animated, { withTiming, useAnimatedProps, useSharedValue, withRepeat, Easing } from 'react-native-reanimated';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector, useLocalStoreWithInjectedDependencies } from '../../services/container';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';

const AnimatedCircle = Animated.createAnimatedComponent(Circle);
const CIRCLE = Math.PI * 2;
const STROKE_WIDTH = 17;
const MINUTE = 60_000;
const TWO_MINUTES = 2 * MINUTE;
const HOUR = 60 * MINUTE;
const CLOCK_COMMON_PROPS = {
    fill: colors.black,
    stroke: colors.black,
    fontSize: 80,
    textAnchor: 'middle',
    x: '50%',
} as const;

@injectable()
class MetronomeLocalStore {
    public startTime = 0;
    public readonly diameter = Math.min(Dimensions.get('screen').width - 40, 390);
    public readonly progressBarDiameter = this.diameter * 0.43 - STROKE_WIDTH / 2;

    constructor() {
        makeAutoObservable(this, undefined, { autoBind: true });
    }

    public get circumference() {
        return this.progressBarDiameter * CIRCLE;
    }

    private get timeDiff() {
        return now('frame') - this.startTime;
    }

    private get twoMinutePartition() {
        if (!this.startTime) {
            return 0;
        }
        return this.timeDiff / TWO_MINUTES;
    }

    public get progress() {
        const progressOfRound = this.twoMinutePartition % 1;
        const roundCount = Math.floor(this.twoMinutePartition);
        const isOddRound = roundCount % 2 === 1;
        const isOddOddRound = (roundCount / 2) % 2 === 1;
        if (isOddRound && !isOddOddRound) {
            return progressOfRound;
        }
        const factor = isOddRound ? -1 : 1;
        return factor - progressOfRound;
    }

    public get clock() {
        if (!this.startTime) {
            return { time: { ...CLOCK_COMMON_PROPS, y: this.diameter / 2 + 25, children: '00:00' } };
        }
        const diff = this.timeDiff;
        const hours = Math.floor(diff / HOUR);
        const minutes = Math.floor((diff - hours * HOUR) / MINUTE);
        const seconds = Math.floor((diff - minutes * MINUTE) / 1000);
        const hoursString = hours ? `${hours.toString().padStart(2, '0')}` : '';
        const hoursProps = hours ? { ...CLOCK_COMMON_PROPS, y: this.diameter / 2 - 5, children: hoursString } : undefined;
        const timeProps = {
            ...CLOCK_COMMON_PROPS,
            ...(hours && { fontSize: 48 }),
            y: this.diameter / 2 + (hours ? 45 : 25),
            children: `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`,
        };
        return { hours: hoursProps, time: timeProps };
    }

    private startMetronome() {
        this.startTime = Date.now();
    }

    private stopMetronome() {
        this.startTime = 0;
    }

    public toggleMetronome() {
        if (this.startTime) {
            this.stopMetronome();
            return;
        }
        this.startMetronome();
    }
}

export const Metronome = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalStoreWithInjectedDependencies(MetronomeLocalStore);
    const scale = useSharedValue(49);
    const strokeDashoffset = useSharedValue(CIRCLE * (localStore.progressBarDiameter * 1));

    const shadowAnimatedProps = useAnimatedProps(() => ({ r: `${scale.value}%` }));
    const circleAnimatedProps = useAnimatedProps(() => ({ r: `${scale.value - 2}%` }));
    const progressBarAnimatedProps = useAnimatedProps(() => ({ strokeDashoffset: strokeDashoffset.value }));
    useEffect(() => {
        const theta = localStore.startTime ? localStore.progress : 1.0001;
        const duration = localStore.startTime ? 10 : 320;
        strokeDashoffset.value = withTiming(theta * CIRCLE * localStore.progressBarDiameter, { duration, easing: Easing.linear });
    }, [strokeDashoffset, localStore.startTime, localStore.progress, localStore.progressBarDiameter]);
    useEffect(() => {
        if (!localStore.startTime) {
            scale.value = withTiming(49, { duration: 320 });
            return;
        }
        scale.value = withRepeat(withTiming(scale.value === 49 ? 50 : 49, { duration: 360, easing: Easing.inOut(Easing.cubic) }), -1, true);
    }, [scale, localStore.startTime]);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    return (
        <BasePageView scrollType="none">
            <Pressable style={styles.page} onPress={localStore.toggleMetronome}>
                <Text style={styles.title}>{t('tools:metronome:title')}</Text>
                <View style={styles.wrapper}>
                    <Svg width={localStore.diameter} height={localStore.diameter}>
                        <AnimatedCircle cx="50%" cy="50%" fill={colors.shadowEnd} animatedProps={shadowAnimatedProps} />
                        <AnimatedCircle cx="50%" cy="50%" fill={colors.white} animatedProps={circleAnimatedProps} />
                        <Circle cx="50%" cy="50%" r={localStore.progressBarDiameter} stroke={colors.grey} strokeWidth={STROKE_WIDTH} fill="none" />
                        <AnimatedCircle
                            r={localStore.progressBarDiameter}
                            cx="50%"
                            cy="50%"
                            fill="none"
                            strokeLinecap="round"
                            strokeWidth={STROKE_WIDTH}
                            stroke={colors.oldPrimary}
                            animatedProps={progressBarAnimatedProps}
                            strokeDasharray={`${localStore.circumference} ${localStore.circumference}`}
                        />
                        {localStore.clock.hours && <SvgText {...localStore.clock.hours} />}
                        <SvgText {...localStore.clock.time} />
                    </Svg>
                </View>
            </Pressable>
        </BasePageView>
    );
});
Metronome.displayName = 'Metronome';

const styles = StyleSheet.create({
    page: {
        flex: 1,
    },
    title: {
        fontSize: 18,
        marginTop: 10,
        alignSelf: 'center',
    },
    wrapper: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});
