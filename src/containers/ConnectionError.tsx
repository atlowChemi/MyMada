import React from 'react';
import { useTranslation } from 'react-i18next';
import RadialGradient from 'react-native-radial-gradient';
import { Animated, Image, StyleSheet, View } from 'react-native';
import { Text } from '../Components/Text';
import { colors } from '../consts';

export const ConnectionError = React.memo(() => {
    const { t } = useTranslation();
    const left = React.useRef(new Animated.Value(0)).current;

    React.useEffect(() => {
        const move = Animated.timing(left, { toValue: 1, duration: 3000, useNativeDriver: false });
        Animated.loop(move).start();
    }, [left]);

    return (
        <RadialGradient colors={[colors.white, colors.bgShadowEnd]} stops={[0.9]} style={styles.connectionErrorPage}>
            <Text style={styles.title}>{t('errors:connection:title')}</Text>
            <View style={styles.slider}>
                <Image style={styles.ecg} source={require('../assets/images/ecg.png')} />
                <Animated.View style={[styles.sliderBar, { left: left.interpolate({ inputRange: [0, 1], outputRange: ['100%', '0%'] }) }]} />
            </View>
            <Text style={styles.message}>{t('errors:connection:message')}</Text>
        </RadialGradient>
    );
});
ConnectionError.displayName = 'ConnectionError';

const styles = StyleSheet.create({
    connectionErrorPage: {
        flex: 1,
        alignContent: 'center',
        justifyContent: 'center',
        alignItems: 'center',
        gap: 10,
        position: 'absolute',
        top: 0,
        bottom: 0,
        right: 0,
        left: 0,
        backgroundColor: colors.bgShadowEnd,
    },
    title: {
        color: colors.colorPrimary,
        fontSize: 28,
        fontWeight: 'bold',
    },
    slider: {
        width: '100%',
        height: 145,
        backgroundColor: colors.errorColor,
        alignItems: 'center',
    },
    sliderBar: {
        position: 'absolute',
        height: 145,
        top: 0,
        backgroundColor: colors.errorColor,
        width: 20,
    },
    ecg: {
        width: '95%',
        height: 145,
        resizeMode: 'cover',
    },
    message: {
        fontSize: 18,
        fontWeight: 'bold',
        textAlign: 'center',
    },
});
