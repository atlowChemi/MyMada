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
            <HomeBtn wrapperStyle={styles.privacyButtonWrapper} style={styles.privacyButton} textStyle={styles.btnTextStyle} size={70} icon="privacy" onPress={navigationStore.goToPrivacyPolicy} />
        </BasePageView>
    );
});
AboutTheApp.displayName = 'AboutTheApp';

const styles = StyleSheet.create({
    text: {
        fontSize: 18,
    },
    btnTextStyle: {
        flex: 1,
    },
    privacyButtonWrapper: {
        width: '100%',
        height: 85,
        maxHeight: 85,
        maxWidth: 390,
        borderRadius: 7,
    },
    privacyButton: {
        flex: 1,
        flexDirection: 'row',
        paddingHorizontal: 10,
        alignSelf: 'center',
    },
});
