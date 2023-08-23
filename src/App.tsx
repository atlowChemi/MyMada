import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomePage } from './containers/Pages/HomePage';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { Privacy } from './containers/Pages/Privacy';
import type { useNavigationBuilder } from '@react-navigation/native';

type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];
export interface NavigatableScreenProps {
    navigation: Navigation;
}
const Stack = createNativeStackNavigator();

export const App: React.FC = () => {
    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName="Home" screenOptions={{ animation: 'slide_from_right', headerShown: false }}>
                <Stack.Screen name="Home" component={HomePage} options={{ orientation: 'portrait' }} />
                <Stack.Screen name="About" component={AboutTheApp} />
                <Stack.Screen name="Privacy" component={Privacy} />
            </Stack.Navigator>
        </NavigationContainer>
    );
};
