import React from 'react';
import { Text as NativeText, StyleSheet } from 'react-native';

type TextProps = React.ComponentProps<typeof NativeText>;

export const Text: React.FC<TextProps> = props => {
    return <NativeText {...props} style={[styles.text, props.style]} />;
};

const styles = StyleSheet.create({
    text: {
        fontFamily: 'opensanshebrew',
    },
});
