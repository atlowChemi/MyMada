import { Svg, Path } from 'react-native-svg';
import { forwardRef, useImperativeHandle } from 'react';
import { Animated, Easing, TouchableOpacity, View } from 'react-native';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Text } from './Text';
import { colors } from '../consts';
import type { FC } from 'react';
import type { Linecap, PathProps } from 'react-native-svg';
import type { GestureResponderEvent, StyleProp, TextStyle, ViewProps, ViewStyle } from 'react-native';

const MIN_ARC_ANGLE = 0.1;
const MAX_ARC_ANGLE = 1.5 * Math.PI;
const CIRCLE = Math.PI * 2;

function makeArcPath(x: number, y: number, startAngleArg: number, endAngleArg: number, radius: number, direction: 'clockwise' | 'counter-clockwise') {
    let startAngle = startAngleArg;
    let endAngle = endAngleArg;
    if (endAngle - startAngle >= CIRCLE) {
        endAngle = CIRCLE + (endAngle % CIRCLE);
    } else {
        endAngle = endAngle % CIRCLE;
    }
    startAngle = startAngle % CIRCLE;
    const angle = startAngle > endAngle ? CIRCLE - startAngle + endAngle : endAngle - startAngle;

    if (angle >= CIRCLE) {
        return `M${x + radius} ${y}
            a${radius} ${radius} 0 0 1 0 ${radius * 2}
            a${radius} ${radius} 0 0 1 0 ${radius * -2}`;
    }

    const directionFactor = direction === 'counter-clockwise' ? -1 : 1;
    endAngle *= directionFactor;
    startAngle *= directionFactor;
    const startSine = Math.sin(startAngle);
    const startCosine = Math.cos(startAngle);
    const endSine = Math.sin(endAngle);
    const endCosine = Math.cos(endAngle);

    const arcFlag = angle > Math.PI ? 1 : 0;
    const reverseFlag = direction === 'counter-clockwise' ? 0 : 1;

    return `M${x + radius * (1 + startSine)} ${y + radius - radius * startCosine}
          A${radius} ${radius} 0 ${arcFlag} ${reverseFlag} ${x + radius * (1 + endSine)} ${y + radius - radius * endCosine}`;
}

interface ArcProps extends PathProps {
    startAngle: number;
    endAngle: number;
    radius: number;
    offset?: { top: number; left: number };
    strokeCap?: Linecap;
    strokeWidth?: number;
    direction?: 'clockwise' | 'counter-clockwise';
}
interface CircleSnailProps extends ViewProps {
    animating?: boolean;
    color?: string | string[];
    backgroundColor?: string;
    title?: string;
    direction?: 'clockwise' | 'counter-clockwise';
    duration?: number;
    hidesWhenStopped?: boolean;
    size?: number;
    spinDuration?: number;
    style?: StyleProp<ViewStyle>;
    thickness?: number;
    strokeCap?: Linecap;
    useNativeDriver?: boolean;
    onPress?(e: GestureResponderEvent): void;
}

const Arc = Animated.createAnimatedComponent(
    forwardRef<unknown, ArcProps>(({ endAngle, radius, startAngle = 0, direction = 'clockwise', offset = { top: 0, left: 0 }, strokeCap = 'butt', strokeWidth = 0, ...pathProps }, ref) => {
        useImperativeHandle(ref, () => ({}), []);
        const path = makeArcPath((offset.left || 0) + strokeWidth / 2, (offset.top || 0) + strokeWidth / 2, startAngle, endAngle, radius - strokeWidth / 2, direction);

        return <Path d={path} strokeLinecap={strokeCap} strokeWidth={strokeWidth} {...pathProps} />;
    }),
);
Arc.displayName = 'Arc';

export const CircularProgressButton: FC<CircleSnailProps> = ({
    animating = true,
    color = colors.ocean,
    backgroundColor = colors.white,
    direction = 'counter-clockwise',
    duration = 1000,
    hidesWhenStopped = false,
    size = 190,
    spinDuration = 5000,
    style,
    thickness = 3,
    title,
    strokeCap = 'round',
    useNativeDriver = false,
    onPress,
    ...restProps
}) => {
    const isAnimating = useRef<boolean>();
    const [colorIndex, setColorIndex] = useState(0);
    const { current: startAngle } = useRef(new Animated.Value(-MIN_ARC_ANGLE));
    const { current: endAngle } = useRef(new Animated.Value(0));
    const { current: rotation } = useRef(new Animated.Value(0));

    const animate = useCallback(
        (iteration = 1) => {
            const endAngleTargetValue = -MAX_ARC_ANGLE * iteration;
            Animated.sequence([
                Animated.timing(startAngle, { toValue: endAngleTargetValue - MIN_ARC_ANGLE, duration, isInteraction: false, easing: Easing.inOut(Easing.quad), useNativeDriver }),
                Animated.timing(endAngle, { toValue: endAngleTargetValue, duration, isInteraction: false, easing: Easing.inOut(Easing.quad), useNativeDriver }),
            ]).start(endState => {
                if (!endState.finished) {
                    return;
                }
                if (Array.isArray(color)) {
                    setColorIndex(iteration % color.length);
                }
                animate(++iteration);
            });
        },
        [color, duration, useNativeDriver, endAngle, startAngle],
    );
    const spin = useCallback(() => {
        Animated.timing(rotation, { toValue: 1, duration: spinDuration, easing: Easing.linear, isInteraction: false, useNativeDriver }).start(endState => {
            if (!endState.finished) {
                return;
            }
            rotation.setValue(0);
            spin();
        });
    }, [spinDuration, useNativeDriver, rotation]);
    const stopAnimations = useCallback(() => {
        startAngle.stopAnimation();
        endAngle.stopAnimation();
        rotation.stopAnimation();
        startAngle.setValue(-MIN_ARC_ANGLE);
        endAngle.setValue(0);
        rotation.setValue(0);
    }, [startAngle, endAngle, rotation]);

    useEffect(() => {
        if (isAnimating.current === animating) {
            return;
        }
        isAnimating.current = animating;
        if (!animating) {
            stopAnimations();
            return;
        }
        animate();
        spin();
    }, [animating, animate, spin, stopAnimations]);

    const radius = useMemo(() => size / 2 - thickness, [size, thickness]);
    const offset = useMemo(() => ({ top: thickness, left: thickness }), [thickness]);
    const directionFactor = useMemo(() => (direction === 'counter-clockwise' ? -1 : 1), [direction]);
    const currentColor = useMemo(() => (Array.isArray(color) ? color[colorIndex] : color), [color, colorIndex]);
    const touchableStyle = useMemo<StyleProp<ViewStyle>>(() => ({ height: size, width: size, borderRadius: radius, backgroundColor, overflow: 'hidden' }), [radius, backgroundColor, size]);
    const viewStyle = useMemo<StyleProp<ViewStyle>>(
        () => ({
            backgroundColor: colors.transparent,
            overflow: 'hidden',
            transform: [
                {
                    rotate: rotation.interpolate({
                        inputRange: [0, 1],
                        outputRange: ['0deg', `${directionFactor * 360}deg`],
                    }),
                },
            ],
        }),
        [rotation, directionFactor],
    );
    const textSize = size - thickness * 2;
    const textWrapperStyle = useMemo<StyleProp<ViewStyle>>(
        () => ({
            position: 'absolute',
            left: thickness,
            top: thickness,
            width: textSize,
            height: textSize,
            borderRadius: textSize / 2,
            alignItems: 'center',
            justifyContent: 'center',
        }),
        [thickness, textSize],
    );
    const computedTextStyle = useMemo<StyleProp<TextStyle>>(() => ({ color: currentColor, fontSize: size / 8, fontWeight: '300', textAlign: 'center' }), [currentColor, size]);

    if (!animating && hidesWhenStopped) {
        return null;
    }

    return (
        <TouchableOpacity onPress={onPress} activeOpacity={0.7} style={touchableStyle}>
            <Animated.View {...restProps} style={[style, viewStyle]}>
                <Svg width={size} height={size} fill={colors.transparent}>
                    <Arc
                        direction={direction === 'counter-clockwise' ? 'clockwise' : 'counter-clockwise'}
                        radius={radius}
                        stroke={animating ? currentColor : colors.transparent}
                        offset={offset}
                        startAngle={startAngle}
                        endAngle={endAngle}
                        strokeCap={strokeCap}
                        strokeWidth={thickness}
                    />
                </Svg>
            </Animated.View>
            {title && (
                <View style={textWrapperStyle}>
                    <Text style={computedTextStyle}>{title}</Text>
                </View>
            )}
        </TouchableOpacity>
    );
};
CircularProgressButton.displayName = 'CircularProgressButton';
