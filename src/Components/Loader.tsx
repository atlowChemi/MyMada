import { useState, useEffect, useRef, memo, useCallback } from 'react';
import { StyleSheet, Animated, Easing, View, StyleProp, ViewStyle } from 'react-native';
import { colors as COLORS } from '../consts';
import { BasePageView } from './BasePageView';

const defaultColors = [COLORS.black, COLORS.colorPrimaryDark, COLORS.ocean, COLORS.colorAqua];

interface LoaderProps {
    dots?: number;
    colors?: string[];
    size?: number;
    bounceHeight?: number;
    borderRadius?: number;
}

export const Loader = memo<LoaderProps>(({ dots = 4, colors = defaultColors, size = 20, bounceHeight = 20, borderRadius }) => {
    const [reverse, setReverse] = useState(false);
    const { current: opacity } = useRef(new Animated.Value(0));
    const { current: animations } = useRef(Array.from({ length: dots }, () => new Animated.Value(0)));

    const appearAnimation = useCallback(() => Animated.timing(opacity, { toValue: 1, easing: Easing.ease, useNativeDriver: true }).start(), [opacity]);

    const floatAnimation = useCallback(
        (node: Animated.Value, reverseY: boolean, delay: number) => {
            const floatSequence = Animated.sequence([
                Animated.timing(node, { toValue: reverseY ? bounceHeight : -bounceHeight, easing: Easing.bezier(0.41, -0.15, 0.56, 1.21), delay, useNativeDriver: true }),
                Animated.timing(node, { toValue: reverseY ? -bounceHeight : bounceHeight, easing: Easing.bezier(0.41, -0.15, 0.56, 1.21), delay, useNativeDriver: true }),
                Animated.timing(node, { toValue: 0, delay, useNativeDriver: true }),
            ]);
            return floatSequence;
        },
        [bounceHeight],
    );

    const loadingAnimation = useCallback(
        (nodes: Animated.Value[], reverseY: boolean) => {
            Animated.parallel(nodes.map((node, index) => floatAnimation(node, reverseY, index * 100))).start(() => setReverse(!reverse));
        },
        [reverse, floatAnimation],
    );

    useEffect(() => {
        if (!animations.length) {
            return;
        }
        loadingAnimation(animations, reverse);
        appearAnimation();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    useEffect(() => {
        if (!animations.length) {
            return;
        }
        loadingAnimation(animations, reverse);
    }, [reverse, animations, loadingAnimation]);

    return (
        <Animated.View style={[styles.loading, { opacity, width: size * (dots + 2) }]}>
            {animations.map((animation, index) => (
                <Animated.View
                    key={`loading-anim-${index}`}
                    style={[
                        {
                            width: size,
                            height: size,
                            borderRadius: borderRadius || size / 2,
                            backgroundColor: colors[index] || COLORS.colorPrimaryDark,
                            transform: [{ translateY: animation }],
                        },
                    ]}
                />
            ))}
        </Animated.View>
    );
});
Loader.displayName = 'Loader';

export const PageLoader = memo<LoaderProps & { wrapperStyles?: StyleProp<ViewStyle> }>(({ wrapperStyles, ...props }) => (
    <BasePageView scrollType="none">
        <View style={[styles.wrapper, wrapperStyles]}>
            <Loader {...props} />
        </View>
    </BasePageView>
));

const styles = StyleSheet.create({
    loading: {
        display: 'flex',
        alignItems: 'center',
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    wrapper: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
});
