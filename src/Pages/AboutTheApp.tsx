import React from 'react';
import BuildConfig from 'react-native-build-config';
import { SafeAreaView, ScrollView, StatusBar, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { colors } from '../consts';
import { Text } from '../Components/Text';
import { Toolbar } from '../Components/Toolbar';
import { HomeBtn } from '../Components/HomeBtn';
import type { NavigatableScreenProps } from '../App';

export const AboutTheApp = React.memo<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigateToPrivacy = React.useCallback(() => navigation.navigate('Privacy'), [navigation]);

    return (
        <SafeAreaView style={styles.aboutPage}>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <Toolbar navigation={navigation} title={t('aboutUs:toolbarTitle')} />
            <ScrollView style={styles.scroller} contentContainerStyle={styles.scrollContent} showsHorizontalScrollIndicator={false}>
                <Text style={styles.text}>{t('aboutUs:message', { version: BuildConfig.VERSION_NAME || 'TEST VERSION' })}</Text>
                <HomeBtn style={styles.privacyButton} img={require('../assets/homeBtns/privacy.png')} onPress={navigateToPrivacy} />
            </ScrollView>
        </SafeAreaView>
    );
});

const styles = StyleSheet.create({
    aboutPage: {
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
        minHeight: '100%',
        rowGap: 20,
    },
    text: {
        fontSize: 18,
    },
    privacyButton: {
        height: 85,
        borderRadius: 23,
        alignSelf: 'center',
        maxWidth: 390,
    },
});
