import { Pressable, StyleSheet, View } from 'react-native';
import type { StyleProp, ViewStyle } from 'react-native';
import type { FunctionComponent, ComponentProps } from 'react';

interface PressableRippleWrapperProps extends ComponentProps<typeof Pressable> {
    wrapperStyle?: StyleProp<ViewStyle>;
}

export const PressableRippleWrapper: FunctionComponent<PressableRippleWrapperProps> = ({ wrapperStyle, style, ...rest }) => {
    return (
        <View style={[wrapperStyle, styles.view]}>
            <Pressable {...rest} style={StyleSheet.flatten([style, styles.pressable])} />
        </View>
    );
};
PressableRippleWrapper.displayName = 'PressableRippleWrapper';

const styles = StyleSheet.create({
    view: {
        padding: 0,
        overflow: 'hidden',
    },
    pressable: {
        margin: 0,
        elevation: 0,
        borderRadius: 0,
    },
});
