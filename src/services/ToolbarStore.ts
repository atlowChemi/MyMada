import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { makeAutoObservable } from 'mobx';
import { NavigationStore } from './NavigationStore';
import type { TranslationKeys } from '../localization';

const noop = () => null;

interface NavBarIcon {
    key: string;
    icon: number;
    onPress(): void;
    rotatable?: boolean;
}

@injectable()
export class ToolbarStore {
    private dictionaryItemAdder: () => void = noop;
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
        if (this.navigationStore.currentPage === 'Tools') {
            return {
                key: 'drawer',
                onPress: this.navigationStore.openDrawer,
                icon: require('../assets/icons/menu.webp'),
            };
        }
        if (this.navigationStore.canGoBack) {
            return {
                key: 'back',
                rotatable: i18n.dir() === 'rtl',
                onPress: this.navigationStore.goBack,
                icon: require('../assets/icons/back_arrow.webp'),
            };
        }
        return undefined;
    }

    public get navIcons(): NavBarIcon[] {
        return [
            ...(true /* this.navigationStore.currentPage !== 'Settings' */ ? [{ key: 'settings', onPress: noop, icon: require('../assets/icons/settings.webp') }] : []),
            ...(this.navigationStore.currentSubPage === 'Dictionary'
                ? [
                      { key: 'search', onPress: noop, icon: require('../assets/icons/search.webp') },
                      { key: 'add', onPress: this.dictionaryItemAdder, icon: require('../assets/icons/add.webp') },
                  ]
                : []),
        ];
    }

    public setDictionaryItemAdder(onPress: () => void) {
        this.dictionaryItemAdder = onPress;
        return () => {
            this.dictionaryItemAdder = noop;
        };
    }
}
