import analytics from '@react-native-firebase/analytics';
import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { PermissionsAndroid } from 'react-native';
import { NavigationContainerRef } from '@react-navigation/native';
import { makeAutoObservable, observable, reaction, when } from 'mobx';
import { colors } from '../consts';
import { AdsStore } from './AdsStore';
import { DialogStore } from './DialogStore';
import { NotificationService } from './NotificationService';
import { BaseDialog } from '../containers/dialogs/BaseDialog';
import { SharedPreferencesStore } from './SharedPreferencesStore';
import { MadaNotificationsStore } from './MadaNotificationsStore';
import { NavigatableSettingsScreenProps, SETTINGS_PAGES, SettingPage } from '../containers/Pages/Settings';
import type { NavigatableScreenProps } from '../App';
import type { NavigatableToolsScreenProps } from '../containers/Pages/Tools';

export type Tools =
    | 'ContractionTimer'
    | 'PulseCalc'
    | 'MetronomeActivity'
    | 'VitalsChecker'
    | 'OxygenCalculator'
    | 'ApgarCalculator'
    | 'GlazgowCalculator'
    | 'Dictionary'
    | 'BlsProtocoles'
    | 'AlsProtocoles'
    | 'Kuntres';
export type Page = 'Home' | 'Tools' | 'Lock' | 'Dispatch' | 'ContactUs' | 'RequestCode' | 'ActivateCode' | 'About' | 'Privacy' | 'Settings';

export const TOOLS_PAGES = new Set<Tools>([
    'ContractionTimer',
    'PulseCalc',
    'MetronomeActivity',
    'VitalsChecker',
    'OxygenCalculator',
    'ApgarCalculator',
    'GlazgowCalculator',
    'Dictionary',
    'BlsProtocoles',
    'AlsProtocoles',
    'Kuntres',
]);
interface DrawerNavigator {
    openDrawer(): void;
    closeDrawer(): void;
    toggleDrawer(): void;
    jumpTo(route: Tools, params?: Record<string, any>): void;
}

@injectable()
export class NavigationStore {
    private page: Page = 'Home';
    private subPage?: Tools | SettingPage = undefined;
    public canGoBack = false;
    private eventListenerCleanup?: () => void;
    private navigator?: NavigationContainerRef<Record<Page, undefined>>;
    private toolsNavigator?: DrawerNavigator = undefined;
    private settingsNavigator?: NavigatableSettingsScreenProps['navigation'] = undefined;
    @inject(AdsStore) private readonly adsStore!: AdsStore;
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(MadaNotificationsStore) private readonly madaNotificationsStore!: MadaNotificationsStore;
    @inject(NotificationService) private readonly notificationService!: NotificationService;
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;

    constructor() {
        makeAutoObservable<NavigationStore, 'toolsNavigator'>(this, { toolsNavigator: observable.ref }, { autoBind: true });
        reaction(
            () => this.page,
            async () => {
                this.canGoBack = this.navigator?.canGoBack() || false;
                await analytics().logScreenView({ screen_name: this.page, screen_class: this.page });
            },
        );
    }

    public get currentPage() {
        return this.page;
    }

    public get currentSubPage() {
        if (!['Tools', 'Settings'].includes(this.page)) {
            return undefined;
        }
        return this.subPage || this.navigator?.getCurrentRoute()?.name;
    }

    public setNavigator(navigator: NavigationContainerRef<any>) {
        this.eventListenerCleanup?.();
        this.navigator = navigator;
        this.canGoBack = navigator?.canGoBack() || false;
        this.eventListenerCleanup = navigator?.addListener('state', this.onNavigate);
    }

    public setToolsNavigator(navigator: NavigatableToolsScreenProps['navigation']) {
        this.toolsNavigator = navigator;
        return () => {
            this.toolsNavigator = undefined;
        };
    }

    public setSettingsNavigator(navigator: NavigatableSettingsScreenProps['navigation']) {
        this.settingsNavigator = navigator;
        return () => {
            this.settingsNavigator = undefined;
        };
    }

    // @ts-expect-error generic inconvenience
    public navigate = <T extends Page = Page>(page: T, params?: NavigatableScreenProps<T>['route']['params']) => this.navigator?.navigate(page, params);
    public navigateWithoutHistory = <T extends Page = Page>(name: T, params?: NavigatableScreenProps<T>['route']['params']) => {
        const isHome = name === 'Home';
        const index = isHome ? 0 : 1;
        const routes = [{ name: 'Home', ...(isHome && { params }) }, ...(isHome ? [] : [{ name, params }])];
        this.navigator?.reset({ index, routes });
    };

    public goHome = () => this.navigate('Home');
    public goToTools = () => this.navigate('Tools');
    public goToSettings = () => this.navigate('Settings');
    public goToRequestCode = () => this.navigateWithoutHistory('RequestCode');
    public goToCodeActivation = () => this.navigateWithoutHistory('ActivateCode');
    public goToToolsTab = async <T extends Tools = Tools>(route: T, params?: NavigatableToolsScreenProps<T>['route']['params']) => {
        this.goToTools();
        await when(() => this.toolsNavigator !== undefined);
        this.toolsNavigator?.jumpTo(route, params);
    };
    public goToSettingsTab = async (route: SettingPage) => {
        this.goToSettings();
        await when(() => this.settingsNavigator !== undefined);
        this.settingsNavigator?.jumpTo(route);
    };
    public goToDispatch = async () => {
        const {
            general: { isMarkedAsVolunteer },
        } = await this.sharedPreferencesStore.applicationSettings;
        const { DEBUG, BUILD_TYPE, APPLICATION_ID } = this.madaNotificationsStore.appData;
        if (DEBUG || BUILD_TYPE === 'debug' || APPLICATION_ID?.endsWith('.debug')) {
            const navigateToLockPage = await this.dialogStore.openDialog(BaseDialog, {
                description: 'זוהי גרסה לבדיקה. עמוד חסימה או עמוד דיווחים',
                okText: 'עמוד חסימה',
                cancelText: 'עמוד דיווחים',
            });
            if (typeof navigateToLockPage === 'undefined') {
                return;
            }
            if (!navigateToLockPage && !isMarkedAsVolunteer) {
                await this.sharedPreferencesStore.setItem('realM', 'true');
            }
            return this.navigate(navigateToLockPage ? 'Lock' : 'Dispatch');
        }
        if (isMarkedAsVolunteer || (await this.madaNotificationsStore.hasMadaApp())) {
            await this.sharedPreferencesStore.setItem('realM', 'true');
            return this.navigate('Dispatch');
        }
        return this.navigate('Lock');
    };
    public goToContactUs = async () => {
        const {
            general: { isMarkedAsVolunteer },
        } = await this.sharedPreferencesStore.applicationSettings;
        if (!isMarkedAsVolunteer && !(await this.madaNotificationsStore.hasMadaApp())) {
            const navigateToContact = await this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('contact:plsNote'),
                okText: i18n.t('contact:toolbarTitle'),
                cancelText: i18n.t('activationCode:goToCodeRequest'),
                negativeBtnColor: colors.oldPrimary,
                description: i18n.t('contact:nonVolunteerMsg'),
            });
            if (typeof navigateToContact === 'undefined') {
                return;
            }
            if (!navigateToContact) {
                return this.goToRequestCode();
            }
        }
        const { status } = await this.notificationService.requestNotificationPermission(this.madaNotificationsStore.appData);
        if (status !== PermissionsAndroid.RESULTS.GRANTED) {
            return;
        }
        await this.adsStore.showInterstitialAd();
        return this.navigate('ContactUs');
    };
    public goToAboutTheApp = async () => {
        await this.adsStore.showInterstitialAd();
        return this.navigate('About');
    };
    public goToPrivacyPolicy = async () => {
        await this.adsStore.showInterstitialAd();
        return this.navigate('Privacy');
    };
    public goBack = () => this.navigator?.goBack();
    public openDrawer = () => this.toolsNavigator?.toggleDrawer() || this.settingsNavigator?.toggleDrawer();

    private isToolsTab = (route?: string): route is Tools => TOOLS_PAGES.has(route as Tools);
    private isSettingsTab = (route?: string): route is SettingPage => SETTINGS_PAGES.has(route as SettingPage);

    private onNavigate = () => {
        const route = this.navigator?.getCurrentRoute()?.name as Page | Tools | undefined;
        if (this.isToolsTab(route)) {
            this.subPage = route;
            this.page = 'Tools';
            return;
        }
        if (this.isSettingsTab(route)) {
            this.subPage = route;
            this.page = 'Settings';
            return;
        }
        this.page = route || 'Home';
    };
}
