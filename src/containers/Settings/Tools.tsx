import { computedFn } from 'mobx-utils';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Snackbar } from '../../Components/Snackbar';
import { PageLoader } from '../../Components/Loader';
import { TOOLS_PAGES } from '../../services/NavigationStore';
import { SettingsItem } from '../../Components/SettingsItem';
import { BasePageView } from '../../Components/BasePageView';
import { SnackbarStore } from '../../services/SnackbarStore';
import { useDependencyInjector } from '../../services/container';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SharedPreferencesStore, TOOL_SHARED_PREF_MAP } from '../../services/SharedPreferencesStore';
import type { TranslationKeys } from '../../localization';
import type { NavigatableSettingsScreenProps } from '../Pages/Settings';
import type { Tools as ToolsType } from '../../services/NavigationStore';

const TOOL_LABEL_MAP = new Map<ToolsType, TranslationKeys>([
    ['ContractionTimer', 'tools:contraction:menuName'],
    ['PulseCalc', 'tools:pulse:menuName'],
    ['MetronomeActivity', 'tools:metronome:menuName'],
    ['VitalsChecker', 'tools:vitals:menuName'],
    ['OxygenCalculator', 'tools:oxygen:menuName'],
    ['ApgarCalculator', 'tools:apgar:menuName'],
    ['GlazgowCalculator', 'tools:glazgow:menuName'],
    ['Dictionary', 'tools:dictionary:menuName'],
    ['BlsProtocoles', 'tools:protocols:menuName'],
]);

export const Tools = observer<NavigatableSettingsScreenProps>(props => {
    const { t } = useTranslation();
    useInnerNavigator('Settings', props);
    const sharedPreferencesStore = useDependencyInjector(SharedPreferencesStore);
    const snackbarStore = useDependencyInjector(SnackbarStore);
    const localStore = useLocalObservable(() => ({
        get selectedTools() {
            return sharedPreferencesStore.selectedTools.case({
                pending: () => new Set<string>(),
                rejected: () => new Set<string>(),
                fulfilled: selectedTools => selectedTools,
            });
        },
        get value() {
            return computedFn((tool: ToolsType) => this.selectedTools.has(TOOL_SHARED_PREF_MAP.get(tool)!));
        },
        get onChange() {
            return computedFn((tool: ToolsType) => (value: boolean) => {
                const values = Object.fromEntries(Array.from(TOOLS_PAGES, val => [TOOL_SHARED_PREF_MAP.get(val)!, this.value(val)]));
                values[TOOL_SHARED_PREF_MAP.get(tool)!] = value;
                if (!Object.values(values).some(Boolean)) {
                    return snackbarStore.openSnack(Snackbar, { msg: t('settings:tools:minimumMsg'), timeout: 5000 });
                }
                const toolsStr = Object.entries(values)
                    .filter(([, val]) => val)
                    .map(([key]) => key)
                    .join(', ');
                return sharedPreferencesStore.setItem('Tools', `[${toolsStr}]`);
            });
        },
    }));

    if (sharedPreferencesStore.selectedTools.state === 'pending') {
        return <PageLoader />;
    }

    return (
        <BasePageView scrollContentStyle={styles.content}>
            <Text style={styles.pageTitle}>{t('settings:tools:pageTitle')}</Text>
            {Array.from(TOOL_LABEL_MAP.entries(), ([tool, labelKey]) => (
                <SettingsItem key={tool} type="switch" label={t(labelKey)} value={localStore.value(tool)} onChange={localStore.onChange(tool)} />
            ))}
        </BasePageView>
    );
});
Tools.displayName = 'Settings.Tools';

const styles = StyleSheet.create({
    content: {
        rowGap: 0,
        paddingHorizontal: 0,
    },
    pageTitle: {
        fontSize: 16,
        marginBottom: 8,
        color: colors.ocean,
        paddingHorizontal: 15,
    },
});
