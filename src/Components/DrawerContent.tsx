import { useTranslation } from 'react-i18next';
import { observable, runInAction } from 'mobx';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { CommonActions, DrawerActions } from '@react-navigation/native';
import { DrawerContentScrollView, DrawerItem, DrawerItemList } from '@react-navigation/drawer';
import { colors } from '../consts';
import { Text } from './Text';
import { Icon, IconName } from './Icon';
import { NavigationStore } from '../services/NavigationStore';
import { useDependencyInjector } from '../services/container';
import { useEffect, type ComponentProps, useCallback } from 'react';
import type { TranslationKeys } from '../localization';

type Separator = { type: 'separator'; label: TranslationKeys };
type Btn = { type: 'btn'; label: TranslationKeys; icon: IconName; onPress: () => void };
type Link<T extends string> = { type: 'link'; label: TranslationKeys; icon: IconName; page: T };
export type Item<T extends string> = Separator | Btn | Link<T>;
interface DrawerContentProps<T extends string> extends ComponentProps<typeof DrawerItemList> {
    items: Item<T>[];
}

interface DrawerItemWrapperProps<T extends string> extends ComponentProps<typeof DrawerItemList> {
    item: Item<T>;
}

const DrawerItemWrapper = observer(<T extends string>({ item, state, navigation }: DrawerItemWrapperProps<T>) => {
    const { t } = useTranslation();
    const localStore = useLocalObservable(
        () => ({
            item,
            key: state.key,
            index: state.index,
            routes: state.routes,
            get indexOfRoute() {
                const { item: _item, routes } = this;
                if (_item.type !== 'link') {
                    return -1;
                }
                return routes.findIndex(({ name }) => name === _item.page);
            },
            get isFocused() {
                return this.index === this.indexOfRoute;
            },
            navigate() {
                if (item.type === 'btn') {
                    return item.onPress();
                }
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
            localStore.item = item;
            localStore.key = state.key;
            localStore.index = state.index;
            localStore.routes = state.routes;
        });
    }, [state.key, state.index, state.routes, item, localStore]);

    if (item.type === 'separator') {
        return (
            <View style={styles.category}>
                <Text style={styles.categoryText}>{t(item.label)}</Text>
            </View>
        );
    }

    return (
        <DrawerItem
            label={t(item.label)}
            labelStyle={[styles.label, localStore.isFocused && styles.focusedLabel]}
            icon={({ size, color }) => <Icon name={item.icon} size={size} color={color} />}
            onPress={localStore.navigate}
            focused={localStore.isFocused}
            activeTintColor={colors.navPickedTextColor}
        />
    );
});
DrawerItemWrapper.displayName = 'DrawerItemWrapper';

export const DrawerContent = observer(<T extends string>({ items, ...props }: DrawerContentProps<T>) => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);
    const pressBack = useCallback(() => {
        props.navigation.closeDrawer();
        navigationStore.goHome();
    }, [navigationStore, props.navigation]);

    return (
        <DrawerContentScrollView {...props}>
            <DrawerItem label={t('common:toolbarBack')} labelStyle={styles.label} icon={({ size }) => <Icon name="backArrow" size={size} />} onPress={pressBack} />
            {items.map(item => (
                <DrawerItemWrapper key={item.label} {...props} item={item} />
            ))}
        </DrawerContentScrollView>
    );
});
DrawerContent.displayName = 'DrawerContent';

const styles = StyleSheet.create({
    label: {
        fontFamily: 'opensanshebrew',
    },
    focusedLabel: {
        fontWeight: 'bold',
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
