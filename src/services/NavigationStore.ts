import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { makeAutoObservable, reaction } from 'mobx';
import { NavigationContainerRef } from '@react-navigation/native';
import { PermissionsStore } from './PermissionsStore';
import { DialogStore } from './DialogStore';
import { BaseDialog } from '../containers/dialogs/BaseDialog';

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
export type Page = 'Home' | 'Tools' | 'ContactUs' | 'About' | 'Privacy';

const TOOLS_PAGES = new Set<Tools>([
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

@injectable()
export class NavigationStore {
    private page: Page = 'Home';
    private subPage?: Tools = undefined;
    public canGoBack = false;
    private eventListenerCleanup?: () => void;
    private navigator?: NavigationContainerRef<Record<Page, undefined>>;
    private toolsNavigator?: { toggleDrawer(): void };
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(PermissionsStore) private readonly permissionsStore!: PermissionsStore;

    constructor() {
        makeAutoObservable<NavigationStore, 'onNavigate'>(this, undefined, { autoBind: true });
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

    public setToolsNavigator(navigator: { toggleDrawer(): void }) {
        this.toolsNavigator = navigator;
        return () => {
            this.toolsNavigator = undefined;
        };
    }

    public goToTools = () => this.navigator?.navigate('Tools');
    public goToContactUs = async () => {
        const shouldNavigate = await this.permissionsStore.requestPermission('android.permission.POST_NOTIFICATIONS');
        if (!shouldNavigate) {
            // TODO: add a link to the permissions page
            this.dialogStore.openDialog(BaseDialog, {
                title: i18n.t('permissions:alertTitle'),
                description: i18n.t('permissions:blocked'),
                okText: i18n.t('common:ok'),
            });
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
