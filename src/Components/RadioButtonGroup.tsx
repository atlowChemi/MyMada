import { useEffect } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { Text } from './Text';
import { RadioButton } from './RadioButton';

interface RadioButtonItemProps<T extends {} = any> {
    value: T;
    label: string;
    selectedValue: T;
    onChange(value: T): void;
}

export interface RadioButtonGroupProps<T extends {} = any> {
    value: T;
    direction?: 'horizontal' | 'vertical';
    options: { [label: string]: T };
    onChange(value: T): void;
}

const RadioButtonItem = observer(<T extends {}>({ value, onChange, label, selectedValue }: RadioButtonItemProps<T>) => {
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
        <Pressable style={styles.container} onPress={localStore.onPress}>
            <RadioButton value={value} status={localStore.status} onPress={localStore.onPress} />
            <Text style={styles.label}>{label}</Text>
        </Pressable>
    );
});
RadioButtonItem.displayName = 'RadioButtonItem';

export const RadioButtonGroup = observer(<T extends {}>({ value, onChange, options, direction = 'horizontal' }: RadioButtonGroupProps<T>) => (
    <View style={direction === 'horizontal' && styles.groupHorizontal} accessibilityRole="radiogroup">
        {Object.entries(options).map(([label, val]) => (
            <RadioButtonItem key={label} label={label} value={val} selectedValue={value} onChange={onChange} />
        ))}
    </View>
));
RadioButtonGroup.displayName = 'RadioButtonGroup';

const styles = StyleSheet.create({
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
