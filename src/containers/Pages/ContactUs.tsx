import React from 'react';
import BuildConfig from 'react-native-build-config';
import { Pressable, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';

export const ContactUs = React.memo<NavigatableScreenProps>(({ navigation }) => {
    const { t } = useTranslation();

    return (
        <BasePageView navigation={navigation}>
            <Pressable onPress={() => null}>
                <Text style={styles.text}>{t('contact:changeName', { version: BuildConfig.VERSION_NAME || 'TEST VERSION' })}</Text>
            </Pressable>
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
