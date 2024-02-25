import Color from 'color';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { View, StyleSheet, StyleProp, ViewStyle, TextInput } from 'react-native';
import { colors } from '../consts';
import { Text } from '../Components/Text';
import { Icon } from '../Components/Icon';
import { useDependencyInjector } from '../services/container';
import { NavBarIcon, ToolbarStore } from '../services/ToolbarStore';
import { PressableRippleWrapper } from '../Components/PressableRippleWrapper';

interface ToolbarNavIconProps extends NavBarIcon {
    style?: StyleProp<ViewStyle>;
}

const rippleColor = Color(colors.white).alpha(0.5).string();
const ToolbarNavIcon = observer<ToolbarNavIconProps>(({ icon, onPress, show, style }) => {
    if (typeof show === 'boolean' && !show) {
        return null;
    }
    return (
        <PressableRippleWrapper android_ripple={{ color: rippleColor, borderless: true }} wrapperStyle={styles.iconRippleWrapper} style={style} onPress={onPress}>
            <Icon size={26} color={colors.white} name={icon} />
        </PressableRippleWrapper>
    );
});
ToolbarNavIcon.displayName = 'Toolbar.NavIcon';

export const Toolbar = observer(() => {
    const { t } = useTranslation();
    const toolbarStore = useDependencyInjector(ToolbarStore);

    return (
        <View style={styles.toolbar}>
            {toolbarStore.navBtn && <ToolbarNavIcon {...toolbarStore.navBtn} />}
            {toolbarStore.showSearch ? (
                <TextInput
                    autoFocus
                    style={styles.searchInput}
                    cursorColor={colors.white}
                    placeholderTextColor={colors.white}
                    onChangeText={toolbarStore.setSearchQuery}
                    placeholder={t('tools:dictionary:searchHint')}
                />
            ) : (
                <Text style={styles.titleText}>{toolbarStore.title}</Text>
            )}
            <View style={styles.toolbarBtnWrapper}>
                {toolbarStore.navIcons.map(navIcon => (
                    <ToolbarNavIcon style={styles.navItem} {...navIcon} />
                ))}
            </View>
        </View>
    );
});
Toolbar.displayName = 'Toolbar';

const styles = StyleSheet.create({
    toolbar: {
        height: 56,
        padding: 10,
        color: colors.white,
        flexDirection: 'row',
        alignContent: 'center',
        backgroundColor: colors.colorPrimary,
        gap: 5,
    },
    iconRippleWrapper: {
        width: 40,
        height: 40,
        flexShrink: 0,
        borderRadius: 20,
        alignItems: 'center',
        justifyContent: 'center',
    },
    rotated: {
        transform: [{ rotate: '180deg' }],
    },
    searchInput: {
        flex: 1,
        color: colors.white,
    },
    titleText: {
        flex: 1,
        fontSize: 20,
        fontWeight: 'bold',
        color: colors.white,
        textAlignVertical: 'center',
    },
    toolbarBtnWrapper: {
        flexDirection: 'row',
        gap: 10,
        flexShrink: 0,
        alignContent: 'center',
        justifyContent: 'center',
    },
    navItem: {},
    toolbarIcon: {
        height: 30,
        width: 30,
    },
    roundBg: {
        position: 'relative',
        top: 0,
        bottom: 0,
        right: '-15%',
        left: '-15%',
        // zIndex: 3,
        width: '130%',
        height: 130,
        borderTopStartRadius: 0,
        borderTopEndRadius: 0,
        borderBottomRightRadius: 130,
        borderBottomLeftRadius: 130,
    },
});
