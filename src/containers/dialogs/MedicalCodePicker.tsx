import Color from 'color';
import Dialog from 'react-native-dialog';
import { observable } from 'mobx';
import { computedFn } from 'mobx-utils';
import { useTranslation } from 'react-i18next';
import { FlatList, StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Checkbox } from '../../Components/Checkbox';
import { getMedicalCodeItems, MedicalCodeEl } from '../../utils/medicalCodes';
import { TouchableRipple } from '../../Components/TouchableRipple';
import type { IDialogInstance } from '../../services/DialogStore';

interface MedicalCodePickerProps extends IDialogInstance<Set<string>> {
    selectedCodes?: Set<string>;
}

const SECTION_COLOR_MAP = {
    AREA: { color: colors.medicalCodes.areaAndPain, ripple: Color(colors.medicalCodes.areaAndPain).alpha(0.32).rgb().string() },
    TRAUMA: { color: colors.medicalCodes.trauma, ripple: Color(colors.medicalCodes.trauma).alpha(0.32).rgb().string() },
    PAIN: { color: colors.medicalCodes.areaAndPain, ripple: Color(colors.medicalCodes.areaAndPain).alpha(0.32).rgb().string() },
    CARDIO_VASCULAR: { color: colors.medicalCodes.cardioVascular, ripple: Color(colors.medicalCodes.cardioVascular).alpha(0.32).rgb().string() },
    RESPIRATORY: { color: colors.medicalCodes.respiratory, ripple: Color(colors.medicalCodes.respiratory).alpha(0.32).rgb().string() },
    NEUROLOGY: { color: colors.medicalCodes.neurology, ripple: Color(colors.medicalCodes.neurology).alpha(0.32).rgb().string() },
    GASTRO: { color: colors.medicalCodes.gastro, ripple: Color(colors.medicalCodes.gastro).alpha(0.32).rgb().string() },
    INFECTION: { color: colors.medicalCodes.infection, ripple: Color(colors.medicalCodes.infection).alpha(0.32).rgb().string() },
    PSYCHIATRY: { color: colors.medicalCodes.psychiatry, ripple: Color(colors.medicalCodes.psychiatry).alpha(0.32).rgb().string() },
    ENDOCRINOLOGY: { color: colors.medicalCodes.endocrinology, ripple: Color(colors.medicalCodes.endocrinology).alpha(0.32).rgb().string() },
    UROLOGY: { color: colors.medicalCodes.urology, ripple: Color(colors.medicalCodes.urology).alpha(0.32).rgb().string() },
    GENERAL: { color: colors.medicalCodes.general, ripple: Color(colors.medicalCodes.general).alpha(0.32).rgb().string() },
} as const;

const MedicalCodeItem = observer<{ item: MedicalCodeEl; selectedCodes: Set<string>; onToggleItem(code: string): () => void }>(({ item, selectedCodes, onToggleItem }) => {
    const { color, ripple } = SECTION_COLOR_MAP[item.section];
    if (item.type === 'title') {
        return (
            <View style={[styles.separator, { backgroundColor: color }]}>
                <Text style={styles.separatorTitle}>{item.title}</Text>
            </View>
        );
    }
    return (
        <TouchableRipple rippleColor={ripple} onPress={onToggleItem(item.code)}>
            <View pointerEvents="none" style={styles.item}>
                <Checkbox color={color} checked={selectedCodes.has(item.code)} />
                <Text>
                    {item.code} {item.name}
                </Text>
            </View>
        </TouchableRipple>
    );
});

export const MedicalCodePicker = observer<MedicalCodePickerProps>(({ selectedCodes, closeDialog }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        query: '',
        selectedCodes: observable.set<string>(selectedCodes),
        onSearch(query: string) {
            this.query = query;
        },
        onCancel() {
            closeDialog();
        },
        onApprove() {
            closeDialog(this.selectedCodes);
        },
        innerItemToggle(code: string) {
            if (this.selectedCodes.has(code)) {
                this.selectedCodes.delete(code);
                return;
            }
            this.selectedCodes.add(code);
        },
        get onToggleItem() {
            return computedFn((code: string) => () => this.innerItemToggle(code));
        },
        get items() {
            return getMedicalCodeItems(this.query);
        },
    }));

    return (
        <Dialog.Container visible onRequestClose={localStore.onCancel} contentStyle={styles.container}>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{t('dispatch:form:pickMedicalCode')}</Dialog.Title>
            <Dialog.Input
                autoComplete="off"
                value={localStore.query}
                label={t('common:search')}
                wrapperStyle={styles.input}
                cursorColor={colors.colorNice}
                onChangeText={localStore.onSearch}
                underlineColorAndroid={colors.colorNice}
                style={[styles.font, styles.dialogColorDark]}
                unstableLabelStyle={{ color: colors.colorNice }}
            />
            <FlatList data={localStore.items} renderItem={({ item }) => <MedicalCodeItem item={item} selectedCodes={localStore.selectedCodes} onToggleItem={localStore.onToggleItem} />} />
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});
MedicalCodePicker.displayName = 'MedicalCodePicker';

const styles = StyleSheet.create({
    container: {
        padding: 0,
        margin: 0,
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    dialogColor: {
        color: colors.colorNice,
    },
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
    input: {
        marginBottom: 0,
        paddingBottom: 0,
    },
    separator: {
        paddingVertical: 17,
    },
    separatorTitle: {
        fontWeight: '900',
        textAlign: 'center',
        color: colors.white,
    },
    item: {
        padding: 5,
        alignItems: 'center',
        flexDirection: 'row',
    },
});
