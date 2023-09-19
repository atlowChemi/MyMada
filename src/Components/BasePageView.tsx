import { memo } from 'react';
import { FlatList, SafeAreaView, ScrollView, StyleSheet, View } from 'react-native';
import RadialGradient from 'react-native-radial-gradient';
import { colors } from '../consts';
import type { ReactNode } from 'react';
import type { ListRenderItem, StyleProp, ViewStyle } from 'react-native';
import { observer } from 'mobx-react-lite';

interface CommonProps<T extends {} = any> {
    data?: T[];
    scrollType?: 'scroll' | 'flat' | 'none';
    pageStyle?: StyleProp<ViewStyle>;
    scrollViewStyle?: StyleProp<ViewStyle>;
    scrollContentStyle?: StyleProp<ViewStyle>;
}

interface NoScrollProps {
    preScroll?: never;
    postScroll?: never;
    scrollViewStyle?: never;
    scrollContentStyle?: never;
    children: ReactNode;
    scrollType: 'none';
    data?: never;
}

interface ScrollProps {
    preScroll?: ReactNode;
    postScroll?: ReactNode;
    scrollViewStyle?: StyleProp<ViewStyle>;
    scrollContentStyle?: StyleProp<ViewStyle>;
    children: ReactNode;
    scrollType?: 'scroll';
    data?: never;
}

interface FlatScrollProps<T extends {}> {
    children?: never;
    preScroll?: ReactNode;
    postScroll?: ReactNode;
    scrollType: 'flat';
    data: T[];
    renderItem: ListRenderItem<T>;
    keyExtractor?: ((item: T, index: number) => string) | undefined;
}

const Separator = memo(() => <View style={styles.separator} />);

type BasePageViewProps<T extends {} = any> = CommonProps<T> & (NoScrollProps | ScrollProps | FlatScrollProps<T>);

const Content = observer(<T extends {} = {}>(props: BasePageViewProps<T>) => {
    if (props.scrollType === 'none') {
        return props.children;
    }
    if (props.scrollType === 'flat') {
        return (
            <FlatList
                data={props.data}
                renderItem={props.renderItem}
                keyExtractor={props.keyExtractor}
                ItemSeparatorComponent={Separator}
                style={[styles.scroller, props.scrollViewStyle]}
                contentContainerStyle={props.scrollContentStyle}
            />
        );
    }
    return (
        <ScrollView style={[styles.scroller, props.scrollViewStyle]} contentContainerStyle={[styles.scrollContent, props.scrollContentStyle]} showsHorizontalScrollIndicator={false}>
            {props.children}
        </ScrollView>
    );
});

const BasePageViewFn = <T extends {} = {}>(props: BasePageViewProps<T>) => {
    const { scrollType = 'scroll', preScroll, postScroll } = props;
    const noScroll = scrollType === 'none';
    return (
        <SafeAreaView style={[styles.basePage, props.pageStyle]}>
            <RadialGradient colors={[colors.white, colors.bgShadowEnd]} stops={[0.9]} style={styles.radialBg}>
                {!noScroll && preScroll}
                <Content {...props} />
                {!noScroll && postScroll}
            </RadialGradient>
            {/* TODO: show adds when relevant */}
        </SafeAreaView>
    );
};
export const BasePageView = memo(BasePageViewFn) as typeof BasePageViewFn & { displayName: string };
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
        maxHeight: '100%',
        flex: 1,
    },
    scrollContent: {
        paddingTop: 10,
        paddingBottom: 30,
        paddingHorizontal: 15,
        flex: 1,
        rowGap: 20,
    },
    separator: {
        backgroundColor: colors.bgShadowEnd,
        height: 1,
    },
});
