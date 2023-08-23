import React from 'react';
import { observable } from 'mobx';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { Image, Pressable, StyleSheet } from 'react-native';
import { colors } from '../consts';
import type { GestureResponderEvent, PressableStateCallbackType, StyleProp, ViewStyle, ImageSourcePropType } from 'react-native';

interface HomeBtnProps {
    big?: boolean;
    img: ImageSourcePropType;
    style?: StyleProp<ViewStyle>;
    onPress(event: GestureResponderEvent): void;
}

export const HomeBtn = observer<HomeBtnProps>(({ big, img, style, onPress }) => {
    const state = useLocalObservable(
        () => ({
            big,
            style,
            isPressed: false,
            pressableStyle({ pressed }: PressableStateCallbackType): StyleProp<ViewStyle> {
                return [
                    styles.btnCard,
                    !this.big && { flexBasis: '50%' },
                    pressed && {
                        opacity: 0.7,
                    },
                    this.style,
                ];
            },
            onPressIn() {
                this.isPressed = true;
            },
            onPressOut() {
                this.isPressed = false;
            },
            get imageStyle() {
                return [styles.image, this.isPressed && { opacity: 0.7 }];
            },
        }),
        { style: observable.ref },
    );

    React.useEffect(() => {
        state.big = big;
    }, [state, big]);
    React.useEffect(() => {
        state.style = style;
    }, [state, style]);

    return (
        <Pressable style={state.pressableStyle} onPressIn={state.onPressIn} onPressOut={state.onPressOut} onPress={onPress}>
            <Image source={img} style={styles.image} />
        </Pressable>
    );
});

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
        backgroundColor: colors.white,
    },
    image: {
        maxWidth: '100%',
        height: '100%',
        resizeMode: 'cover',
    },
});
