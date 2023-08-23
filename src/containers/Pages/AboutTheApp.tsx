import React from 'react';
import BuildConfig from 'react-native-build-config';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { HomeBtn } from '../HomeBtn';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';

export const AboutTheApp = React.memo<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();
    const navigateToPrivacy = React.useCallback(() => navigation.navigate('Privacy'), [navigation]);

    return (
        <BasePageView navigation={navigation} toolbarTitle={t('aboutUs:toolbarTitle')}>
            <Text style={styles.text}>{t('aboutUs:message', { version: BuildConfig.VERSION_NAME || 'TEST VERSION' })}</Text>
            <HomeBtn style={styles.privacyButton} img={require('../../assets/homeBtns/privacy.png')} onPress={navigateToPrivacy} />
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    text: {
        fontSize: 18,
    },
    privacyButton: {
        height: 85,
        maxHeight: 85,
        borderRadius: 23,
        alignSelf: 'center',
        maxWidth: 390,
    },
});
