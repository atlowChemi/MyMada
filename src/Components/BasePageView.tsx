import React from 'react';
import { SafeAreaView, ScrollView, StyleSheet } from 'react-native';
import { colors } from '../consts';
import RadialGradient from 'react-native-radial-gradient';
import type { StyleProp, ViewStyle } from 'react-native';

interface BasePageViewProps {
    children: React.ReactNode;
    preScroll?: React.ReactNode;
    postScroll?: React.ReactNode;
    noScroll?: boolean;
    pageStyle?: StyleProp<ViewStyle>;
    scrollViewStyle?: StyleProp<ViewStyle>;
    scrollContentStyle?: StyleProp<ViewStyle>;
}

export const BasePageView = React.memo<BasePageViewProps>(({ noScroll, preScroll, postScroll, children, ...styleProps }) => {
    return (
        <SafeAreaView style={[styles.basePage, styleProps.pageStyle]}>
            <RadialGradient colors={[colors.white, colors.bgShadowEnd]} stops={[0.9]} style={styles.radialBg}>
                {!noScroll && preScroll}
                {noScroll ? (
                    children
                ) : (
                    <ScrollView
                        style={[styles.scroller, styleProps.scrollViewStyle]}
                        contentContainerStyle={[styles.scrollContent, styleProps.scrollContentStyle]}
                        showsHorizontalScrollIndicator={false}>
                        {children}
                    </ScrollView>
                )}
                {!noScroll && postScroll}
            </RadialGradient>
            {/* TODO: show adds when relevant */}
        </SafeAreaView>
    );
});
BasePageView.displayName = 'BasePageView';

const styles = StyleSheet.create({
    basePage: {
        height: '100%',
        flexGrow: 1,
    },
    radialBg: {
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
