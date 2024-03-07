import { StyleSheet } from 'react-native';
import { Dropdown } from 'react-native-element-dropdown';
import { Text } from './Text';
import { colors } from '../consts';
import { observer } from 'mobx-react-lite';

interface ToolsDropDownProps<T> {
    title: string;
    options: { label: string; value: T }[];
    value: T;
    onChange: (item: { label: string; value: T }) => void;
}

const styles = StyleSheet.create({
    dropdownTitle: {
        fontSize: 21,
        fontWeight: 'bold',
        textAlign: 'center',
        color: colors.colorAqua,
    },
    dropdown: {
        marginHorizontal: 10,
        borderBottomWidth: 0.5,
        borderColor: colors.buttonMaterialDark,
    },
});

export const ToolsDropDown = observer(<T extends string | number>({ title, options, value, onChange }: ToolsDropDownProps<T>) => (
    <>
        <Text style={styles.dropdownTitle}>{title}</Text>
        <Dropdown data={options} labelField="label" valueField="value" fontFamily="OpenSans" style={styles.dropdown} value={value as any} onChange={onChange} />
    </>
));
ToolsDropDown.displayName = 'ToolsDropDown';
