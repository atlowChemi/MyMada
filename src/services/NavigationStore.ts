import i18n from '../localization';
import { Linking } from 'react-native';
import { inject, injectable } from 'inversify';
import { NavigationContainerRef } from '@react-navigation/native';
import { makeAutoObservable, observable, reaction, when } from 'mobx';
import { PermissionsStore } from './PermissionsStore';
import { DialogStore } from './DialogStore';
import { BaseDialog } from '../containers/dialogs/BaseDialog';
import { SharedPreferencesStore } from './SharedPreferencesStore';
import { MadaNotificationsStore } from './MadaNotificationsStore';
import { colors } from '../consts';

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
export type Page = 'Home' | 'Tools' | 'ContactUs' | 'RequestCode' | 'About' | 'Privacy';

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

    public setToolsNavigator(navigator: DrawerNavigator) {
        this.toolsNavigator = navigator;
        return () => {
            this.toolsNavigator = undefined;
        };
    }

    public goHome = () => this.navigator?.navigate('Home');
    public goToTools = () => this.navigator?.navigate('Tools');
    public goToToolsTab = async (route: Tools) => {
        this.goToTools();
        await when(() => this.toolsNavigator !== undefined);
        this.toolsNavigator?.jumpTo(route);
    };
    public goToContactUs = async () => {
        this.sharedPreferencesStore.name = 'Settings';
        const { realM } = await this.sharedPreferencesStore.activeChannel;
        const isMarkedAsVolunteer = this.sharedPreferencesStore.makePrefBoolean(realM, false);
        if (!isMarkedAsVolunteer && !(await this.madaNotificationsStore.hasMadaApp())) {
            const navigateToContact = await this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('contact:plsNote'),
                okText: i18n.t('contact:toolbarTitle'),
                cancelText: i18n.t('contact:goToCodeRequest'),
                negativeBtnColor: colors.oldPrimary,
                description: i18n.t('contact:nonVolunteerMsg'),
            });
            if (typeof navigateToContact === 'undefined') {
                return;
            }
            if (!navigateToContact) {
                this.navigator?.navigate('RequestCode');
                return;
            }
        }
        // Open ad
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
                okText: i18n.t('permissions:goToSettings'),
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
        this.navigator?.navigate('ContactUs');
    };
    public goToAboutTheApp = () => /* open ad */ this.navigator?.navigate('About');
    public goToPrivacyPolicy = () => /* open ad */ this.navigator?.navigate('Privacy');
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
