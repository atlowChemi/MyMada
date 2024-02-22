import Color from 'color';
import { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { Text } from './Text';
import { colors } from '../consts';
import { RadioButton } from './RadioButton';
import { PressableRippleWrapper } from './PressableRippleWrapper';
import type { StyleProp, ViewStyle } from 'react-native';

interface RadioButtonItemProps<T extends {} = any> {
    value: T;
    label: string;
    selectedValue: T;
    onChange(value: T): void;
    itemStyle?: StyleProp<ViewStyle>;
}

export interface RadioButtonGroupProps<T extends {} = any> {
    value: T;
    direction?: 'horizontal' | 'vertical';
    options: { [label: string]: T };
    onChange(value: T): void;
    itemStyle?: StyleProp<ViewStyle>;
}

const rippleColor = Color(colors.materialGrey600).fade(0.5).string();

const RadioButtonItem = observer(<T extends {}>({ value, onChange, label, selectedValue, itemStyle }: RadioButtonItemProps<T>) => {
    const localStore = useLocalObservable(() => ({
        label,
        value,
        onChange,
        selectedValue,
        updateStore(_value: T, _onChange: (value: T) => void, _label: string, _selectedValue: T) {
            this.value = _value;
            this.onChange = _onChange;
            this.label = _label;
            this.selectedValue = _selectedValue;
        },
        get status() {
            return this.value === this.selectedValue ? 'checked' : 'unchecked';
        },
        onPress() {
            this.onChange(this.value);
        },
    }));
    useEffect(() => localStore.updateStore(value, onChange, label, selectedValue), [value, onChange, label, selectedValue, localStore]);
    return (
        <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: false }} style={[styles.container, itemStyle]} wrapperStyle={styles.radius} onPress={localStore.onPress}>
            <RadioButton value={value} status={localStore.status} onPress={localStore.onPress} />
            <Text style={styles.label}>{label}</Text>
        </PressableRippleWrapper>
    );
});
RadioButtonItem.displayName = 'RadioButtonItem';

export const RadioButtonGroup = observer(<T extends {}>({ value, onChange, options, itemStyle, direction = 'horizontal' }: RadioButtonGroupProps<T>) => (
    <View style={direction === 'horizontal' && styles.groupHorizontal} accessibilityRole="radiogroup">
        {Object.entries(options).map(([label, val]) => (
            <RadioButtonItem key={label} label={label} value={val} selectedValue={value} onChange={onChange} itemStyle={itemStyle} />
        ))}
    </View>
));
RadioButtonGroup.displayName = 'RadioButtonGroup';

const styles = StyleSheet.create({
    radius: {
        borderRadius: 8,
    },
    container: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingVertical: 8,
        paddingHorizontal: 16,
    },
    label: {
        flexShrink: 1,
        flexGrow: 1,
    },
    groupHorizontal: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        width: '100%',
    },
});
