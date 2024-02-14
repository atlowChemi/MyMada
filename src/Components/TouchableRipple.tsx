import { forwardRef, Children } from 'react';
import { PressableAndroidRippleConfig, StyleProp, Platform, ViewStyle, StyleSheet, GestureResponderEvent, View, ColorValue, Pressable } from 'react-native';
import type { PressableProps, PressableStateCallbackType } from 'react-native';

const ANDROID_VERSION_LOLLIPOP = 21;
const ANDROID_VERSION_PIE = 28;

export interface TouchableRippleProps extends Omit<PressableProps, 'android_ripple' | 'children'> {
    borderless?: boolean;
    background?: PressableAndroidRippleConfig;
    centered?: boolean;
    disabled?: boolean;
    onPress?: (e: GestureResponderEvent) => void | null;
    onLongPress?: (e: GestureResponderEvent) => void;
    onPressIn?: (e: GestureResponderEvent) => void;
    onPressOut?: (e: GestureResponderEvent) => void;
    rippleColor: ColorValue;
    children: React.ReactNode;
}

const supported = Platform.OS === 'android' && Platform.Version >= ANDROID_VERSION_LOLLIPOP;

export const TouchableRipple = forwardRef<View, TouchableRippleProps>(({ style, background, borderless = false, disabled: disabledProp, rippleColor, children, ...rest }, ref) => {
    const { onPress, onLongPress, onPressIn, onPressOut } = rest;
    const hasPassedTouchHandler = Boolean(onPress || onLongPress || onPressIn || onPressOut);
    const disabled = disabledProp || !hasPassedTouchHandler;
    const styleFn = (state: PressableStateCallbackType): StyleProp<ViewStyle> => {
        return [borderless && styles.overflowHidden, typeof style === 'function' ? style(state) : style];
    };

    if (supported) {
        // A workaround for ripple on Android P is to use useForeground + overflow: 'hidden' https://github.com/facebook/react-native/issues/6480
        const useForeground = Platform.OS === 'android' && Platform.Version >= ANDROID_VERSION_PIE && borderless;
        const androidRipple = background ?? { color: rippleColor, borderless, foreground: useForeground };
        return (
            <Pressable {...rest} ref={ref} disabled={disabled} style={styleFn} android_ripple={androidRipple}>
                {Children.only(children)}
            </Pressable>
        );
    }

    return (
        <Pressable {...rest} ref={ref} disabled={disabled} style={styleFn}>
            {({ pressed }) => (
                <>
                    {pressed && <View testID="touchable-ripple-underlay" style={[styles.underlay, { backgroundColor: rippleColor }]} />}
                    {Children.only(children)}
                </>
            )}
        </Pressable>
    );
});
TouchableRipple.displayName = 'TouchableRipple';

const styles = StyleSheet.create({
    overflowHidden: {
        overflow: 'hidden',
    },
    underlay: {
        ...StyleSheet.absoluteFillObject,
        zIndex: 2,
    },
});
