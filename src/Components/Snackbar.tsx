import { useCallback } from 'react';
import { Animated, Pressable, StyleSheet } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import { useAsyncEffect } from '../utils';
import type { GestureResponderEvent, StyleProp, ViewProps, ViewStyle } from 'react-native';

export interface SnackbarProps extends Omit<ViewProps, 'style'> {
    msg: string;
    timeout?: number;
    closeSnackbar(byAction: boolean): void;
    action?: string;
    style?: Animated.WithAnimatedValue<StyleProp<ViewStyle>>;
}

export const Snackbar: React.FC<SnackbarProps> = ({ msg, action, style, closeSnackbar, timeout, ...rest }) => {
    // const bottom = React.useRef(new Animated.Value(0)).current;
    const close = useCallback((e?: GestureResponderEvent) => closeSnackbar(Boolean(e)), [closeSnackbar]);

    useAsyncEffect(
        isMounted => {
            const closeTimeout =
                timeout &&
                setTimeout(() => {
                    if (isMounted()) {
                        close();
                    }
                }, timeout);
            return () => {
                if (closeTimeout) {
                    clearTimeout(closeTimeout);
                }
            };
        },
        [timeout, close],
    );
    return (
        <Animated.View style={[styles.snackbar, style]} {...rest}>
            <Text style={styles.text}>{msg}</Text>
            {action && (
                <Pressable style={styles.action} onPress={close}>
                    {action}
                </Pressable>
            )}
        </Animated.View>
    );
};

const styles = StyleSheet.create({
    snackbar: {
        elevation: 6,
        flexWrap: 'wrap',
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
        paddingVertical: 8,
        paddingHorizontal: 16,
        borderTopEndRadius: 4,
        borderTopStartRadius: 4,
        borderBottomEndRadius: 4,
        borderBottomStartRadius: 4,
        backgroundColor: colors.colorPrimary,
    },
    text: {
        marginVertical: 8,
        color: colors.white,
    },
    action: {
        marginStart: 'auto',
        marginEnd: -8,
    },
});
