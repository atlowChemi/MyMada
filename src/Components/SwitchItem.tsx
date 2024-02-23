import Color from 'color';
import { useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { StyleSheet, Switch } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';
import { PressableRippleWrapper } from './PressableRippleWrapper';

const rippleColor = Color(colors.white).darken(0.5).string();
const activeThumb = colors.colorAqua;
const activeTrack = Color(activeThumb).white(53).string();
const inactiveThumb = colors.materialGrey600;
const inactiveTrack = Color(inactiveThumb).lighten(0.2).string();

interface SwitchItemProps {
    label: string;
    value?: boolean;
    onChange?(value: boolean): void;
}

export const SwitchItem = observer<SwitchItemProps>(({ label, value, onChange }) => {
    const onValueChange = useCallback(() => onChange?.(!value), [value, onChange]);
    return (
        <PressableRippleWrapper onPress={onValueChange} pointerEvents="box-only" android_ripple={{ color: rippleColor, borderless: true }} style={styles.switch} wrapperStyle={styles.switchWrapper}>
            <Text style={styles.switchText}>{label}</Text>
            <Switch value={value} thumbColor={value ? activeThumb : inactiveThumb} trackColor={{ false: inactiveTrack, true: activeTrack }} />
        </PressableRippleWrapper>
    );
});
SwitchItem.displayName = 'SwitchItem';

const styles = StyleSheet.create({
    switchWrapper: {
        borderBottomWidth: 1,
        borderBottomColor: colors.colorBlueAlpha,
    },
    switch: {
        height: 60,
        alignItems: 'center',
        flexDirection: 'row',
        paddingHorizontal: 15,
        justifyContent: 'space-between',
    },
    switchText: {
        fontSize: 21,
    },
});
