import Dialog from './@FontWrappers';
import Slider from '@react-native-community/slider';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import type { IDialogInstance } from '../../services/DialogStore';
import { SettingsItem } from '../../Components/SettingsItem';
import { Text } from '../../Components/Text';

interface AlertVolumeDialogResponse {
    overrideVolume: boolean;
    volume: number;
}

interface AlertVolumeDialogProps extends IDialogInstance<AlertVolumeDialogResponse> {
    maxVolume: number;
    initialVolume: number;
    initialOverrideVolume: boolean;
}

export const AlertVolumeDialog = observer<AlertVolumeDialogProps>(({ initialOverrideVolume, initialVolume, maxVolume, closeDialog }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(() => ({
        overrideVolume: initialOverrideVolume ?? false,
        volume: initialVolume ?? 0,
        onCancel() {
            closeDialog(undefined);
        },
        onApprove() {
            closeDialog({ overrideVolume: this.overrideVolume, volume: this.volume });
        },
        onChangeOverrideVolume(value: boolean) {
            this.overrideVolume = value;
        },
        onVolumeChange(value: number) {
            this.volume = Math.min(value, maxVolume);
        },
    }));

    return (
        <Dialog.Container visible contentStyle={styles.content} headerStyle={styles.content} onBackdropPress={localStore.onCancel} onRequestClose={localStore.onCancel}>
            <SettingsItem
                type="switch"
                value={localStore.overrideVolume}
                onChange={localStore.onChangeOverrideVolume}
                label={t('settings:advanced:alertVolume:title')}
                subLabel={t(`settings:advanced:alertVolume:subTitle${localStore.overrideVolume ? 'Enabled' : 'Disabled'}`)}
            />
            <Text style={styles.pickerTitle}>{t('settings:advanced:alertVolume:pickerTitle')}</Text>
            <Slider
                step={1}
                maximumValue={maxVolume}
                value={localStore.volume}
                disabled={!localStore.overrideVolume}
                onValueChange={localStore.onVolumeChange}
                thumbTintColor={colors.colorPrimaryDark}
                minimumTrackTintColor={colors.colorPrimary}
            />
            <Dialog.Button style={styles.btn} label={t('common:cancel')} onPress={localStore.onCancel} />
            <Dialog.Button style={styles.btn} label={t('common:ok')} onPress={localStore.onApprove} />
        </Dialog.Container>
    );
});

const styles = StyleSheet.create({
    content: {
        width: 360,
        padding: 0,
        margin: 0,
    },
    pickerTitle: {
        marginVertical: 8,
        textAlign: 'center',
    },
    btn: {
        color: colors.colorNice,
        marginVertical: 8,
        marginHorizontal: 4,
    },
});
