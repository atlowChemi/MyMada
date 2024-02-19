import { observer } from 'mobx-react-lite';
import { createDrawerNavigator, DrawerScreenProps } from '@react-navigation/drawer';
import { isTablet } from '../../utils';
import { useDeviceOrientation } from '../../utils/useDeviceOrientation';
import type { NavigatableScreenProps } from '../../App';

export type SettingPage = 'Detection' | 'Notification' | 'Advanced' | 'AmPm' | 'Tools';
type SettingsStackParamList = Record<SettingPage, undefined>;
export type NavigatableSettingsScreenProps = DrawerScreenProps<SettingsStackParamList, SettingPage>;
export const SETTINGS_PAGES = new Set<SettingPage>(['Detection', 'Notification', 'Advanced', 'AmPm', 'Tools']);

const Drawer = createDrawerNavigator<SettingsStackParamList>();

const Nullish = () => null;

export const Settings = observer<NavigatableScreenProps>(() => {
    const orientation = useDeviceOrientation();

    return (
        <Drawer.Navigator backBehavior="none" screenOptions={{ headerShown: false, drawerType: isTablet() && orientation === 'landscape' ? 'permanent' : 'front' }}>
            {/* drawerContent={props => <DrawerContent {...props} toolsToShow={toolsToShow} />}> */}
            {/* <Drawer.Screen name="ContractionTimer" component={ContractionTimer} /> */}
            <Drawer.Screen name="Detection" component={Nullish} />
        </Drawer.Navigator>
    );
});
Settings.displayName = 'Dispatch';
