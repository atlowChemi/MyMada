import { fromPromise } from 'mobx-utils';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { createDrawerNavigator, DrawerScreenProps } from '@react-navigation/drawer';
import { isTablet } from '../../utils';
import { Metronome } from '../Tools/Metronome';
import { DocsViewer } from '../Tools/DocsViewer';
import { Dictionary } from '../Tools/Dictionary';
import { ApgarCalculator } from '../Tools/Apgar';
import { PulseCalculator } from '../Tools/Pulse';
import { VitalsCalculator } from '../Tools/Vitals';
import { OxygenCalculator } from '../Tools/Oxygen';
import { PageLoader } from '../../Components/Loader';
import { GlazgowCalculator } from '../Tools/Glazgow';
import { BasePageView } from '../../Components/BasePageView';
import { ContractionTimer } from '../Tools/ContractionTimer';
import { useDependencyInjector } from '../../services/container';
import { DrawerContent, Item } from '../../Components/DrawerContent';
import { useDeviceOrientation } from '../../utils/useDeviceOrientation';
import { TOOLS_PAGES, type Tools as ToolsType } from '../../services/NavigationStore';
import { SharedPreferencesStore, TOOL_SHARED_PREF_MAP } from '../../services/SharedPreferencesStore';
import type { NavigatableScreenProps } from '../../App';

type ProtocolPages = 'BlsProtocoles' | 'AlsProtocoles' | 'Kuntres';
type ToolsStackParamList = Record<Exclude<ToolsType, ProtocolPages>, undefined> & Record<Extract<ToolsType, ProtocolPages>, { type: 'bls' | 'als' | 'kuntres' }>;
export type NavigatableToolsScreenProps = DrawerScreenProps<ToolsStackParamList, ToolsType>;

const Drawer = createDrawerNavigator<ToolsStackParamList>();

const TOOL_DRAWER_DATA_MAP = new Map<ToolsType, Item<ToolsType>>([
    ['ContractionTimer', { type: 'link', icon: 'contractions', label: 'tools:contraction:menuName', page: 'ContractionTimer' }],
    ['PulseCalc', { type: 'link', icon: 'pulse', label: 'tools:pulse:menuName', page: 'PulseCalc' }],
    ['MetronomeActivity', { type: 'link', icon: 'metronome', label: 'tools:metronome:menuName', page: 'MetronomeActivity' }],
    ['VitalsChecker', { type: 'link', icon: 'vital', label: 'tools:vitals:menuName', page: 'VitalsChecker' }],
    ['OxygenCalculator', { type: 'link', icon: 'oxygen', label: 'tools:oxygen:menuName', page: 'OxygenCalculator' }],
    ['ApgarCalculator', { type: 'link', icon: 'apgar', label: 'tools:apgar:menuName', page: 'ApgarCalculator' }],
    ['GlazgowCalculator', { type: 'link', icon: 'glazgo', label: 'tools:glazgow:menuName', page: 'GlazgowCalculator' }],
    ['Dictionary', { type: 'link', icon: 'dictionary', label: 'tools:dictionary:menuName', page: 'Dictionary' }],
    ['BlsProtocoles', { type: 'link', icon: 'bls', label: 'tools:protocols:bls', page: 'BlsProtocoles' }],
    ['AlsProtocoles', { type: 'link', icon: 'als', label: 'tools:protocols:als', page: 'AlsProtocoles' }],
    ['Kuntres', { type: 'link', icon: 'dead', label: 'tools:protocols:death', page: 'Kuntres' }],
]);

export const Tools = observer<NavigatableScreenProps>(() => {
    const orientation = useDeviceOrientation();
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);
    const localStore = useLocalObservable(() => ({
        get initialPage() {
            return this.toolsToShow.case<ToolsType>({
                fulfilled: toolsToShow => {
                    const index = toolsToShow.findIndex(el => el.type === 'link' && el.page === 'MetronomeActivity');
                    if (index === -1) {
                        const [first] = toolsToShow;
                        return first.type !== 'link' ? 'BlsProtocoles' : first.page;
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
        async buildToolsList() {
            const toolsToShow = await sharedPreferencesStore.selectedTools;
            const hasProtocols = toolsToShow.has(TOOL_SHARED_PREF_MAP.get('BlsProtocoles')!);
            const toolsForDisplay: Item<ToolsType>[] = Array.from(TOOLS_PAGES)
                .filter(page => toolsToShow.has(TOOL_SHARED_PREF_MAP.get(page)!))
                .map(page => TOOL_DRAWER_DATA_MAP.get(page)!);
            if (hasProtocols) {
                const index = toolsForDisplay.indexOf(TOOL_DRAWER_DATA_MAP.get('BlsProtocoles')!);
                toolsForDisplay.splice(index, 0, { type: 'separator', label: 'tools:protocols:menuName' });
            }
            return toolsForDisplay;
        },
    }));

    if (localStore.toolsToShow.state !== 'fulfilled') {
        return <PageLoader />;
    }
    const toolsToShow = localStore.toolsToShow.value;
    return (
        <BasePageView scrollType="none">
            <Drawer.Navigator
                backBehavior="none"
                screenOptions={{ headerShown: false, drawerType: isTablet() && orientation === 'landscape' ? 'permanent' : 'front' }}
                drawerContent={props => <DrawerContent {...props} items={toolsToShow} />}
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
