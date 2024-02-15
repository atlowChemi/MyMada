import BuildConfig from 'react-native-build-config';
import i18n from '../localization';
import { Linking } from 'react-native';
import { inject, injectable } from 'inversify';
import { NavigationContainerRef } from '@react-navigation/native';
import { makeAutoObservable, observable, reaction, when } from 'mobx';
import { colors } from '../consts';
import { AdsStore } from './AdsStore';
import { DialogStore } from './DialogStore';
import { PermissionsStore } from './PermissionsStore';
import { BaseDialog } from '../containers/dialogs/BaseDialog';
import { SharedPreferencesStore } from './SharedPreferencesStore';
import { MadaNotificationsStore } from './MadaNotificationsStore';
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
    private subPage?: Tools = undefined;
    public canGoBack = false;
    private eventListenerCleanup?: () => void;
    private navigator?: NavigationContainerRef<Record<Page, undefined>>;
    private toolsNavigator?: DrawerNavigator = undefined;
    @inject(AdsStore) private readonly adsStore!: AdsStore;
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(MadaNotificationsStore) private readonly madaNotificationsStore!: MadaNotificationsStore;
    @inject(PermissionsStore) private readonly permissionsStore!: PermissionsStore;
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;

    constructor() {
        makeAutoObservable<NavigationStore, 'toolsNavigator'>(this, { toolsNavigator: observable.ref }, { autoBind: true });
        reaction(
            () => this.page,
            () => {
                this.canGoBack = this.navigator?.canGoBack() || false;
            },
        );
    }

    public get currentPage() {
        return this.page;
    }

    public get currentSubPage() {
        if (!['Tools'].includes(this.page)) {
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

    // @ts-expect-error generic inconvenience
    public navigate = <T extends Page = Page>(page: T, params?: NavigatableScreenProps<T>['route']['params']) => this.navigator?.navigate(page, params);
    public navigateWithoutHistory = (name: Page) => this.navigator?.reset({ index: name === 'Home' ? 0 : 1, routes: [{ name: 'Home' }, ...(name === 'Home' ? [] : [{ name }])] });

    public goHome = () => this.navigate('Home');
    public goToTools = () => this.navigate('Tools');
    public goToRequestCode = () => this.navigateWithoutHistory('RequestCode');
    public goToCodeActivation = () => this.navigateWithoutHistory('ActivateCode');
    public goToToolsTab = async (route: Tools, params?: Record<string, any>) => {
        this.goToTools();
        await when(() => this.toolsNavigator !== undefined);
        this.toolsNavigator?.jumpTo(route, params);
    };
    public goToDispatch = async () => {
        this.sharedPreferencesStore.name = 'Settings';
        const { realM } = await this.sharedPreferencesStore.activeChannel;
        const isMarkedAsVolunteer = this.sharedPreferencesStore.makePrefBoolean(realM, false);
        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE === 'debug' || BuildConfig.APPLICATION_ID?.endsWith('.debug')) {
            const navigateToLockPage = await this.dialogStore.openDialog(BaseDialog, {
                description: 'זוהי גרסה לבדיקה. עמוד חסימה או עמוד דיווחים',
                okText: 'עמוד חסימה',
                cancelText: 'עמוד דיווחים',
            });
            if (typeof navigateToLockPage === 'undefined') {
                return;
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
        this.sharedPreferencesStore.name = 'Settings';
        const { realM } = await this.sharedPreferencesStore.activeChannel;
        const isMarkedAsVolunteer = this.sharedPreferencesStore.makePrefBoolean(realM, false);
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
        await this.adsStore.showInterstitialAd();
        const permissionStatus = await this.permissionsStore.requestPermission('android.permission.POST_NOTIFICATIONS', {
            title: i18n.t('permissions:alertTitle'),
            message: i18n.t('permissions:notificationExplain'),
            buttonPositive: i18n.t('permissions:approve'),
            buttonNegative: i18n.t('permissions:deny'),
        });
        if (permissionStatus !== 'granted') {
            const goToSettings = await this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('permissions:alertTitle'),
                description: i18n.t('permissions:blocked'),
                cancelText: i18n.t('common:cancel'),
                okText: i18n.t('common:goToSettings'),
            });
            if (goToSettings) {
                const { packageName, uid } = await this.madaNotificationsStore.getAppData();
                await Linking.sendIntent('android.settings.APP_NOTIFICATION_SETTINGS', [
                    { key: 'app_uid', value: uid },
                    { key: 'app_package', value: packageName },
                    { key: 'android.provider.extra.APP_PACKAGE', value: packageName },
                ]);
            }
            return;
        }
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
    public openDrawer = () => this.toolsNavigator?.toggleDrawer();

    private isToolsTab = (route?: string): route is Tools => TOOLS_PAGES.has(route as Tools);

    private onNavigate = () => {
        const route = this.navigator?.getCurrentRoute()?.name as Page | Tools | undefined;
        if (this.isToolsTab(route)) {
            this.subPage = route;
            this.page = 'Tools';
            return;
        }
        this.page = route || 'Home';
    };
}
