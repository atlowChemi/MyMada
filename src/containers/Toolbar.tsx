import { View, Image, Pressable, StyleSheet } from 'react-native';
import { Text } from '../Components/Text';
import { colors } from '../consts';
import { useDependencyInjector } from '../services/container';
import { ToolbarStore } from '../services/ToolbarStore';
import { observer } from 'mobx-react-lite';

export const Toolbar = observer(() => {
    const toolbarStore = useDependencyInjector(ToolbarStore);

    return (
        <View style={styles.toolbar}>
            {toolbarStore.navBtn && (
                <Pressable style={[styles.backBtn, toolbarStore.navBtn.rotatable && styles.rotated]} onPress={toolbarStore.navBtn.onPress}>
                    <Image source={toolbarStore.navBtn.icon} style={styles.toolbarIcon} />
                </Pressable>
            )}
            <Text style={styles.titleText}>{toolbarStore.title}</Text>
            <View style={styles.toolbarBtnWrapper}>
                {toolbarStore.navIcons.map(({ key, icon, onPress }) => (
                    <Pressable style={styles.settingsBtn} key={key} onPress={onPress}>
                        <Image source={icon} style={styles.toolbarIcon} />
                    </Pressable>
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
    settingsBtn: {},
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
