import i18n from '../localization';
import { inject, injectable } from 'inversify';
import { computed, makeObservable } from 'mobx';
import { NavigationStore } from './NavigationStore';
import type { TranslationKeys } from '../localization';

@injectable()
export class ToolbarStore {
    @inject(NavigationStore) private readonly navigationStore!: NavigationStore;
    constructor() {
        makeObservable<this, 'titleTranslationKey'>(this, {
            titleTranslationKey: computed,
            title: computed,
            hideBack: computed,
            showDrawer: computed,
            hideSettings: computed,
        });
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

    public get hideBack() {
        return ['Home', 'Tools'].includes(this.navigationStore.currentPage);
    }

    public get showDrawer() {
        return this.navigationStore.currentPage === 'Tools';
    }

    public get hideSettings() {
        return false;
    }
}
