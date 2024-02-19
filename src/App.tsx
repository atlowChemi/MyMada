import RadialGradient from 'react-native-radial-gradient';
import { FC, useEffect } from 'react';
import { PortalProvider } from '@gorhom/portal';
import { StatusBar, StyleSheet } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator, NativeStackScreenProps } from '@react-navigation/native-stack';
import { colors } from './consts';
import { Tools } from './containers/Pages/Tools';
import { Privacy } from './containers/Pages/Privacy';
import { HomePage } from './containers/Pages/HomePage';
import { LockPage } from './containers/Pages/LockPage';
import { Dispatch, DispatchPage } from './containers/Pages/Dispatch';
import { ContactUs } from './containers/Pages/ContactUs';
import { RequestCode } from './containers/Pages/RequestCode';
import { AboutTheApp } from './containers/Pages/AboutTheApp';
import { CodeActivation } from './containers/Pages/CodeActivation';
import { Settings } from './containers/Pages/Settings';
import { DialogWrapper } from './services/DialogStore';
import { MessagingStore } from './services/MessagingStore';
import { MetronomeStore } from './services/MetronomeStore';
import { SnackbarWrapper } from './services/SnackbarStore';
import { DependencyInjectionProvider, useDependencyInjector, container } from './services/container';
import { NavigationStore, Page, Tools as ToolsType } from './services/NavigationStore';
import { Toolbar } from './containers/Toolbar';
import { languageSetup } from './localization';
import { AppPropsStore } from './services/AppPropsStore';
import { Loader } from './Components/Loader';
import { observer } from 'mobx-react-lite';

const NOTIFICATION_HELPER_SOURCE = 'NOTIFICATION_HELPER';
type NavigatablePage = 'Dispatch' | 'Settings' | 'Tools';
// TODO: add Settings page types
type RootStackParamList = Record<Exclude<Page, NavigatablePage>, undefined> & { Dispatch: { page?: DispatchPage } } & { Settings: { page?: string } } & { Tools: { page?: ToolsType } };
export type NavigatableScreenProps<T extends Page = Page> = NativeStackScreenProps<RootStackParamList, T>;
const Stack = createNativeStackNavigator<RootStackParamList>();

export const AppWithDI = observer((props: any) => {
    const appPropsStore = useDependencyInjector(AppPropsStore);
    const navigationStore = useDependencyInjector(NavigationStore);
    const messagingStore = useDependencyInjector(MessagingStore);
    const metronomeStore = useDependencyInjector(MetronomeStore);

    useEffect(() => {
        const { source, target, navigateToTools = false } = props || {};
        const navigationFn = navigateToTools ? navigationStore.goToToolsTab : navigationStore.navigate;
        if (source === NOTIFICATION_HELPER_SOURCE && typeof target === 'string') {
            navigationFn(target as never, props);
        }
    }, [props, metronomeStore, navigationStore]);

    useEffect(() => appPropsStore.setProps(props), [appPropsStore, props]);
    useEffect(() => messagingStore.setUpMessaging(), [messagingStore]);

    if (languageSetup.state !== 'fulfilled') {
        return (
            <RadialGradient colors={[colors.white, colors.bgShadowEnd]} stops={[0.9]} style={styles.loaderContainer}>
                <Loader />
            </RadialGradient>
        );
    }

    return (
        <>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar />
            <DialogWrapper />
            <PortalProvider>
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
                        <Stack.Screen name="Settings" component={Settings} />
                    </Stack.Navigator>
                </NavigationContainer>
            </PortalProvider>
            <SnackbarWrapper />
        </>
    );
});
AppWithDI.displayName = 'AppWithDI';

export const App: FC = (props: any) => {
    return (
        <DependencyInjectionProvider container={container}>
            <AppWithDI {...props} />
        </DependencyInjectionProvider>
    );
};
App.displayName = 'App';

const styles = StyleSheet.create({
    loaderContainer: {
        flex: 1,
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        position: 'absolute',
        alignItems: 'center',
        justifyContent: 'center',
    },
});
