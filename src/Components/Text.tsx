import React from 'react';
import { Text as NativeText, StyleSheet } from 'react-native';
import { colors } from '../consts';

type TextProps = React.ComponentProps<typeof NativeText>;

export const Text: React.FC<TextProps> = props => {
    return <NativeText {...props} style={[styles.text, props.style]} />;
};
Text.displayName = 'Text';

const styles = StyleSheet.create({
    text: {
        color: colors.black,
        fontFamily: 'opensanshebrew',
    },
});
