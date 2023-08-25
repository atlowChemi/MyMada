import React from 'react';
import BuildConfig from 'react-native-build-config';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { HomeBtn } from '../HomeBtn';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { useDependencyInjector } from '../../services/container';
import { NavigationStore } from '../../services/NavigationStore';
import type { NavigatableScreenProps } from '../../App';

export const AboutTheApp = React.memo<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    const navigationStore = useDependencyInjector(NavigationStore);

    return (
        <BasePageView>
            <Text style={styles.text}>{t('aboutUs:message', { version: BuildConfig.VERSION_NAME || 'TEST VERSION' })}</Text>
            <HomeBtn style={styles.privacyButton} img={require('../../assets/homeBtns/privacy.png')} onPress={navigationStore.goToPrivacyPolicy} />
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
