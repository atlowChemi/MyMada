import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { View, Image, Pressable, StyleSheet, StyleProp, ViewStyle, TextInput } from 'react-native';
import { Text } from '../Components/Text';
import { colors } from '../consts';
import { useDependencyInjector } from '../services/container';
import { NavBarIcon, ToolbarStore } from '../services/ToolbarStore';

interface ToolbarNavIconProps extends NavBarIcon {
    style?: StyleProp<ViewStyle>;
}

const ToolbarNavIcon = observer<ToolbarNavIconProps>(({ icon, onPress, rotatable, show, style }) => {
    if (typeof show === 'boolean' && !show) {
        return null;
    }
    return (
        <Pressable style={[style, rotatable && styles.rotated]} onPress={onPress}>
            <Image source={icon} style={styles.toolbarIcon} />
        </Pressable>
    );
});
ToolbarNavIcon.displayName = 'ToolbarNavIcon';

export const Toolbar = observer(() => {
    const { t } = useTranslation();
    const toolbarStore = useDependencyInjector(ToolbarStore);

    return (
        <View style={styles.toolbar}>
            {toolbarStore.navBtn && <ToolbarNavIcon style={styles.backBtn} {...toolbarStore.navBtn} />}
            {toolbarStore.showSearch ? (
                <TextInput autoFocus style={styles.searchInput} placeholder={t('tools:dictionary:searchHint')} placeholderTextColor={colors.white} onChangeText={toolbarStore.setSearchQuery} />
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
    backBtn: {
        flexShrink: 0,
        alignContent: 'center',
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
