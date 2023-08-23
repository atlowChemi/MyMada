import React from 'react';
import { View, Image, Pressable, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { LinearGradient } from 'react-native-linear-gradient';
import { Text } from '../Components/Text';
import { colors } from '../consts';
import { NavigatableScreenProps } from '../App';

export interface ToolbarProps extends NavigatableScreenProps {
    title?: string;
    hideSettings?: boolean;
    hideBack?: boolean;
    showRoundBg?: boolean;
}

export const Toolbar: React.FC<ToolbarProps> = ({ title, hideSettings, hideBack, showRoundBg, navigation }) => {
    const { t, i18n } = useTranslation();

    return (
        <>
            <View style={styles.toolbar}>
                {!hideBack && navigation.canGoBack() && (
                    <Pressable style={[styles.backBtn, i18n.language === 'he' && styles.rotated]} onPress={navigation.goBack}>
                        <Image source={require('../assets/icons/back_arrow.webp')} style={styles.toolbarIcon} />
                    </Pressable>
                )}
                <Text style={styles.titleText}>{title || t('common:appName')}</Text>
                <View style={styles.toolbarBtnWrapper}>
                    {!hideSettings && (
                        <Pressable style={styles.settingsBtn}>
                            <Image source={require('../assets/icons/ic_settings.webp')} style={styles.toolbarIcon} />
                        </Pressable>
                    )}
                </View>
            </View>
            {showRoundBg && <LinearGradient colors={['#1f425f', colors.colorPrimary, colors.colorPrimary]} angle={0} useAngle style={styles.roundBg} />}
        </>
    );
};

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
