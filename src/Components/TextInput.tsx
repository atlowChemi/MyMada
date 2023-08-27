import { memo, useMemo } from 'react';
import { StyleSheet, TextInput as NativeTextInput, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import type { StyleProp, ViewStyle, TextStyle } from 'react-native';

interface TextInputProps {
    label: string;
    numberOfLines?: number;
    wrapperStyle?: StyleProp<ViewStyle>;
    labelStyle?: StyleProp<TextStyle>;
    inputStyle?: StyleProp<TextStyle>;
    onChangeText?(text: string): void;
    value?: string;
}

export const TextInput = memo<TextInputProps>(({ label, numberOfLines, value, onChangeText, ...styleProps }) => {
    const height = useMemo(() => (numberOfLines ? numberOfLines * 27 : undefined), [numberOfLines]);
    return (
        <View style={[styles.wrapper, styleProps.wrapperStyle]}>
            <Text style={[styles.label, styleProps.labelStyle]}>{label}</Text>
            <NativeTextInput
                style={[styles.input, styleProps.inputStyle, { height }]}
                numberOfLines={numberOfLines}
                multiline={Boolean(numberOfLines)}
                underlineColorAndroid={colors.colorNice}
                value={value}
                onChangeText={onChangeText}
            />
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
        color: colors.colorNice,
    },
    input: {
        fontFamily: 'opensanshebrew',
        textAlignVertical: 'top',
        fontSize: 14,
    },
});
