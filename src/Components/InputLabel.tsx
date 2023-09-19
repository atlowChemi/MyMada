import { Animated, StyleSheet } from 'react-native';
import type { StyleProp, ViewProps, TextStyle, ColorValue } from 'react-native';

const MAX_FONT_SIZE_MULTIPLIER = 1.5;

interface InputLabelProps {
    placeholderOpacity?: number | Animated.Value | Animated.AnimatedInterpolation<number>;
    baseLabelTranslateX: number;
    baseLabelTranslateY: number;
    wiggleOffsetX: number;
    labelScale: number;
    lineHeight?: number | undefined;
    fontWeight?: TextStyle['fontWeight'];
    topPosition: number;
    paddingLeft?: number;
    paddingRight?: number;
    labelTranslationXOffset?: number;
    placeholderColor: ColorValue | null;
    label?: string | React.ReactElement | null;
    hasActiveOutline?: boolean | null;
    activeColor: ColorValue;
    errorColor?: ColorValue;
    labelError?: boolean | null;
    onLayoutAnimatedText: (args: any) => void;
    contentStyle?: StyleProp<ViewProps>;
    labeled: Animated.Value;
    error: Animated.Value;
    focused: boolean;
    wiggle: boolean;
    // labelLayoutMeasured: boolean;
    // labelLayoutWidth: number;
}
export const InputLabel = (props: InputLabelProps) => {
    const {
        labeled,
        wiggle,
        error,
        focused,
        label,
        labelError,
        onLayoutAnimatedText,
        hasActiveOutline,
        activeColor,
        baseLabelTranslateX,
        baseLabelTranslateY,
        lineHeight,
        fontWeight = 'normal',
        placeholderOpacity,
        wiggleOffsetX,
        labelScale,
        topPosition,
        paddingLeft,
        paddingRight,
        placeholderColor,
        errorColor,
        labelTranslationXOffset,
    } = props;

    const paddingOffset = paddingLeft && paddingRight ? { paddingLeft, paddingRight } : {};

    const labelTranslationX = {
        transform: [
            {
                // Offset label scale since RN doesn't support transform origin
                translateX: labeled.interpolate({ inputRange: [0, 1], outputRange: [baseLabelTranslateX, labelTranslationXOffset || 0] }),
            },
        ],
    };
    const fontSize = 16;

    const labelStyle = {
        ...styles.font,
        fontSize,
        lineHeight,
        fontWeight,
        opacity: labeled.interpolate({ inputRange: [0, 1], outputRange: [hasActiveOutline ? 1 : 0, 0] }),
        transform: [
            {
                // Wiggle the label when there's an error
                translateX: wiggle ? error.interpolate({ inputRange: [0, 0.5, 1], outputRange: [0, wiggleOffsetX, 0] }) : 0,
            },
            {
                // Move label to top
                translateY: baseLabelTranslateY !== 0 ? labeled.interpolate({ inputRange: [0, 1], outputRange: [baseLabelTranslateY, 0] }) : 0,
            },
            {
                // Make label smaller
                scale: labelScale !== 0 ? labeled.interpolate({ inputRange: [0, 1], outputRange: [labelScale, 1] }) : labeled,
            },
        ],
    };

    const textColor = labelError && errorColor ? errorColor : focused ? activeColor : placeholderColor;

    return (
        // Position colored placeholder and gray placeholder on top of each other and crossfade them
        // This gives the effect of animating the color, but allows us to use native driver
        <Animated.View pointerEvents="none" style={[StyleSheet.absoluteFill, styles.labelContainer, labelTranslationX]}>
            <Animated.Text
                onLayout={onLayoutAnimatedText}
                style={[labelStyle, paddingOffset, { top: topPosition, color: activeColor }]}
                numberOfLines={1}
                maxFontSizeMultiplier={MAX_FONT_SIZE_MULTIPLIER}>
                {label}
            </Animated.Text>
            <Animated.Text
                style={[labelStyle, paddingOffset, { fontSize, top: topPosition, color: textColor!, opacity: placeholderOpacity }]}
                numberOfLines={1}
                maxFontSizeMultiplier={MAX_FONT_SIZE_MULTIPLIER}>
                {label}
            </Animated.Text>
        </Animated.View>
    );
};
InputLabel.displayName = 'TextInputLabel';

const styles = StyleSheet.create({
    font: {
        fontFamily: 'opensanshebrew',
    },
    labelContainer: {
        zIndex: 3,
    },
});
