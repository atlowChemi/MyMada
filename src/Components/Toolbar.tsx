import React from 'react';
import { View, Image, Pressable, StyleSheet, Text } from 'react-native';
import { LinearGradient } from 'react-native-linear-gradient';
import { colors } from '../consts';

export const Toolbar: React.FC<{ title?: string; hideSettings?: boolean; showRoundBg?: boolean }> = ({ title, hideSettings, showRoundBg }) => {
    return (
        <>
            <View style={styles.toolbar}>
                <Text style={styles.titleText}>{title || 'דיווחי מגן דוד אדום'}</Text>
                <View style={styles.toolbarBtnWrapper}>
                    {!hideSettings && (
                        <Pressable style={styles.settingsBtn}>
                            <Image source={require('../assets/icons/ic_settings.webp')} style={styles.settingsImg} />
                        </Pressable>
                    )}
                </View>
            </View>
            {showRoundBg && <LinearGradient colors={['#1f425f', colors.ColorPrimary, colors.ColorPrimary]} angle={0} useAngle style={styles.roundBg} />}
        </>
    );
};

const styles = StyleSheet.create({
    toolbar: {
        height: 56,
        padding: 10,
        color: colors.White,
        flexDirection: 'row',
        alignContent: 'center',
        backgroundColor: colors.ColorPrimary,
    },
    titleText: {
        flex: 1,
        fontSize: 20,
        fontWeight: 'bold',
        color: colors.White,
        textAlignVertical: 'center',
    },
    toolbarBtnWrapper: {
        flexShrink: 0,
        alignContent: 'center',
        justifyContent: 'center',
    },
    settingsBtn: {},
    settingsImg: {
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
