import Color from 'color';
import { useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { StyleSheet, Switch, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import { PressableRippleWrapper } from './PressableRippleWrapper';

const rippleColor = Color(colors.white).darken(0.5).string();
const activeThumb = colors.colorAqua;
const activeTrack = Color(activeThumb).white(53).string();
const inactiveThumb = colors.materialGrey600;
const inactiveTrack = Color(inactiveThumb).lighten(0.2).string();

interface SettingItemProps {
    type: 'switch' | 'row';
    label: string;
    subLabel?: string;
    value?: boolean;
    disabled?: boolean;
    onPress?(): void;
    onChange?(value: boolean): void;
}

export const SettingsItem = observer<SettingItemProps>(({ type, label, subLabel, value, disabled, onPress, onChange }) => {
    const onPressHandler = useCallback(() => (type === 'switch' ? onChange?.(!value) : onPress?.()), [type, value, onChange, onPress]);
    return (
        <PressableRippleWrapper
            disabled={disabled}
            style={styles.switch}
            onPress={onPressHandler}
            pointerEvents="box-only"
            wrapperStyle={styles.switchWrapper}
            android_ripple={{ color: rippleColor, borderless: true }}>
            <View style={styles.textWrapper}>
                <Text style={[styles.switchText, disabled && styles.disabled]}>{label}</Text>
                {subLabel && <Text style={[styles.switchSubText, disabled && styles.disabled]}>{subLabel}</Text>}
            </View>
            {type === 'switch' && <Switch value={value} thumbColor={value ? activeThumb : inactiveThumb} trackColor={{ false: inactiveTrack, true: activeTrack }} />}
        </PressableRippleWrapper>
    );
});
SettingsItem.displayName = 'SettingsItem';

const styles = StyleSheet.create({
    switchWrapper: {
        borderBottomWidth: 1,
        borderBottomColor: colors.colorBlueAlpha,
    },
    switch: {
        minHeight: 60,
        alignItems: 'center',
        flexDirection: 'row',
        paddingVertical: 8,
        paddingHorizontal: 15,
        justifyContent: 'space-between',
    },
    textWrapper: {
        flexShrink: 1,
    },
    switchText: {
        fontSize: 21,
    },
    switchSubText: {
        fontSize: 16,
    },
    disabled: {
        color: colors.colorBlueAlpha,
    },
});
