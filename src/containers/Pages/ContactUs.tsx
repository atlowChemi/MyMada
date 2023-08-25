import React from 'react';
import { Pressable, StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import database from '@react-native-firebase/database';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { colors } from '../../consts';
import type { NavigatableScreenProps } from '../../App';

export const ContactUs = React.memo<NavigatableScreenProps>(() => {
    const { t } = useTranslation();
    // const a = database().ref('chats').ref();

    return (
        <BasePageView scrollContentStyle={styles.scrollContent}>
            <Pressable onPress={() => null} style={styles.contactChatCard}>
                <Text style={styles.text}>{t('contact:changeName')}</Text>
            </Pressable>
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    scrollContent: {
        rowGap: 10,
    },
    text: {
        fontSize: 20,
    },
    contactChatCard: {
        padding: 10,
        backgroundColor: colors.white,
        borderRadius: 6,
        elevation: 5,
    },
});
