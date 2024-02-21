import { observer } from 'mobx-react-lite';
import { createDrawerNavigator, DrawerScreenProps } from '@react-navigation/drawer';
import { isTablet } from '../../utils';
import { Detection } from '../Settings/Detection';
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
        <Drawer.Navigator backBehavior="none" initialRouteName="Detection" screenOptions={{ headerShown: false, drawerType: isTablet() && orientation === 'landscape' ? 'permanent' : 'front' }}>
            {/* drawerContent={props => <DrawerContent {...props} toolsToShow={toolsToShow} />}> */}
            <Drawer.Screen name="Detection" component={Detection} />
            <Drawer.Screen name="Notification" component={Nullish} />
            <Drawer.Screen name="Advanced" component={Nullish} />
            <Drawer.Screen name="AmPm" component={Nullish} />
            <Drawer.Screen name="Tools" component={Nullish} />
        </Drawer.Navigator>
    );
});
Settings.displayName = 'Dispatch';
