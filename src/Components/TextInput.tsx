import { memo, useMemo } from 'react';
import { StyleSheet, TextInput as NativeTextInput, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import type { StyleProp, ViewStyle, TextStyle, KeyboardTypeOptions } from 'react-native';

interface TextInputProps {
    label: string;
    error?: string;
    numberOfLines?: number;
    wrapperStyle?: StyleProp<ViewStyle>;
    labelStyle?: StyleProp<TextStyle>;
    errorStyle?: StyleProp<TextStyle>;
    inputStyle?: StyleProp<TextStyle>;
    onChangeText?(text: string): void;
    value?: string;
    keyboardType?: KeyboardTypeOptions;
    editable?: boolean;
}

export const TextInput = memo<TextInputProps>(({ label, error, numberOfLines, wrapperStyle, labelStyle, errorStyle, inputStyle, ...props }) => {
    const height = useMemo(() => (numberOfLines ? numberOfLines * 27 : undefined), [numberOfLines]);
    const color = useMemo(() => {
        if (error) {
            return colors.oldPrimaryDark;
        }
        return props.editable ?? true ? colors.colorNice : colors.buttonMaterialDark;
    }, [error, props.editable]);
    return (
        <View style={[styles.wrapper, wrapperStyle]}>
            <Text style={[styles.label, { color }, labelStyle]}>{label}</Text>
            <NativeTextInput style={[styles.input, inputStyle, { height }]} numberOfLines={numberOfLines} multiline={Boolean(numberOfLines)} underlineColorAndroid={color} {...props} />
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
