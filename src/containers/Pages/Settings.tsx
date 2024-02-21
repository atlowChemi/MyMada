import i18n from '../../localization';
import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { computed, makeAutoObservable } from 'mobx';
import { createDrawerNavigator, DrawerScreenProps } from '@react-navigation/drawer';
import { isTablet } from '../../utils';
import { Loader } from '../../Components/Loader';
import { Detection } from '../Settings/Detection';
import { DialogStore } from '../../services/DialogStore';
import { BasePageView } from '../../Components/BasePageView';
import { DrawerContent } from '../../Components/DrawerContent';
import { RadioSelectDialog, RadioSelectDialogProps } from '../dialogs/RadioSelectDialog';
import { useDeviceOrientation } from '../../utils/useDeviceOrientation';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';
import type { Item } from '../../Components/DrawerContent';

export type SettingPage = 'Detection' | 'Notification' | 'Advanced' | 'AmPm' | 'Tools';
type SettingsStackParamList = Record<SettingPage, undefined>;
export type NavigatableSettingsScreenProps = DrawerScreenProps<SettingsStackParamList, SettingPage>;
export const SETTINGS_PAGES = new Set<SettingPage>(['Detection', 'Notification', 'Advanced', 'AmPm', 'Tools']);

const Drawer = createDrawerNavigator<SettingsStackParamList>();

const Nullish = () => null;

@injectable()
class SettingsLocalStore {
    constructor(private readonly dialogStore: DialogStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, { drawerItems: computed.struct }, { autoBind: true });
    }

    public get drawerItems() {
        return fromPromise(this.buildOptions());
    }

    private async buildOptions(): Promise<Item<SettingPage>[]> {
        return [
            { type: 'link', icon: 'notification', label: 'settings:detection:menuName', page: 'Detection' },
            { type: 'link', icon: 'audio', label: 'settings:notification:menuName', page: 'Notification' },
            { type: 'link', icon: 'advanced', label: 'settings:advanced:menuName', page: 'Advanced' },
            { type: 'link', icon: 'clock', label: 'settings:amPm:menuName', page: 'AmPm' },
            { type: 'link', icon: 'edit', label: 'settings:tools:menuName', page: 'Tools' },
            { type: 'separator', label: 'settings:generalSettings:menuName' },
            { type: 'btn', label: 'settings:generalSettings:detectionDisabled', icon: 'detection', onPress: Nullish },
            { type: 'btn', label: 'settings:generalSettings:language', icon: 'translate', onPress: this.setLanguage },
            { type: 'btn', label: 'settings:generalSettings:region', icon: 'mada', onPress: this.setRegion },
        ];
    }

    private async setLanguage() {
        const language = await this.dialogStore.openDialog<RadioSelectDialogProps<'he' | 'en'>>(RadioSelectDialog, {
            title: i18n.t('settings:generalSettings:selectLanguage'),
            options: { English: 'en', עברית: 'he' },
            initialValue: i18n.language as any,
        });
        i18n.changeLanguage(language);
    }

    private setRegion() {
        this.dialogStore.openDialog(RadioSelectDialog, { title: 'hi', options: { moked1: 'en', moked2: 'he' }, initialValue: i18n.language });
    }
}

export const Settings = observer<NavigatableScreenProps>(() => {
    const orientation = useDeviceOrientation();
    const localStore = useLocalStoreWithInjectedDependencies(SettingsLocalStore);

    if (localStore.drawerItems.state !== 'fulfilled') {
        return (
            <BasePageView scrollType="none">
                <View style={styles.loaderWrapper}>
                    <Loader />
                </View>
            </BasePageView>
        );
    }

    const drawerItems = localStore.drawerItems.value;

    return (
        <Drawer.Navigator
            backBehavior="none"
            initialRouteName="Detection"
            screenOptions={{ headerShown: false, drawerType: isTablet() && orientation === 'landscape' ? 'permanent' : 'front' }}
            drawerContent={props => <DrawerContent {...props} items={drawerItems} />}>
            <Drawer.Screen name="Detection" component={Detection} />
            <Drawer.Screen name="Notification" component={Nullish} />
            <Drawer.Screen name="Advanced" component={Nullish} />
            <Drawer.Screen name="AmPm" component={Nullish} />
            <Drawer.Screen name="Tools" component={Nullish} />
        </Drawer.Navigator>
    );
});
Settings.displayName = 'Dispatch';

const styles = StyleSheet.create({
    loaderWrapper: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});
