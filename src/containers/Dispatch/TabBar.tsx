import { StyleSheet, Text, View } from 'react-native';
import { TabBar, TabBarIndicator } from 'react-native-tab-view';
import { ParamListBase, Route, TabNavigationState } from '@react-navigation/native';
import { colors } from '../../consts';
import type { MaterialTopTabBarProps } from '@react-navigation/material-top-tabs';

export function DispatchTabBar({ state, navigation, descriptors, ...rest }: MaterialTopTabBarProps) {
    const focusedOptions = descriptors[state.routes[state.index].key].options;

    const activeColor = focusedOptions.tabBarActiveTintColor ?? colors.white;
    const inactiveColor = focusedOptions.tabBarInactiveTintColor ?? colors.whiteOpacity; //Color(activeColor).alpha(0.5).rgb().string();

    return (
        <TabBar
            {...rest}
            navigationState={state}
            scrollEnabled={focusedOptions.tabBarScrollEnabled}
            bounces={focusedOptions.tabBarBounces}
            activeColor={activeColor}
            inactiveColor={inactiveColor}
            pressColor={focusedOptions.tabBarPressColor}
            pressOpacity={focusedOptions.tabBarPressOpacity}
            tabStyle={focusedOptions.tabBarItemStyle}
            indicatorStyle={[{ backgroundColor: colors.white }, focusedOptions.tabBarIndicatorStyle]}
            gap={focusedOptions.tabBarGap}
            android_ripple={focusedOptions.tabBarAndroidRipple}
            indicatorContainerStyle={focusedOptions.tabBarIndicatorContainerStyle}
            contentContainerStyle={focusedOptions.tabBarContentContainerStyle}
            style={[{ backgroundColor: colors.colorPrimary }, focusedOptions.tabBarStyle]}
            getAccessibilityLabel={({ route }) => descriptors[route.key].options.tabBarAccessibilityLabel}
            getTestID={({ route }) => descriptors[route.key].options.tabBarTestID}
            onTabPress={({ route, preventDefault }) => {
                const event = navigation.emit({
                    type: 'tabPress',
                    target: route.key,
                    canPreventDefault: true,
                });

                if (event.defaultPrevented) {
                    preventDefault();
                }
            }}
            onTabLongPress={({ route }) =>
                navigation.emit({
                    type: 'tabLongPress',
                    target: route.key,
                })
            }
            renderIcon={({ route, focused, color }) => {
                const { options } = descriptors[route.key];

                if (options.tabBarShowIcon === false) {
                    return null;
                }

                if (options.tabBarIcon !== undefined) {
                    const icon = options.tabBarIcon({ focused, color });

                    return <View style={[styles.icon, options.tabBarIconStyle]}>{icon}</View>;
                }

                return null;
            }}
            renderLabel={({ route, focused, color }) => {
                const { options } = descriptors[route.key];

                if (options.tabBarShowLabel === false) {
                    return null;
                }

                const label = options.tabBarLabel !== undefined ? options.tabBarLabel : options.title !== undefined ? options.title : (route as Route<string>).name;

                if (typeof label === 'string') {
                    return (
                        <Text style={[styles.label, { color }, options.tabBarLabelStyle]} allowFontScaling={options.tabBarAllowFontScaling}>
                            {label}
                        </Text>
                    );
                }

                const children = typeof options.tabBarLabel === 'string' ? options.tabBarLabel : options.title !== undefined ? options.title : route.name;

                return label({ focused, color, children });
            }}
            renderBadge={({ route }) => {
                const { tabBarBadge } = descriptors[route.key].options;

                return tabBarBadge?.() ?? null;
            }}
            renderIndicator={({ navigationState: state, ...rest }) => {
                return focusedOptions.tabBarIndicator ? (
                    focusedOptions.tabBarIndicator({
                        state: state as TabNavigationState<ParamListBase>,
                        ...rest,
                    })
                ) : (
                    <TabBarIndicator navigationState={state} {...rest} />
                );
            }}
        />
    );
}
DispatchTabBar.displayName = 'DispatchTabBar';

const styles = StyleSheet.create({
    icon: {
        height: 24,
        width: 24,
    },
    label: {
        margin: 4,
        fontSize: 13,
        textAlign: 'center',
        textTransform: 'uppercase',
        fontFamily: 'opensanshebrew',
        backgroundColor: 'transparent',
    },
});
