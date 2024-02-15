import Color from 'color';
import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import { Icon, IconName } from './Icon';
import { TouchableRipple } from './TouchableRipple';
import type { ReactNode } from 'react';
import type { ColorValue, GestureResponderEvent, PressableAndroidRippleConfig, StyleProp, TextStyle, ViewStyle } from 'react-native';

const MIN_WIDTH = 112;
const MAX_WIDTH = 280;

interface MenuItemProps {
    title: ReactNode;
    leadingIcon?: IconName;
    trailingIcon?: IconName;
    disabled?: boolean;
    background?: PressableAndroidRippleConfig;
    onPress?(e: GestureResponderEvent): void;
    style?: StyleProp<ViewStyle>;
    contentStyle?: StyleProp<ViewStyle>;
    titleStyle?: StyleProp<TextStyle>;
    rippleColor?: ColorValue;
}

export const MenuItem = observer<MenuItemProps>(({ leadingIcon, trailingIcon, title, disabled, background, onPress, style, contentStyle, titleStyle, rippleColor: customRippleColor }) => {
    const color = disabled ? colors.materialGrey600 : colors.black;
    const rippleColor = customRippleColor || Color(colors.colorPrimary).alpha(0.32).rgb().string();
    const maxWidth = MAX_WIDTH - (leadingIcon && trailingIcon ? 2 * 24 + 24 : leadingIcon || trailingIcon ? 24 + 24 : 12);

    return (
        <TouchableRipple style={[styles.container, style]} onPress={onPress} disabled={disabled} testID="menu-item" background={background} accessibilityRole="menuitem" rippleColor={rippleColor}>
            <View style={styles.row}>
                {leadingIcon && (
                    <View style={styles.icon} pointerEvents="box-none">
                        <Icon name={leadingIcon} size={24} color={color} />
                    </View>
                )}
                <View style={[styles.content, { maxWidth }, contentStyle]} pointerEvents="none">
                    <Text selectable={false} numberOfLines={1} testID="menu-item-title" style={[{ color }, titleStyle]}>
                        {title}
                    </Text>
                </View>
                {trailingIcon && (
                    <View style={styles.icon} pointerEvents="box-none">
                        <Icon name={trailingIcon} size={24} color={color} />
                    </View>
                )}
            </View>
        </TouchableRipple>
    );
});

MenuItem.displayName = 'MenuItem';

const styles = StyleSheet.create({
    container: {
        height: 48,
        minWidth: MIN_WIDTH,
        maxWidth: MAX_WIDTH,
        paddingHorizontal: 12,
        justifyContent: 'center',
    },
    row: {
        gap: 8,
        flexDirection: 'row',
    },
    title: {
        fontSize: 16,
    },
    icon: {
        width: 24,
    },
    content: {
        minWidth: MIN_WIDTH - 12,
        justifyContent: 'center',
    },
});
