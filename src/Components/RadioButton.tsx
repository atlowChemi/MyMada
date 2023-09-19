import { useEffect, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import { Animated, GestureResponderEvent, Pressable, StyleSheet, View } from 'react-native';
import { colors } from '../consts';

const scale = 1.0;
const BORDER_WIDTH = 2;

interface Props<T extends {}> {
    value: T;
    status?: 'checked' | 'unchecked';
    disabled?: boolean;
    onPress?(e: GestureResponderEvent): void;
}

export const RadioButton = observer(<T extends {}>({ disabled, onPress, value: _value, status }: Props<T>) => {
    const checked = status === 'checked';
    const { current: borderAnim } = useRef(new Animated.Value(BORDER_WIDTH));
    const { current: radioAnim } = useRef(new Animated.Value(1));
    const isInitialRender = useRef(true);

    useEffect(() => {
        const { current: isInitial } = isInitialRender;
        if (isInitial) {
            isInitialRender.current = false;
            return;
        }
        if (status === 'checked') {
            radioAnim.setValue(1.2);
            Animated.timing(radioAnim, {
                toValue: 1,
                duration: 250 * scale,
                useNativeDriver: true,
            }).start();
            return;
        }
        borderAnim.setValue(10);
        Animated.timing(borderAnim, {
            toValue: BORDER_WIDTH,
            duration: 250 * scale,
            useNativeDriver: false,
        }).start();
    }, [status, borderAnim, radioAnim]);

    return (
        <Pressable
            onPress={onPress}
            android_ripple={{ color: colors.shadow, radius: 13, borderless: true }}
            accessibilityRole="radio"
            accessibilityState={{ disabled, checked }}
            accessibilityLiveRegion="polite"
            style={styles.container}>
            <Animated.View
                style={[
                    styles.radio,
                    {
                        borderColor: colors.colorNice,
                        borderWidth: borderAnim,
                    },
                ]}>
                {checked && (
                    <View style={[StyleSheet.absoluteFill, styles.radioContainer]}>
                        <Animated.View
                            style={[
                                styles.dot,
                                {
                                    backgroundColor: colors.colorNice,
                                    transform: [{ scale: radioAnim }],
                                },
                            ]}
                        />
                    </View>
                )}
            </Animated.View>
        </Pressable>
    );
});
RadioButton.displayName = 'RadioButton';

const styles = StyleSheet.create({
    container: {
        borderRadius: 18,
    },
    radioContainer: {
        alignItems: 'center',
        justifyContent: 'center',
    },
    radio: {
        height: 20,
        width: 20,
        borderRadius: 10,
        margin: 8,
    },
    dot: {
        height: 10,
        width: 10,
        borderRadius: 5,
    },
});
