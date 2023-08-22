import React from 'react';
import { Image, Pressable, StyleSheet } from 'react-native';
import { colors } from '../consts';
import type { GestureResponderEvent, PressableStateCallbackType, StyleProp, ViewStyle, ImageSourcePropType } from 'react-native';

export const HomeBtn: React.FC<{ big?: boolean; img: ImageSourcePropType; onPress(event: GestureResponderEvent): void }> = ({ big, img, onPress }) => {
    const pressableStyle = React.useCallback(
        (state: PressableStateCallbackType): StyleProp<ViewStyle> => {
            return [
                styles.btnCard,
                !big && { flexBasis: '50%' },
                state.pressed && {
                    opacity: 0.7,
                },
            ];
        },
        [big],
    );

    return (
        <Pressable style={pressableStyle} onPress={onPress}>
            <Image source={img} style={styles.image} />
        </Pressable>
    );
};

const styles = StyleSheet.create({
    btnCard: {
        elevation: 5,
        borderRadius: 27,
        padding: 'auto',
        flexShrink: 1,
        height: 150,
        overflow: 'hidden',
        alignItems: 'center',
        alignContent: 'center',
        justifyContent: 'center',
        textAlignVertical: 'center',
        backgroundColor: colors.White,
    },
    image: {
        maxWidth: '100%',
        height: '100%',
        resizeMode: 'cover',
    },
});
