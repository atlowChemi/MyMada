import { observer } from 'mobx-react-lite';
import { BasePageView } from '../../Components/BasePageView';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { Metronome } from '../Tools/Metronome';
import { ApgarCalculator } from '../Tools/Apgar';
import { PulseCalculator } from '../Tools/Pulse';
import { OxygenCalculator } from '../Tools/Oxygen';
import type { NavigatableScreenProps } from '../../App';

const Drawer = createDrawerNavigator();

export const Tools = observer<NavigatableScreenProps>(() => {
    return (
        <BasePageView noScroll>
            <Drawer.Navigator backBehavior="none" screenOptions={{ headerShown: false }} initialRouteName="Metronome">
                <Drawer.Screen name="ContractionTimer" component={Metronome} />
                <Drawer.Screen name="PulseCalc" component={PulseCalculator} />
                <Drawer.Screen name="MetronomeActivity" component={Metronome} />
                <Drawer.Screen name="VitalsChecker" component={Metronome} />
                <Drawer.Screen name="OxygenCalculator" component={OxygenCalculator} />
                <Drawer.Screen name="ApgarCalculator" component={ApgarCalculator} />
                <Drawer.Screen name="GlazgoCalculator" component={Metronome} />
                <Drawer.Screen name="Dictionary" component={Metronome} />
                <Drawer.Screen name="BlsProtocoles" component={Metronome} />
                <Drawer.Screen name="AlsProtocoles" component={Metronome} />
                <Drawer.Screen name="Kuntres" component={Metronome} />
            </Drawer.Navigator>
        </BasePageView>
    );
});
