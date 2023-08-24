import { injectable } from 'inversify';
import i18n, { CustomTypeOptions } from 'i18next';
import { action, observable, computed, makeObservable } from 'mobx';
import type { NavigationState } from '@react-navigation/native';

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

type Page = 'Home' | 'ContactUs' | 'About' | 'Privacy';

@injectable()
export class ToolbarStore {
    private currentPage: Page = 'Home';

    constructor() {
        makeObservable<this, 'currentPage' | 'titleTranslationKey'>(this, {
            currentPage: observable.ref,
            titleTranslationKey: computed,
            title: computed,
            hideBack: computed,
            showRoundBg: computed,
            hideSettings: computed,
            onNavigate: action.bound,
        });
    }

    private get titleTranslationKey(): Join<PathsToStringProps<CustomTypeOptions['resources']['he']>, ':'> {
        if (this.currentPage === 'ContactUs') {
            return 'contact:toolbarTitle';
        }
        if (this.currentPage === 'About') {
            return 'aboutUs:toolbarTitle';
        }
        if (this.currentPage === 'Privacy') {
            return 'privacy:toolbarTitle';
        }
        return 'common:appName';
    }

    public get title() {
        return i18n.t(this.titleTranslationKey);
    }

    public get hideBack() {
        return this.currentPage === 'Home';
    }

    public get showRoundBg() {
        return this.currentPage === 'Home';
    }

    public get hideSettings() {
        return false;
    }

    public onNavigate({ routes }: Pick<NavigationState, 'routes'> = { routes: [] }) {
        const currentRoute = routes?.at(-1);
        if (!currentRoute) {
            return;
        }
        this.currentPage = currentRoute.name as Page;
    }
}
