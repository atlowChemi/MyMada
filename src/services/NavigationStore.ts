import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { NavigationContainerRef } from '@react-navigation/native';
import { action, computed, makeObservable, observable, reaction } from 'mobx';
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

@injectable()
export class NavigationStore {
    private page: Page = 'Home';
    public canGoBack = false;
    private eventListenerCleanup?: () => void;
    private navigator?: NavigationContainerRef<Record<Page, undefined>>;
    private toolsNavigator?: { toggleDrawer(): void };
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(PermissionsStore) private readonly permissionsStore!: PermissionsStore;

    constructor() {
        makeObservable<NavigationStore, 'page' | 'onNavigate'>(this, {
            page: observable.ref,
            canGoBack: observable.ref,
            currentPage: computed,
            onNavigate: action.bound,
            setNavigator: action.bound,
            setToolsNavigator: action.bound,
        });
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
    public goToAboutTheApp = () => this.navigator?.navigate('About');
    public goToPrivacyPolicy = () => this.navigator?.navigate('Privacy');
    public goBack = () => this.navigator?.goBack();
    public openDrawer = () => this.toolsNavigator?.toggleDrawer();

    private onNavigate = () => {
        const route = this.navigator?.getCurrentRoute()?.name;
        const innerNavigationMap: Record<string, Page> = {
            ContractionTimer: 'Tools',
            PulseCalc: 'Tools',
            MetronomeActivity: 'Tools',
            VitalsChecker: 'Tools',
            OxygenCalculator: 'Tools',
            ApgarCalculator: 'Tools',
            GlazgowCalculator: 'Tools',
            Dictionary: 'Tools',
            BlsProtocoles: 'Tools',
            AlsProtocoles: 'Tools',
            Kuntres: 'Tools',
        } satisfies Record<Tools, Page>;
        const pageParent = innerNavigationMap[route || ''];
        this.page = pageParent || route || 'Home';
    };
}
