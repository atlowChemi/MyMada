import { useEffect } from 'react';
import { observable } from 'mobx';
import { useTranslation } from 'react-i18next';
import { Pressable, StyleSheet } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../consts';
import { Text } from '../Components/Text';
import { Icon, type IconName } from '../Components/Icon';
import type { GestureResponderEvent, PressableStateCallbackType, StyleProp, ViewStyle } from 'react-native';

interface HomeBtnProps {
    big?: boolean;
    icon: IconName;
    size?: number;
    style?: StyleProp<ViewStyle>;
    textStyle?: StyleProp<ViewStyle>;
    onPress(event: GestureResponderEvent): void;
}

export const HomeBtn = observer<HomeBtnProps>(({ big, icon, size = 80, textStyle, style, onPress }) => {
    const { t, i18n } = useTranslation();
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
        }),
        { style: observable.ref },
    );

    useEffect(() => {
        state.big = big;
    }, [state, big]);
    useEffect(() => {
        state.style = style;
    }, [state, style]);

    return (
        <Pressable style={state.pressableStyle} onPressIn={state.onPressIn} onPressOut={state.onPressOut} onPress={onPress}>
            <Icon name={icon} size={size} color={colors.oldPrimary} />
            <Text style={[styles.name, i18n.language === 'en' && styles.englishName, textStyle]}>{t(`home:${icon}`)}</Text>
        </Pressable>
    );
});
HomeBtn.displayName = 'HomeBtn';

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
    name: {
        fontSize: 28,
        fontWeight: '900',
        textAlign: 'center',
    },
    englishName: {
        letterSpacing: -2,
    },
});
