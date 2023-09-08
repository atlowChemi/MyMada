import { useEffect } from 'react';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { StyleSheet, TextInput as NativeTextInput, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import type { ComponentProps } from 'react';
import type { StyleProp, ViewStyle, TextStyle } from 'react-native';

interface TextInputProps extends ComponentProps<typeof NativeTextInput> {
    label: string;
    error?: string;
    numberOfLines?: number;
    wrapperStyle?: StyleProp<ViewStyle>;
    labelStyle?: StyleProp<TextStyle>;
    errorStyle?: StyleProp<TextStyle>;
    inputStyle?: StyleProp<TextStyle>;
    onChangeText?(text: string): void;
}

export const TextInput = observer<TextInputProps>(({ label, error, numberOfLines, wrapperStyle, labelStyle, errorStyle, inputStyle, ...props }) => {
    const localStore = useLocalObservable(() => ({
        error,
        numberOfLines,
        focused: false,
        editable: props.editable,
        updateValues(err?: string, editable?: boolean, numOfLines?: number) {
            this.error = err;
            this.editable = editable;
            this.numberOfLines = numOfLines;
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
            if (this.error) {
                return colors.oldPrimaryDark;
            }
            if (this.focused && (this.editable ?? true)) {
                return colors.colorNice;
            }
            return colors.buttonMaterialDark;
        },
    }));
    useEffect(() => {
        localStore.updateValues(error, props.editable, numberOfLines);
    }, [error, props.editable, numberOfLines, localStore]);
    return (
        <View style={[styles.wrapper, wrapperStyle]}>
            <Text style={[styles.label, { color: localStore.color }, labelStyle]}>{label}</Text>
            <NativeTextInput
                {...props}
                onBlur={localStore.onBlur}
                onFocus={localStore.onFocus}
                numberOfLines={numberOfLines}
                underlineColorAndroid={localStore.underlineColor}
                multiline={Boolean(numberOfLines)}
                style={[styles.input, inputStyle, { height: localStore.height }]}
            />
            {error && <Text style={[styles.error, errorStyle]}>{error}</Text>}
        </View>
    );
});

const styles = StyleSheet.create({
    wrapper: {
        marginHorizontal: 10,
        marginBottom: 20,
    },
    label: {
        fontSize: 14,
    },
    input: {
        fontFamily: 'opensanshebrew',
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
