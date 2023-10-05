import { observer } from 'mobx-react-lite';
import { BasePageView } from '../../Components/BasePageView';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { Metronome } from '../Tools/Metronome';
import { DocsViewer } from '../Tools/DocsViewer';
import { Dictionary } from '../Tools/Dictionary';
import { ApgarCalculator } from '../Tools/Apgar';
import { PulseCalculator } from '../Tools/Pulse';
import { VitalsCalculator } from '../Tools/Vitals';
import { OxygenCalculator } from '../Tools/Oxygen';
import { GlazgowCalculator } from '../Tools/Glazgow';
import type { NavigatableScreenProps } from '../../App';

const Drawer = createDrawerNavigator();

export const Tools = observer<NavigatableScreenProps>(() => {
    return (
        <BasePageView scrollType="none">
            <Drawer.Navigator backBehavior="none" screenOptions={{ headerShown: false }} initialRouteName="MetronomeActivity">
                <Drawer.Screen name="ContractionTimer" component={Metronome} />
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
