import { useEffect } from 'react';
import { NavigationStore } from '../services/NavigationStore';
import { useDependencyInjector } from '../services/container';
import type { Page } from '../services/NavigationStore';
import type { NavigatableToolsScreenProps } from '../containers/Pages/Tools';
import type { NavigatableSettingsScreenProps } from '../containers/Pages/Settings';

export function useInnerNavigator(page: Extract<Page, 'Tools'>, props: Pick<NavigatableToolsScreenProps, 'navigation'>): void;
export function useInnerNavigator(page: Extract<Page, 'Settings'>, props: Pick<NavigatableSettingsScreenProps, 'navigation'>): void;
export function useInnerNavigator(page: Extract<Page, 'Settings' | 'Tools'>, props: Pick<NavigatableToolsScreenProps | NavigatableSettingsScreenProps, 'navigation'>): void {
    const navigationStore = useDependencyInjector(NavigationStore);

    useEffect(() => {
        if (page === 'Settings') {
            return navigationStore.setSettingsNavigator(props.navigation as NavigatableSettingsScreenProps['navigation']);
        }
        return navigationStore.setToolsNavigator(props.navigation as NavigatableToolsScreenProps['navigation']);
    }, [navigationStore, page, props.navigation]);
}
