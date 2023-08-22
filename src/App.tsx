import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { HomePage } from './Pages/HomePage';
import { AboutTheApp } from './Pages/AboutTheApp';
import type { useNavigationBuilder } from '@react-navigation/native';

type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];
export interface NavigatableScreenProps {
    navigation: Navigation;
}
const Stack = createNativeStackNavigator();

export const App: React.FC = () => {
    return (
        <NavigationContainer>
            <Stack.Navigator initialRouteName="Home" screenOptions={{ header: () => null }}>
                <Stack.Screen name="Home" component={HomePage} />
                <Stack.Screen name="About" component={AboutTheApp} />
                {/* <Stack.Screen name="Profile" component={ProfileScreen} /> */}
            </Stack.Navigator>
        </NavigationContainer>
    );
};
