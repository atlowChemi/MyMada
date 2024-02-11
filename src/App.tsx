import { useEffect } from 'react';
import { StatusBar } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator, NativeStackScreenProps } from '@react-navigation/native-stack';
import { Tools } from './containers/Pages/Tools';
import { Privacy } from './containers/Pages/Privacy';
import { HomePage } from './containers/Pages/HomePage';
import { LockPage } from './containers/Pages/LockPage';
import { Dispatch } from './containers/Pages/Dispatch';
import { ContactUs } from './containers/Pages/ContactUs';
import { RequestCode } from './containers/Pages/RequestCode';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { CodeActivation } from './containers/Pages/CodeActivation';
import { DialogWrapper } from './services/DialogStore';
import { MessagingStore } from './services/MessagingStore';
import { MetronomeStore } from './services/MetronomeStore';
import { SnackbarWrapper } from './services/SnackbarStore';
import { NavigationStore, Page } from './services/NavigationStore';
import { DependencyInjectionProvider, container } from './services/container';
import { colors } from './consts';
import { Toolbar } from './containers/Toolbar';
import { setLanguage } from './localization';

const NOTIFICATION_HELPER_SOURCE = 'NOTIFICATION_HELPER';
type RootStackParamList = Record<Page, undefined>;
export type NavigatableScreenProps = NativeStackScreenProps<RootStackParamList, Page>;
const Stack = createNativeStackNavigator<RootStackParamList>();

export const App: React.FC = (props: any) => {
    const navigationStore = container.get(NavigationStore);
    const messagingStore = container.get(MessagingStore);
    const metronomeStore = container.get(MetronomeStore);
    setLanguage('he');

    useEffect(() => {
        const { source, target, navigateToTools = false } = props || {};
        const navigationFn = navigateToTools ? navigationStore.goToToolsTab : navigationStore.navigate;
        if (source === NOTIFICATION_HELPER_SOURCE && typeof target === 'string') {
            navigationFn(target as never);
        }
    }, [props, metronomeStore, navigationStore]);

    useEffect(() => messagingStore.setUpMessaging(), [messagingStore]);

    return (
        <DependencyInjectionProvider container={container}>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar />
            <DialogWrapper />
            <NavigationContainer ref={navigationStore.setNavigator}>
                <Stack.Navigator initialRouteName="Home" screenOptions={{ animation: 'none', headerShown: false }}>
                    <Stack.Screen name="Home" component={HomePage} options={{ orientation: 'portrait' }} />
                    <Stack.Screen name="Tools" component={Tools} />
                    <Stack.Screen name="Lock" component={LockPage} />
                    <Stack.Screen name="Dispatch" component={Dispatch} />
                    <Stack.Screen name="ContactUs" component={ContactUs} />
                    <Stack.Screen name="RequestCode" component={RequestCode} />
                    <Stack.Screen name="ActivateCode" component={CodeActivation} />
                    <Stack.Screen name="About" component={AboutTheApp} />
                    <Stack.Screen name="Privacy" component={Privacy} />
                </Stack.Navigator>
            </NavigationContainer>
            <SnackbarWrapper />
        </DependencyInjectionProvider>
    );
};
App.displayName = 'App';
