import i18n from '../../localization';
import { useEffect } from 'react';
import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { computed, makeAutoObservable } from 'mobx';
import { createDrawerNavigator, DrawerScreenProps } from '@react-navigation/drawer';
import { isTablet } from '../../utils';
import { AmPm } from '../Settings/AmPm';
import { Tools } from '../Settings/Tools';
import { Detection } from '../Settings/Detection';
import { PageLoader } from '../../Components/Loader';
import { DialogStore } from '../../services/DialogStore';
import { DrawerContent } from '../../Components/DrawerContent';
import { useDeviceOrientation } from '../../utils/useDeviceOrientation';
import { DispatchService, Region } from '../../services/DispatchService';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { RadioSelectDialog, RadioSelectDialogProps } from '../dialogs/RadioSelectDialog';
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
    constructor(private readonly dialogStore: DialogStore, private readonly dispatchService: DispatchService, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, { drawerItems: computed.struct }, { autoBind: true });
    }

    public get drawerItems() {
        return fromPromise(this.buildOptions());
    }

    public async markSettingsAsVisited() {
        await this.sharedPreferencesStore.setItem('oldU', 'true');
    }

    public async setRegion() {
        const initialValue = await this.dispatchService.getRegion();
        const options = Object.fromEntries(this.dispatchService.regions.map(region => [i18n.t(`dispatch:regions:${region}`), region] as const));
        const selectedRegion = await this.dialogStore.openDialog<RadioSelectDialogProps<Region>>(RadioSelectDialog, { title: i18n.t('settings:generalSettings:region'), options, initialValue });
        if (!selectedRegion) {
            return;
        }
        await this.dispatchService.setRegion(selectedRegion);
    }

    private async setLanguage() {
        const language = await this.dialogStore.openDialog<RadioSelectDialogProps<'he' | 'en'>>(RadioSelectDialog, {
            title: i18n.t('settings:generalSettings:selectLanguage'),
            options: { English: 'en', עברית: 'he' },
            initialValue: i18n.language as any,
        });
        if (!language) {
            return;
        }
        await i18n.changeLanguage(language);
    }

    private async getSettingsItems() {
        const { Mapp } = await this.sharedPreferencesStore.settingsChannel;
        return {
            isCallRecognitionOn: this.sharedPreferencesStore.makePrefBoolean(Mapp, false),
        };
    }

    private async buildOptions(): Promise<Item<SettingPage>[]> {
        const { isCallRecognitionOn } = await this.getSettingsItems();
        return [
            { type: 'link', icon: 'notification', label: 'settings:detection:menuName', page: 'Detection' },
            { type: 'link', icon: 'audio', label: 'settings:notification:menuName', page: 'Notification' },
            { type: 'link', icon: 'advanced', label: 'settings:advanced:menuName', page: 'Advanced' },
            { type: 'link', icon: 'clock', label: 'settings:ampm:menuName', page: 'AmPm' },
            { type: 'link', icon: 'edit', label: 'settings:tools:menuName', page: 'Tools' },
            { type: 'separator', label: 'settings:generalSettings:menuName' },
            {
                type: 'btn',
                label: `settings:generalSettings:${isCallRecognitionOn ? 'detectionEnabled' : 'detectionDisabled'}`,
                icon: isCallRecognitionOn ? 'detection' : 'detectionDisabled',
                onPress: Nullish,
            },
            { type: 'btn', label: 'settings:generalSettings:language', icon: 'translate', onPress: this.setLanguage },
            { type: 'btn', label: 'settings:generalSettings:region', icon: 'mada', onPress: this.setRegion },
        ];
    }
}

export const Settings = observer<NavigatableScreenProps<'Settings'>>(({ route }) => {
    const orientation = useDeviceOrientation();
    const localStore = useLocalStoreWithInjectedDependencies(SettingsLocalStore);
    useEffect(() => {
        localStore.markSettingsAsVisited();
        if (!route.params?.setRegion) {
            return;
        }
        localStore.setRegion();
    }, [localStore]); // eslint-disable-line react-hooks/exhaustive-deps

    if (localStore.drawerItems.state !== 'fulfilled') {
        return <PageLoader />;
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
            <Drawer.Screen name="AmPm" component={AmPm} />
            <Drawer.Screen name="Tools" component={Tools} />
        </Drawer.Navigator>
    );
});
Settings.displayName = 'Dispatch';
