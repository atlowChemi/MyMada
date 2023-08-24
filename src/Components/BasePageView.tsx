import React from 'react';
import { SafeAreaView, ScrollView, StatusBar, StyleSheet } from 'react-native';
import { colors } from '../consts';
import { Toolbar, ToolbarProps } from '../containers/Toolbar';
import type { StyleProp, ViewStyle } from 'react-native';

interface BasePageViewProps extends Pick<ToolbarProps, 'navigation' | 'hideBack' | 'hideSettings' | 'showRoundBg'> {
    children: React.ReactNode;
    preScroll?: React.ReactNode;
    postScroll?: React.ReactNode;
    noScroll?: boolean;
    toolbarTitle?: string;
    pageStyle?: StyleProp<ViewStyle>;
    scrollViewStyle?: StyleProp<ViewStyle>;
    scrollContentStyle?: StyleProp<ViewStyle>;
}

export const BasePageView = React.memo<BasePageViewProps>(({ navigation, hideBack, hideSettings, showRoundBg, toolbarTitle, noScroll, preScroll, postScroll, children, ...styleProps }) => {
    return (
        <SafeAreaView style={[styles.basePage, styleProps.pageStyle]}>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar {...{ navigation, hideBack, hideSettings, showRoundBg }} title={toolbarTitle} />
            {!noScroll && preScroll}
            {noScroll ? (
                children
            ) : (
                <ScrollView style={[styles.scroller, styleProps.scrollViewStyle]} contentContainerStyle={[styles.scrollContent, styleProps.scrollContentStyle]} showsHorizontalScrollIndicator={false}>
                    {children}
                </ScrollView>
            )}
            {!noScroll && postScroll}
            {/* TODO: show adds when relevant */}
        </SafeAreaView>
    );
});

const styles = StyleSheet.create({
    basePage: {
        backgroundColor: colors.bgShadowEnd,
        height: '100%',
        flexGrow: 1,
    },
    scroller: {
        height: '100%',
        flex: 1,
    },
    scrollContent: {
        paddingTop: 10,
        paddingBottom: 30,
        paddingHorizontal: 15,
        flex: 1,
        rowGap: 20,
    },
});
