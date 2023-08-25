import { injectable } from 'inversify';
import { NavigationContainerRef } from '@react-navigation/native';
import { action, computed, makeObservable, observable, reaction } from 'mobx';

export type Page = 'Home' | 'ContactUs' | 'About' | 'Privacy';

@injectable()
export class NavigationStore {
    [Symbol.toStringTag] = 'NavigationStore';
    private page: Page = 'Home';
    public canGoBack = false;
    private eventListenerCleanup?: () => void;
    private navigator?: NavigationContainerRef<Record<Page, undefined>>;

    constructor() {
        makeObservable<NavigationStore, 'page' | 'onNavigate'>(this, {
            page: observable.ref,
            canGoBack: observable.ref,
            currentPage: computed,
            onNavigate: action.bound,
            setNavigator: action.bound,
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

    public goToContactUs = () => this.navigator?.navigate('ContactUs');
    public goToAboutTheApp = () => this.navigator?.navigate('About');
    public goToPrivacyPolicy = () => this.navigator?.navigate('Privacy');
    public goBack = () => this.navigator?.goBack();

    private onNavigate = () => {
        this.page = (this.navigator?.getCurrentRoute()?.name || 'Home') as Page;
    };
}
