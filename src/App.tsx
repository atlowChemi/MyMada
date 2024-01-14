import { useEffect } from 'react';
import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Tools } from './containers/Pages/Tools';
import { Privacy } from './containers/Pages/Privacy';
import { HomePage } from './containers/Pages/HomePage';
import { ContactUs } from './containers/Pages/ContactUs';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { DialogWrapper } from './services/DialogStore';
import { MetronomeStore } from './services/MetronomeStore';
import { SnackbarWrapper } from './services/SnackbarStore';
import { NavigationStore } from './services/NavigationStore';
import { DependencyInjectionProvider, container } from './services/container';
import { colors } from './consts';
import { Toolbar } from './containers/Toolbar';
import { setLanguage } from './localization';
import type { ParamListBase, RouteProp, useNavigationBuilder } from '@react-navigation/native';

const NOTIFICATION_HELPER_SOURCE = 'NOTIFICATION_HELPER';
type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];
export interface NavigatableScreenProps {
    route: RouteProp<ParamListBase>;
    navigation: Navigation;
}
const Stack = createNativeStackNavigator();

export const App: React.FC = (props: any) => {
    const navigationStore = container.get(NavigationStore);
    const metronomeStore = container.get(MetronomeStore);
    setLanguage('he');

    useEffect(() => {
        if (props.source === NOTIFICATION_HELPER_SOURCE) {
            navigationStore.goToToolsTab('MetronomeActivity');
        }
    }, [props, metronomeStore, navigationStore]);

    return (
        <DependencyInjectionProvider container={container}>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar />
            <DialogWrapper />
            <NavigationContainer ref={navigationStore.setNavigator}>
                <Stack.Navigator initialRouteName="Home" screenOptions={{ animation: 'none', headerShown: false }}>
                    <Stack.Screen name="Home" component={HomePage} options={{ orientation: 'portrait' }} />
                    <Stack.Screen name="Tools" component={Tools} />
                    <Stack.Screen name="ContactUs" component={ContactUs} />
                    <Stack.Screen name="About" component={AboutTheApp} />
                    <Stack.Screen name="Privacy" component={Privacy} />
                </Stack.Navigator>
            </NavigationContainer>
            <SnackbarWrapper />
        </DependencyInjectionProvider>
    );
};
App.displayName = 'App';
