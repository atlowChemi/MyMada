import Color from 'color';
import { useRef, useEffect } from 'react';
import { Path, Svg } from 'react-native-svg';
import { Animated, GestureResponderEvent, StyleSheet, View } from 'react-native';
import { colors } from '../consts';
import { TouchableRipple, TouchableRippleProps } from './TouchableRipple';
import type { FunctionComponent } from 'react';

type Props = Omit<TouchableRippleProps, 'children' | 'rippleColor'> & {
    checked?: boolean;
    disabled?: boolean;
    onPress?: (e: GestureResponderEvent) => void;
    /** Custom color for checkbox. */
    color: string;
    /** Custom color for unchecked checkbox. */
    uncheckedColor?: string;
};

const ANIMATION_DURATION = 100;
const SCALE = 1.0;

export const Checkbox: FunctionComponent<Props> = ({ checked, disabled, onPress, color, uncheckedColor = color, ...rest }) => {
    const isFirstRendering = useRef(true);
    const { current: scaleAnim } = useRef<Animated.Value>(new Animated.Value(1));

    useEffect(() => {
        // Do not run animation on very first rendering
        if (isFirstRendering.current) {
            isFirstRendering.current = false;
            return;
        }

        Animated.sequence([
            Animated.timing(scaleAnim, {
                toValue: 0.85,
                duration: checked ? ANIMATION_DURATION * SCALE : 0,
                useNativeDriver: false,
            }),
            Animated.timing(scaleAnim, {
                toValue: 1,
                duration: checked ? ANIMATION_DURATION * SCALE : ANIMATION_DURATION * SCALE * 1.75,
                useNativeDriver: false,
            }),
        ]).start();
    }, [checked, scaleAnim]);

    const rippleColor = Color(color).alpha(0.32).rgb().string();
    const selectionControlColor = (disabled && colors.bgShadowEnd) || checked ? color : uncheckedColor;

    const borderWidth = scaleAnim.interpolate({ inputRange: [0.8, 1], outputRange: [7, 0] });

    return (
        <TouchableRipple
            {...rest}
            borderless
            rippleColor={rippleColor}
            onPress={onPress}
            disabled={disabled}
            accessibilityRole="checkbox"
            accessibilityState={{ disabled, checked }}
            accessibilityLiveRegion="polite"
            style={styles.container}>
            <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
                <Svg width={24} height={24} viewBox="0 -960 960 960" stroke={selectionControlColor} fill={selectionControlColor}>
                    {!checked && <Path d="M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Z" />}
                    {checked && (
                        <Path d="m424-312 282-282-56-56-226 226-114-114-56 56 170 170ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Z" />
                    )}
                </Svg>
                <View style={[StyleSheet.absoluteFill, styles.fillContainer]}>
                    <Animated.View style={[styles.fill, { borderColor: selectionControlColor }, { borderWidth }]} />
                </View>
            </Animated.View>
        </TouchableRipple>
    );
};
Checkbox.displayName = 'Checkbox';

const styles = StyleSheet.create({
    container: {
        borderRadius: 18,
        width: 36,
        height: 36,
        padding: 6,
    },
    fillContainer: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    fill: {
        height: 14,
        width: 14,
    },
});
