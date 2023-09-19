import { reaction } from 'mobx';
import { useEffect, forwardRef } from 'react';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { StyleSheet, TextInput as NativeTextInput, View, Animated } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import { InputLabel } from './InputLabel';
import { roundLayoutSize } from '../utils';
import type { ComponentProps } from 'react';
import type { StyleProp, ViewStyle, TextStyle, LayoutChangeEvent, ColorValue } from 'react-native';

interface TextInputProps extends Omit<ComponentProps<typeof NativeTextInput>, 'style'> {
    label: string;
    error?: string;
    numberOfLines?: number;
    wrapperStyle?: StyleProp<ViewStyle>;
    errorStyle?: StyleProp<TextStyle>;
    inputStyle?: StyleProp<TextStyle>;
    onChangeText?(text: string): void;
}
const SCALE = 1.0;
const LABEL_WIGGLE_X_OFFSET = 4;
const BLUR_ANIMATION_DURATION = 180;
const FOCUS_ANIMATION_DURATION = 150;
const MAXIMIZED_LABEL_FONT_SIZE = 16;
const MINIMIZED_LABEL_FONT_SIZE = 12;
const MINIMIZED_LABEL_Y_OFFSET = -18;

export const TextInput = observer<TextInputProps, NativeTextInput>(
    forwardRef(({ label, error, numberOfLines, wrapperStyle, errorStyle, inputStyle, ...props }, ref) => {
        const localStore = useLocalObservable(() => ({
            underlineColorProp: props.underlineColorAndroid,
            labelLayout: { width: 0, height: 0 },
            value: props.value,
            error,
            numberOfLines,
            focused: false,
            editable: props.editable,
            labeled: new Animated.Value(props.value ? 0 : 1),
            errorAnimation: new Animated.Value(error ? 1 : 0),
            onChange(value: string) {
                this.value = value;
                props.onChangeText?.(value);
            },
            setup() {
                const disposers = [
                    reaction(
                        () => [this.value, this.focused],
                        () => {
                            this.labeled.stopAnimation();
                            // The label should be minimized if the text input is focused, or has text.
                            // In minimized mode, the label moves up and becomes small
                            if (this.value || this.focused) {
                                // minimize label
                                Animated.timing(this.labeled, {
                                    toValue: 0,
                                    duration: BLUR_ANIMATION_DURATION * SCALE,
                                    useNativeDriver: true,
                                }).start();
                                return;
                            }
                            Animated.timing(this.labeled, {
                                toValue: 1,
                                duration: FOCUS_ANIMATION_DURATION * SCALE,
                                useNativeDriver: true,
                            }).start();
                        },
                    ),
                    reaction(
                        () => this.error,
                        () => {
                            // When the input has an error, we wiggle the label and apply error styles
                            if (this.error) {
                                // show error
                                Animated.timing(this.errorAnimation, {
                                    toValue: 1,
                                    duration: FOCUS_ANIMATION_DURATION * SCALE,
                                    useNativeDriver: true,
                                }).start();
                                return;
                            }
                            // hide error
                            Animated.timing(this.errorAnimation, {
                                toValue: 0,
                                duration: BLUR_ANIMATION_DURATION * SCALE,
                                useNativeDriver: true,
                            }).start();
                        },
                    ),
                ];
                return () => disposers.forEach(disposer => disposer());
            },
            updateValues(err?: string, editable?: boolean, numOfLines?: number, underlineColor?: ColorValue) {
                this.error = err;
                this.editable = editable;
                this.numberOfLines = numOfLines;
                this.underlineColorProp = underlineColor;
            },
            onLayoutAnimatedText(e: LayoutChangeEvent) {
                const { layout } = e.nativeEvent;
                const width = roundLayoutSize(layout.width);
                const height = roundLayoutSize(layout.height);

                if (width !== this.labelLayout.width) {
                    this.labelLayout.width = width;
                }
                if (height !== this.labelLayout.height) {
                    this.labelLayout.height = height;
                }
            },
            onFocus() {
                this.focused = true;
            },
            onBlur() {
                this.focused = false;
            },
            get height() {
                return this.numberOfLines ? this.numberOfLines * 27 : undefined;
            },
            get color() {
                if (this.error) {
                    return colors.oldPrimaryDark;
                }
                return this.editable ?? true ? colors.colorNice : colors.buttonMaterialDark;
            },
            get underlineColor() {
                if (this.underlineColorProp) {
                    return this.underlineColorProp;
                }
                if (this.error) {
                    return colors.oldPrimaryDark;
                }
                if (this.focused && (this.editable ?? true)) {
                    return colors.colorNice;
                }
                return colors.buttonMaterialDark;
            },
            get labelScale() {
                return MINIMIZED_LABEL_FONT_SIZE / MAXIMIZED_LABEL_FONT_SIZE;
            },
            get topPosition() {
                return -3;
            },
            get baseLabelTranslateX() {
                const labelWidth = this.labelLayout.width;
                const labelHalfWidth = labelWidth / 2;
                const paddingLeft = 15; // arbitrary number
                // (I18nManager.getConstants().isRTL ? 1 : -1)
                return 1 * (labelHalfWidth - (this.labelScale * labelWidth) / 2) + (1 - this.labelScale) * paddingLeft;
            },
            get baseLabelTranslateY() {
                const labelHalfHeight = -1 * (this.labelLayout.height / 2);
                return labelHalfHeight - (this.topPosition + MINIMIZED_LABEL_Y_OFFSET) - 34;
            },
        }));
        useEffect(() => {
            localStore.updateValues(error, props.editable, numberOfLines, props.underlineColorAndroid);
        }, [error, props.editable, numberOfLines, props.underlineColorAndroid, localStore]);
        useEffect(() => localStore.setup(), [localStore]);
        return (
            <View style={[styles.wrapper, wrapperStyle]}>
                {/* <Text style={[styles.label, { color: localStore.color }, labelStyle]}>{label}</Text> */}
                <InputLabel
                    label={label}
                    wiggle={Boolean(error)}
                    labelError={Boolean(error)}
                    focused={localStore.focused}
                    labeled={localStore.labeled}
                    activeColor={props.underlineColorAndroid || colors.colorNice}
                    error={localStore.errorAnimation}
                    errorColor={colors.oldPrimaryDark}
                    labelScale={localStore.labelScale}
                    topPosition={localStore.topPosition}
                    wiggleOffsetX={LABEL_WIGGLE_X_OFFSET}
                    placeholderColor={props.underlineColorAndroid || colors.buttonMaterialDark}
                    baseLabelTranslateX={localStore.baseLabelTranslateX}
                    baseLabelTranslateY={localStore.baseLabelTranslateY}
                    onLayoutAnimatedText={localStore.onLayoutAnimatedText}
                />
                <NativeTextInput
                    {...props}
                    ref={ref}
                    onBlur={localStore.onBlur}
                    onFocus={localStore.onFocus}
                    onChangeText={localStore.onChange}
                    numberOfLines={numberOfLines}
                    underlineColorAndroid={localStore.underlineColor}
                    multiline={Boolean(numberOfLines)}
                    style={[styles.input, styles.font, inputStyle, { height: localStore.height }]}
                />
                {error && <Text style={[styles.error, errorStyle]}>{error}</Text>}
            </View>
        );
    }),
);
TextInput.displayName = 'TextInput';

const styles = StyleSheet.create({
    wrapper: {
        paddingTop: 6,
        marginHorizontal: 10,
        marginBottom: 20,
    },
    label: {
        fontSize: 14,
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    input: {
        textAlignVertical: 'top',
        fontSize: 14,
    },
    error: {
        fontSize: 14,
        start: 5,
        top: -5,
        color: colors.oldPrimaryDark,
    },
});
