import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { Tools } from './containers/Pages/Tools';
import { Privacy } from './containers/Pages/Privacy';
import { HomePage } from './containers/Pages/HomePage';
import { ContactUs } from './containers/Pages/ContactUs';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { NavigationStore } from './services/NavigationStore';
import { DependencyInjectionProvider, container } from './services/container';
import { colors } from './consts';
import { Toolbar } from './containers/Toolbar';
import type { useNavigationBuilder } from '@react-navigation/native';
import { setLanguage } from './localization';

type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];
export interface NavigatableScreenProps {
    navigation: Navigation;
}
const Stack = createNativeStackNavigator();

export const App: React.FC = () => {
    const navigationStore = container.get(NavigationStore);
    setLanguage('he');

    return (
        <DependencyInjectionProvider container={container}>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar />
            <NavigationContainer ref={navigationStore.setNavigator}>
                <Stack.Navigator initialRouteName="Home" screenOptions={{ animation: 'none', headerShown: false }}>
                    <Stack.Screen name="Home" component={HomePage} options={{ orientation: 'portrait' }} />
                    <Stack.Screen name="Tools" component={Tools} />
                    <Stack.Screen name="ContactUs" component={ContactUs} />
                    <Stack.Screen name="About" component={AboutTheApp} />
                    <Stack.Screen name="Privacy" component={Privacy} />
                </Stack.Navigator>
            </NavigationContainer>
        </DependencyInjectionProvider>
    );
};
