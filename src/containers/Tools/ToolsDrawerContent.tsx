import { useTranslation } from 'react-i18next';
import { observable, runInAction } from 'mobx';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { CommonActions, DrawerActions } from '@react-navigation/native';
import { DrawerContentScrollView, DrawerItem, DrawerItemList } from '@react-navigation/drawer';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Icon, IconName } from '../../Components/Icon';
import { useDependencyInjector } from '../../services/container';
import { NavigationStore, Tools } from '../../services/NavigationStore';
import { useEffect, type ComponentProps, useCallback } from 'react';
import type { TranslationKeys } from '../../localization';

type DrawerContentProps = ComponentProps<typeof DrawerItemList>;
const TOOL_DATA_MAP = new Map<Tools, { icon: IconName; translation: TranslationKeys }>([
    ['ContractionTimer', { icon: 'contractions', translation: 'tools:contraction:menuName' }],
    ['PulseCalc', { icon: 'pulse', translation: 'tools:pulse:menuName' }],
    ['MetronomeActivity', { icon: 'metronome', translation: 'tools:metronome:menuName' }],
    ['VitalsChecker', { icon: 'vital', translation: 'tools:vitals:menuName' }],
    ['OxygenCalculator', { icon: 'oxygen', translation: 'tools:oxygen:menuName' }],
    ['ApgarCalculator', { icon: 'apgar', translation: 'tools:apgar:menuName' }],
    ['GlazgowCalculator', { icon: 'glazgo', translation: 'tools:glazgow:menuName' }],
    ['Dictionary', { icon: 'dictionary', translation: 'tools:dictionary:menuName' }],
    ['BlsProtocoles', { icon: 'bls', translation: 'tools:protocols:bls' }],
    ['AlsProtocoles', { icon: 'als', translation: 'tools:protocols:als' }],
    ['Kuntres', { icon: 'dead', translation: 'tools:protocols:death' }],
]);

const DrawerItemWrapper = observer<DrawerContentProps & { page: Tools | 'ProtocolCategory' }>(({ page, state, navigation }) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(
        () => ({
            page,
            key: state.key,
            index: state.index,
            routes: state.routes,
            get indexOfRoute() {
                return this.routes.findIndex(({ name }) => name === page);
            },
            get isFocused() {
                return this.index === this.indexOfRoute;
            },
            navigate() {
                const { key, routes } = this;
                const { key: routeKey, name } = routes[this.indexOfRoute];
                const event = navigation?.emit({
                    type: 'drawerItemPress',
                    target: routeKey,
                    canPreventDefault: true,
                });

                if (!event?.defaultPrevented) {
                    navigation?.dispatch({
                        ...(this.isFocused ? DrawerActions.closeDrawer() : CommonActions.navigate({ name, merge: true })),
                        target: key,
                    });
                }
            },
        }),
        { routes: observable.ref },
    );

    useEffect(() => {
        runInAction(() => {
            localStore.page = page;
            localStore.key = state.key;
            localStore.index = state.index;
            localStore.routes = state.routes;
        });
    }, [state.key, state.index, state.routes, page, localStore]);

    if (page === 'ProtocolCategory') {
        return (
            <View style={styles.category}>
                <Text style={styles.categoryText}>{t('tools:protocols:menuName')}</Text>
            </View>
        );
    }

    const data = TOOL_DATA_MAP.get(page)!;

    return (
        <DrawerItem
            label={t(data.translation)}
            labelStyle={styles.label}
            icon={({ size, color }) => <Icon name={data.icon} size={size} color={color} />}
            onPress={localStore.navigate}
            focused={localStore.isFocused}
            activeTintColor={colors.navPickedTextColor}
        />
    );
});
DrawerItemWrapper.displayName = 'DrawerItemWrapper';

export const ToolsDrawerContent = observer<DrawerContentProps & { toolsToShow: (Tools | 'ProtocolCategory')[] }>(props => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const pressBack = useCallback(() => {
        props.navigation.closeDrawer();
        navigationStore.goHome();
    }, [navigationStore, props.navigation]);

    return (
        <DrawerContentScrollView {...props}>
            <DrawerItem label={t('common:toolbarBack')} labelStyle={styles.label} icon={({ size }) => <Icon name="backArrow" size={size} />} onPress={pressBack} />
            {props.toolsToShow.map(page => (
                <DrawerItemWrapper key={page} {...props} page={page} />
            ))}
        </DrawerContentScrollView>
    );
});
ToolsDrawerContent.displayName = 'Tools.DrawerContent';

const styles = StyleSheet.create({
    label: {
        fontFamily: 'opensanshebrew',
    },
    category: {
        borderTopWidth: 1,
        borderTopColor: colors.shadow,
        overflow: 'hidden',
        marginHorizontal: 10,
    },
    categoryText: {
        marginTop: 15,
        marginBottom: 9,
        color: colors.oldPrimaryDark,
    },
});
