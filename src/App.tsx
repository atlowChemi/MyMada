import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomePage } from './containers/Pages/HomePage';
import { Tools } from './containers/Pages/Tools';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { ContactUs } from './containers/Pages/ContactUs';
import { Privacy } from './containers/Pages/Privacy';
import { DependencyInjectionProvider, container } from './services/container';
import { NavigationStore } from './services/NavigationStore';
import type { useNavigationBuilder } from '@react-navigation/native';

type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];
export interface NavigatableScreenProps {
    navigation: Navigation;
}
const Stack = createNativeStackNavigator();

export const App: React.FC = () => {
    const navigationStore = container.get(NavigationStore);

    return (
        <DependencyInjectionProvider container={container}>
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
