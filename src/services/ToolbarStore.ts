import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { makeAutoObservable, runInAction } from 'mobx';
import { NavigationStore } from './NavigationStore';
import type { IconName } from '../Components/Icon';
import type { TranslationKeys } from '../localization';

const noop = () => null;

export interface NavBarIcon {
    key: string;
    icon: IconName;
    onPress(): void;
    show?: boolean;
}

@injectable()
export class ToolbarStore {
    public showTrashIcon = false;
    private isSearching = false;
    private _searchQuery = '';
    private dictionaryItemAdder: () => void = noop;
    private trashAction: () => void = noop;
    @inject(NavigationStore) private readonly navigationStore!: NavigationStore;

    constructor() {
        makeAutoObservable(this);
    }

    private get titleTranslationKey(): TranslationKeys {
        if (this.navigationStore.currentPage === 'Tools') {
            return 'tools:toolbarTitle';
        }
        if (this.navigationStore.currentPage === 'ContactUs') {
            return 'contact:toolbarTitle';
        }
        if (this.navigationStore.currentPage === 'About') {
            return 'aboutUs:toolbarTitle';
        }
        if (this.navigationStore.currentPage === 'Privacy') {
            return 'privacy:toolbarTitle';
        }
        return 'common:appName';
    }

    public get title() {
        return i18n.t(this.titleTranslationKey);
    }

    public get navBtn(): NavBarIcon | undefined {
        if (this.navigationStore.currentPage === 'Tools' && !this.isSearching) {
            return {
                key: 'drawer',
                onPress: this.navigationStore.openDrawer,
                icon: 'menu',
            };
        }
        if (this.navigationStore.canGoBack || this.isSearching) {
            return {
                key: 'back',
                onPress: this.isSearching ? this.closeSearch : this.navigationStore.goBack,
                icon: 'backArrow',
            };
        }
        return undefined;
    }

    public get navIcons(): NavBarIcon[] {
        return [
            { key: 'settings', onPress: noop, icon: 'settings', show: !['Lock', 'ActivateCode', 'Settings'].includes(this.navigationStore.currentPage) },
            { key: 'trash', onPress: this.trashAction, icon: 'trash', show: this.navigationStore.currentSubPage === 'ContractionTimer' && this.showTrashIcon },
            { key: 'search', onPress: this.search, icon: 'search', show: this.navigationStore.currentSubPage === 'Dictionary' && !this.isSearching },
            { key: 'add', onPress: this.dictionaryItemAdder, icon: 'add', show: this.navigationStore.currentSubPage === 'Dictionary' },
        ];
    }

    public get showSearch() {
        return this.isSearching;
    }

    public get searchQuery() {
        return this._searchQuery;
    }

    public setTrashAction(onPress: () => void) {
        runInAction(() => (this.trashAction = onPress));
        return () => runInAction(() => (this.trashAction = noop));
    }

    public setDictionaryItemAdder(onPress: () => void) {
        runInAction(() => (this.dictionaryItemAdder = onPress));
        return () =>
            runInAction(() => {
                this.dictionaryItemAdder = noop;
                this._searchQuery = '';
                this.isSearching = false;
            });
    }

    public setSearchQuery = (query: string) => {
        this._searchQuery = query.toLowerCase();
    };

    private search = () => {
        this.isSearching = true;
    };

    private closeSearch = () => {
        this.isSearching = false;
        this._searchQuery = '';
    };
}
