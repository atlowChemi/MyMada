import { fromPromise } from 'mobx-utils';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { BasePageView } from '../../Components/BasePageView';
import { Loader } from '../../Components/Loader';
import { Metronome } from '../Tools/Metronome';
import { DocsViewer } from '../Tools/DocsViewer';
import { Dictionary } from '../Tools/Dictionary';
import { ApgarCalculator } from '../Tools/Apgar';
import { PulseCalculator } from '../Tools/Pulse';
import { VitalsCalculator } from '../Tools/Vitals';
import { OxygenCalculator } from '../Tools/Oxygen';
import { GlazgowCalculator } from '../Tools/Glazgow';
import { ContractionTimer } from '../Tools/ContractionTimer';
import { DrawerContent } from '../Tools/DrawerContent';
import { useDependencyInjector } from '../../services/container';
import { useDeviceOrientation } from '../../utils/useDeviceOrientation';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { TOOLS_PAGES, type Tools as ToolsType } from '../../services/NavigationStore';
import type { NavigatableScreenProps } from '../../App';
import { isTablet } from '../../utils';

const Drawer = createDrawerNavigator();

const TOOL_SETTING_MAP = new Map<ToolsType, string>([
    ['ContractionTimer', 'contrac'],
    ['PulseCalc', 'pulse'],
    ['MetronomeActivity', 'metro'],
    ['VitalsChecker', 'vital'],
    ['OxygenCalculator', 'oxy'],
    ['ApgarCalculator', 'apgar'],
    ['GlazgowCalculator', 'glaz'],
    ['Dictionary', 'dict'],
    ['BlsProtocoles', 'prot'],
    ['AlsProtocoles', 'prot'],
    ['Kuntres', 'prot'],
]);

export const Tools = observer<NavigatableScreenProps>(() => {
    const orientation = useDeviceOrientation();
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);
    const localStore = useLocalObservable(() => ({
        get initialPage() {
            return this.toolsToShow.case({
                fulfilled: toolsToShow => {
                    const index = toolsToShow.findIndex(el => el === 'MetronomeActivity');
                    if (index === -1) {
                        return toolsToShow[index];
                    }
                    return 'MetronomeActivity';
                },
                pending: () => 'MetronomeActivity',
                rejected: () => 'MetronomeActivity',
            });
        },
        get toolsToShow() {
            return fromPromise(this.buildToolsList());
        },
        async loadToolsToShow() {
            sharedPreferencesStore.name = 'Settings';
            const sharedPreferences = await sharedPreferencesStore.activeChannel;
            const { Tools: toolsArrString = `[${Array.from(TOOL_SETTING_MAP.values()).join(', ')}]` } = sharedPreferences;
            const [, toolsString] = toolsArrString.split(/[[\]]/);
            return new Set(toolsString.split(/,\s?/));
        },
        async buildToolsList() {
            const toolsToShow = await this.loadToolsToShow();
            const hasProtocols = toolsToShow.has('prot');
            const toolsForDisplay: (ToolsType | 'ProtocolCategory')[] = Array.from(TOOLS_PAGES).filter(page => toolsToShow.has(TOOL_SETTING_MAP.get(page)!));
            if (hasProtocols) {
                const index = toolsForDisplay.indexOf('BlsProtocoles');
                toolsForDisplay.splice(index, 0, 'ProtocolCategory');
            }
            return toolsForDisplay;
        },
    }));

    if (localStore.toolsToShow.state !== 'fulfilled') {
        return (
            <BasePageView scrollType="none">
                <View style={styles.loaderWrapper}>
                    <Loader />
                </View>
            </BasePageView>
        );
    }
    const toolsToShow = localStore.toolsToShow.value;
    // If tablet lock drawer open
    return (
        <BasePageView scrollType="none">
            <Drawer.Navigator
                backBehavior="none"
                screenOptions={{ headerShown: false, drawerType: isTablet() && orientation === 'landscape' ? 'permanent' : 'front' }}
                drawerContent={props => <DrawerContent {...props} toolsToShow={toolsToShow} />}
                initialRouteName={localStore.initialPage}>
                <Drawer.Screen name="ContractionTimer" component={ContractionTimer} />
                <Drawer.Screen name="PulseCalc" component={PulseCalculator} />
                <Drawer.Screen name="MetronomeActivity" component={Metronome} />
                <Drawer.Screen name="VitalsChecker" component={VitalsCalculator} />
                <Drawer.Screen name="OxygenCalculator" component={OxygenCalculator} />
                <Drawer.Screen name="ApgarCalculator" component={ApgarCalculator} />
                <Drawer.Screen name="GlazgowCalculator" component={GlazgowCalculator} />
                <Drawer.Screen name="Dictionary" component={Dictionary} />
                <Drawer.Screen name="BlsProtocoles" initialParams={{ type: 'bls' }} component={DocsViewer} />
                <Drawer.Screen name="AlsProtocoles" initialParams={{ type: 'als' }} component={DocsViewer} />
                <Drawer.Screen name="Kuntres" initialParams={{ type: 'kuntres' }} component={DocsViewer} />
            </Drawer.Navigator>
        </BasePageView>
    );
});
Tools.displayName = 'Tools';

const styles = StyleSheet.create({
    loaderWrapper: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});
