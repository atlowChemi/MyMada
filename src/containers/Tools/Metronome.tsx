import { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { Circle, Svg, Text as SvgText } from 'react-native-svg';
import { StyleSheet, View, Pressable } from 'react-native';
import Animated, { withTiming, useAnimatedProps, useSharedValue, withRepeat, Easing } from 'react-native-reanimated';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { NavigationStore } from '../../services/NavigationStore';
import { MetronomeStore } from '../../services/MetronomeStore';
import { useDependencyInjector } from '../../services/container';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';

const AnimatedCircle = Animated.createAnimatedComponent(Circle);

export const Metronome = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const store = useDependencyInjector(MetronomeStore);
    const navigationStore = useDependencyInjector(NavigationStore);
    const scale = useSharedValue(49);
    const strokeDashoffset = useSharedValue(store.circle * (store.progressBarDiameter * 1));

    const shadowAnimatedProps = useAnimatedProps(() => ({ r: `${scale.value}%` }));
    const circleAnimatedProps = useAnimatedProps(() => ({ r: `${scale.value - 2}%` }));
    const progressBarAnimatedProps = useAnimatedProps(() => ({ strokeDashoffset: strokeDashoffset.value }));
    useEffect(() => {
        const theta = store.startTime ? store.progress : 1.0001;
        const duration = store.startTime ? 10 : 320;
        strokeDashoffset.value = withTiming(theta * store.circle * store.progressBarDiameter, { duration, easing: Easing.linear });
    }, [strokeDashoffset, store.circle, store.startTime, store.progress, store.progressBarDiameter]);
    useEffect(() => {
        if (!store.startTime) {
            scale.value = withTiming(49, { duration: 320 });
            return;
        }
        scale.value = withRepeat(withTiming(scale.value === 49 ? 50 : 49, { duration: 360, easing: Easing.inOut(Easing.cubic) }), -1, true);
    }, [scale, store.startTime]);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    useEffect(() => {
        store.updateStartTimeFromModule();
    }, [store]);

    return (
        <BasePageView scrollType="none">
            <Pressable style={styles.page} onPress={store.toggleMetronome}>
                <Text style={styles.title}>{t('tools:metronome:title')}</Text>
                <View style={styles.wrapper}>
                    <Svg width={store.diameter} height={store.diameter}>
                        <AnimatedCircle cx="50%" cy="50%" fill={colors.shadowEnd} animatedProps={shadowAnimatedProps} />
                        <AnimatedCircle cx="50%" cy="50%" fill={colors.white} animatedProps={circleAnimatedProps} />
                        <Circle cx="50%" cy="50%" r={store.progressBarDiameter} stroke={colors.grey} strokeWidth={store.strokeWidth} fill="none" />
                        <AnimatedCircle
                            r={store.progressBarDiameter}
                            cx="50%"
                            cy="50%"
                            fill="none"
                            strokeLinecap="round"
                            strokeWidth={store.strokeWidth}
                            stroke={colors.oldPrimary}
                            animatedProps={progressBarAnimatedProps}
                            strokeDasharray={`${store.circumference} ${store.circumference}`}
                        />
                        {store.clock.hours && <SvgText {...store.clock.hours} />}
                        <SvgText {...store.clock.time} />
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
