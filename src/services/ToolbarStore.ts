import { inject, injectable } from 'inversify';
import { computed, makeObservable } from 'mobx';
import i18n, { CustomTypeOptions } from 'i18next';
import { NavigationStore } from './NavigationStore';

type PathsToStringProps<T> = T extends string ? [] : { [K in Extract<keyof T, string>]: [K, ...PathsToStringProps<T[K]>] }[Extract<keyof T, string>];
type Join<T extends string[], D extends string> = T extends []
    ? never
    : T extends [infer F]
    ? F
    : T extends [infer F, ...infer R]
    ? F extends string
        ? `${F}${D}${Join<Extract<R, string[]>, D>}`
        : never
    : string;

@injectable()
export class ToolbarStore {
    @inject(NavigationStore) private readonly navigationStore!: NavigationStore;
    constructor() {
        makeObservable<this, 'titleTranslationKey'>(this, {
            titleTranslationKey: computed,
            title: computed,
            hideBack: computed,
            showRoundBg: computed,
            hideSettings: computed,
        });
    }

    private get titleTranslationKey(): Join<PathsToStringProps<CustomTypeOptions['resources']['he']>, ':'> {
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
        return this.navigationStore.currentPage === 'Home';
    }

    public get showRoundBg() {
        return this.navigationStore.currentPage === 'Home';
    }

    public get hideSettings() {
        return false;
    }
}
