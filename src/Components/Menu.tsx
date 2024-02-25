import { injectable } from 'inversify';
import { isValidElement } from 'react';
import { makeAutoObservable, reaction, runInAction } from 'mobx';
import { observer } from 'mobx-react-lite';
import {
    Animated,
    Dimensions,
    Easing,
    EmitterSubscription,
    I18nManager,
    Keyboard,
    KeyboardEvent as RNKeyboardEvent,
    LayoutRectangle,
    NativeEventSubscription,
    Platform,
    ScrollView,
    ScrollViewProps,
    StyleProp,
    StyleSheet,
    View,
    ViewStyle,
    Pressable,
    BackHandler,
} from 'react-native';

import { createDeferredPromise } from '../utils';
import { useLocalStoreWithInjectedDependencies } from '../services/container';
import { Portal } from '@gorhom/portal';

export interface MenuProps {
    visible: boolean;
    anchor: React.ReactNode | { x: number; y: number };
    anchorPosition?: 'top' | 'bottom';
    onDismiss?(): void;
    children: React.ReactNode;
    contentStyle?: StyleProp<ViewStyle>;
    style?: StyleProp<ViewStyle>;
    elevation?: number;
    keyboardShouldPersistTaps?: ScrollViewProps['keyboardShouldPersistTaps'];
}
type Layout = Omit<LayoutRectangle, 'x' | 'y'>;

const SCREEN_INDENT = 8;
const DEFAULT_ELEVATION = 4;
const STATUS_BAR_HEIGHT = 0;
const ANIMATION_DURATION = 250;
const TEST_ID = 'menu';
const EASING = Easing.bezier(0.4, 0, 0.2, 1);
const WINDOW_LAYOUT = Dimensions.get('window');
const OVERLAY_ACCESSIBILITY_LABEL = 'Close menu';
const elevationColors = [
    'transparent',
    'rgb(244, 243, 249)', // palette.primary40, alpha 0.05
    'rgb(237, 237, 246)', // palette.primary40, alpha 0.08
    'rgb(233, 232, 244)', // palette.primary40, alpha 0.11
    'rgb(230, 231, 243)', // palette.primary40, alpha 0.12
    'rgb(227, 227, 241)', // palette.primary40, alpha 0.14
];

@injectable()
class MenuLocalStore {
    private menu?: View;
    private anchor?: View;
    private onDismiss?: MenuProps['onDismiss'];
    private readonly disposers: (() => void)[] = [];
    private keyboardDidShowListener: EmitterSubscription | undefined;
    private keyboardDidHideListener: EmitterSubscription | undefined;
    private dimensionsSubscription: NativeEventSubscription | undefined;
    private backHandlerSubscription: NativeEventSubscription | undefined;

    private keyboardHeight = 0;
    public top = 0;
    public left = 0;
    public rendered = false;
    public menuLayout: Layout = { width: 0, height: 0 };
    public anchorLayout: Layout = { width: 0, height: 0 };
    public windowLayout: Layout = { width: WINDOW_LAYOUT.width, height: WINDOW_LAYOUT.height };

    public readonly opacityAnimation = new Animated.Value(0);
    public readonly scaleAnimation = new Animated.ValueXY({ x: 0, y: 0 });

    constructor() {
        makeAutoObservable(this, undefined, { autoBind: true });

        this.keyboardDidShowListener = Keyboard.addListener('keyboardDidShow', this.keyboardDidShow);
        this.keyboardDidHideListener = Keyboard.addListener('keyboardDidHide', this.keyboardDidHide);
        this.disposers.push(reaction(() => this.rendered, this.updateVisibility));
    }

    public dispose() {
        this.removeListeners();
        this.keyboardDidShowListener?.remove();
        this.keyboardDidHideListener?.remove();
        this.disposers.forEach(dispose => dispose());
    }

    public setMenuRef(ref: View) {
        this.menu = ref;
    }
    public setAnchor(anchor: View) {
        this.anchor = anchor;
    }

    public init(nextProps: MenuProps) {
        const { visible, onDismiss } = nextProps;
        this.rendered = visible;
        this.onDismiss = onDismiss;
    }

    public isCoordinate(anchor: any): anchor is { x: number; y: number } {
        return !isValidElement(anchor) && typeof anchor?.x === 'number' && typeof anchor?.y === 'number';
    }

    private handleDismiss = () => {
        if (this.rendered) {
            this.onDismiss?.();
        }
        return true;
    };

    private measureMenuLayout() {
        const { promise, resolve } = createDeferredPromise<LayoutRectangle>();
        if (!this.menu) {
            resolve({ x: 0, y: 0, width: 0, height: 0 });
            return promise;
        }
        const timeout = setTimeout(() => resolve({ x: 0, y: 0, width: 0, height: 0 }), 100);
        this.menu.measureInWindow((x, y, width, height) => {
            clearTimeout(timeout);
            resolve({ x, y, width, height });
        });
        return promise;
    }

    private measureAnchorLayout() {
        const { promise, resolve } = createDeferredPromise<LayoutRectangle>();
        const { anchor } = this;
        if (this.isCoordinate(anchor)) {
            resolve({ x: anchor.x, y: anchor.y, width: 0, height: 0 });
            return promise;
        }
        if (!anchor) {
            resolve({ x: 0, y: 0, width: 0, height: 0 });
            return promise;
        }
        const timeout = setTimeout(() => resolve({ x: 0, y: 0, width: 0, height: 0 }), 100);
        anchor.measureInWindow((x, y, width, height) => {
            clearTimeout(timeout);
            resolve({ x, y, width, height });
        });
        return promise;
    }

    private updateVisibility() {
        if (this.rendered) {
            return this.show();
        }
        return this.hide();
    }

    private attachListeners() {
        this.backHandlerSubscription = BackHandler.addEventListener('hardwareBackPress', this.handleDismiss);
        this.dimensionsSubscription = Dimensions.addEventListener('change', this.handleDismiss);
    }

    private removeListeners() {
        this.backHandlerSubscription?.remove();
        this.dimensionsSubscription?.remove();
    }

    private keyboardDidShow(e: RNKeyboardEvent) {
        const keyboardHeight = e.endCoordinates.height;
        this.keyboardHeight = keyboardHeight;
    }

    private keyboardDidHide() {
        this.keyboardHeight = 0;
    }

    private async show() {
        const windowLayout = Dimensions.get('window');
        const [menuLayout, anchorLayout] = await Promise.all([this.measureMenuLayout(), this.measureAnchorLayout()]);

        // When visible is true for first render native views can be still not rendered and measureMenuLayout/measureAnchorLayout functions
        // return wrong values e.g { x:0, y: 0, width: 0, height: 0 } so we have to wait until views are ready and rerun this function to show menu
        if (
            !windowLayout.width ||
            !windowLayout.height ||
            !menuLayout.width ||
            !menuLayout.height ||
            (!anchorLayout.width && !this.isCoordinate(this.anchor)) ||
            (!anchorLayout.height && !this.isCoordinate(this.anchor))
        ) {
            requestAnimationFrame(this.show);
            return;
        }

        runInAction(() => {
            this.left = anchorLayout.x;
            this.top = anchorLayout.y;
            this.anchorLayout = {
                height: anchorLayout.height,
                width: anchorLayout.width,
            };
            this.menuLayout = {
                width: menuLayout.width,
                height: menuLayout.height,
            };
            this.windowLayout = {
                height: windowLayout.height - this.keyboardHeight,
                width: windowLayout.width,
            };
        });

        await Promise.resolve(); // await to ensure render happened
        this.attachListeners();

        const commonConfig = { duration: ANIMATION_DURATION, easing: EASING, useNativeDriver: true };
        Animated.parallel([
            Animated.timing(this.scaleAnimation, { ...commonConfig, toValue: { x: menuLayout.width, y: menuLayout.height } }),
            Animated.timing(this.opacityAnimation, { ...commonConfig, toValue: 1 }),
        ]).start();
    }

    private hide() {
        this.removeListeners();

        Animated.timing(this.opacityAnimation, { toValue: 0, duration: ANIMATION_DURATION, easing: EASING, useNativeDriver: true }).start(({ finished }) => {
            if (!finished) {
                return;
            }
            runInAction(() => {
                this.menuLayout = { width: 0, height: 0 };
                this.rendered = false;
            });
            this.scaleAnimation.setValue({ x: 0, y: 0 });
        });
    }
}

export const Menu = observer<MenuProps>(props => {
    const localStore = useLocalStoreWithInjectedDependencies(MenuLocalStore, props);

    const { visible, anchor, anchorPosition, contentStyle, style, elevation = DEFAULT_ELEVATION, children, onDismiss, keyboardShouldPersistTaps } = props;
    const { rendered, menuLayout, anchorLayout, opacityAnimation, scaleAnimation, windowLayout } = localStore;
    let { left, top } = localStore;

    if (!localStore.isCoordinate(anchor) && anchorPosition === 'bottom') {
        top += anchorLayout.height;
    }
    const additionalVerticalValue = Platform.select({ android: STATUS_BAR_HEIGHT, default: 0 });
    const scaleTransforms = [
        { scaleX: scaleAnimation.x.interpolate({ inputRange: [0, menuLayout.width], outputRange: [0, 1] }) },
        { scaleY: scaleAnimation.y.interpolate({ inputRange: [0, menuLayout.height], outputRange: [0, 1] }) },
    ];

    // We need to translate menu while animating scale to imitate transform origin for scale animation
    const positionTransforms = [];

    // Check if menu fits horizontally and if not align it to right.
    if (left <= windowLayout.width - menuLayout.width - SCREEN_INDENT) {
        positionTransforms.push({ translateX: scaleAnimation.x.interpolate({ inputRange: [0, menuLayout.width], outputRange: [-(menuLayout.width / 2), 0] }) });

        // Check if menu position has enough space from left side
        if (left < SCREEN_INDENT) {
            left = SCREEN_INDENT;
        }
    } else {
        positionTransforms.push({ translateX: scaleAnimation.x.interpolate({ inputRange: [0, menuLayout.width], outputRange: [menuLayout.width / 2, 0] }) });
        left += anchorLayout.width - menuLayout.width;
        const right = left + menuLayout.width;
        // Check if menu position has enough space from right side
        if (right > windowLayout.width - SCREEN_INDENT) {
            left = windowLayout.width - SCREEN_INDENT - menuLayout.width;
        }
    }

    // If the menu is larger than available vertical space, calculate the height of scrollable view
    let scrollableMenuHeight = 0;

    if (
        // Check if the menu overflows from bottom side
        top >= windowLayout.height - menuLayout.height - SCREEN_INDENT - additionalVerticalValue &&
        // And bottom side of the screen has more space than top side
        top <= windowLayout.height - top
    ) {
        // Scrollable menu should be below the anchor (expands downwards)
        scrollableMenuHeight = windowLayout.height - top - SCREEN_INDENT - additionalVerticalValue;
    } else if (
        // Check if the menu overflows from bottom side
        top >= windowLayout.height - menuLayout.height - SCREEN_INDENT - additionalVerticalValue &&
        // And top side of the screen has more space than bottom side
        top >= windowLayout.height - top &&
        // And menu overflows from top side
        top <= menuLayout.height - anchorLayout.height + SCREEN_INDENT - additionalVerticalValue
    ) {
        // Scrollable menu should be above the anchor (expands upwards)
        scrollableMenuHeight = top + anchorLayout.height - SCREEN_INDENT + additionalVerticalValue;
    }

    // Scrollable menu max height
    scrollableMenuHeight = scrollableMenuHeight > windowLayout.height - 2 * SCREEN_INDENT ? windowLayout.height - 2 * SCREEN_INDENT : scrollableMenuHeight;

    // Menu is typically positioned below the element that generates it So first check if it fits below the anchor (expands downwards)
    if (
        // Check if menu fits vertically
        top <= windowLayout.height - menuLayout.height - SCREEN_INDENT - additionalVerticalValue ||
        // Or if the menu overflows from bottom side And bottom side of the screen has more space than top side
        (top >= windowLayout.height - menuLayout.height - SCREEN_INDENT - additionalVerticalValue && top <= windowLayout.height - top)
    ) {
        positionTransforms.push({ translateY: scaleAnimation.y.interpolate({ inputRange: [0, menuLayout.height], outputRange: [-((scrollableMenuHeight || menuLayout.height) / 2), 0] }) });
        // Check if menu position has enough space from top side
        if (top < SCREEN_INDENT) {
            top = SCREEN_INDENT;
        }
    } else {
        positionTransforms.push({ translateY: scaleAnimation.y.interpolate({ inputRange: [0, menuLayout.height], outputRange: [(scrollableMenuHeight || menuLayout.height) / 2, 0] }) });
        top += anchorLayout.height - (scrollableMenuHeight || menuLayout.height);
        const bottom = top + (scrollableMenuHeight || menuLayout.height) + additionalVerticalValue;
        // Check if menu position has enough space from bottom side
        if (bottom > windowLayout.height - SCREEN_INDENT) {
            top = scrollableMenuHeight === windowLayout.height - 2 * SCREEN_INDENT ? -SCREEN_INDENT * 2 : windowLayout.height - menuLayout.height - SCREEN_INDENT - additionalVerticalValue;
        }
    }

    const shadowMenuContainerStyle = {
        opacity: opacityAnimation,
        transform: scaleTransforms,
        borderRadius: 4,
        ...(scrollableMenuHeight && { height: scrollableMenuHeight }),
    };

    const positionStyle = {
        top: localStore.isCoordinate(anchor) ? top : top + additionalVerticalValue,
        ...(I18nManager.getConstants().isRTL ? { right: left } : { left }),
    };

    const pointerEvents = visible ? 'box-none' : 'none';

    return (
        <View ref={localStore.setAnchor} collapsable={false}>
            {localStore.isCoordinate(anchor) ? null : anchor}
            {rendered && (
                <Portal>
                    <Pressable accessibilityLabel={OVERLAY_ACCESSIBILITY_LABEL} accessibilityRole="button" onPress={onDismiss} style={styles.pressableOverlay} />
                    <View
                        ref={localStore.setMenuRef}
                        collapsable={false}
                        accessibilityViewIsModal={visible}
                        style={[styles.wrapper, positionStyle, style]}
                        pointerEvents={pointerEvents}
                        onAccessibilityEscape={onDismiss}
                        testID={`${TEST_ID}-view`}>
                        <Animated.View pointerEvents={pointerEvents} style={{ transform: positionTransforms }}>
                            <Animated.View
                                pointerEvents={pointerEvents}
                                style={[
                                    styles.shadowMenuContainer,
                                    shadowMenuContainerStyle,
                                    { backgroundColor: elevationColors[elevation], elevation: [0, 3, 6, 9, 12, 15][elevation] },
                                    contentStyle,
                                ]}
                                testID={`${TEST_ID}-surface`}>
                                {(scrollableMenuHeight && <ScrollView keyboardShouldPersistTaps={keyboardShouldPersistTaps}>{children}</ScrollView>) || <>{children}</>}
                            </Animated.View>
                        </Animated.View>
                    </View>
                </Portal>
            )}
        </View>
    );
});
Menu.displayName = 'Menu';

const styles = StyleSheet.create({
    wrapper: {
        position: 'absolute',
    },
    shadowMenuContainer: {
        opacity: 0,
        borderRadius: 8,
        overflow: 'hidden',
    },
    pressableOverlay: {
        ...StyleSheet.absoluteFillObject,
        width: '100%',
    },
});
