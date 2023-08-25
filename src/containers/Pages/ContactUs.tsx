import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import database from '@react-native-firebase/database';
import { ActivityIndicator, Alert, Pressable, StyleSheet, View } from 'react-native';
import { Text } from '../../Components/Text';
import { BasePageView } from '../../Components/BasePageView';
import { colors } from '../../consts';
import { useDependencyInjector } from '../../services/container';
import { MessagingStore } from '../../services/MessagingStore';
import { NavigationStore } from '../../services/NavigationStore';
import type { NavigatableScreenProps } from '../../App';

export const ContactUs = observer<NavigatableScreenProps>(() => {
    const navigationStore = useDependencyInjector(NavigationStore);
    const messagingStore = useDependencyInjector(MessagingStore);
    const { t } = useTranslation();

    if (messagingStore.token.state !== 'fulfilled') {
        if (messagingStore.token.state === 'rejected') {
            const okButton = { text: t('common:ok'), onPress: () => navigationStore.goBack() };
            Alert.alert(t('Shoooooooo'), undefined, [okButton], { cancelable: false });
        }
        return (
            <BasePageView noScroll>
                <View style={styles.loadingWrapper}>
                    <ActivityIndicator size={60} color={colors.colorPrimary} />
                </View>
            </BasePageView>
        );
    }

    const root = database().ref('chats').child(messagingStore.token.value);
    root.on('child_added', data => {
        debugger;
    });

    return (
        <BasePageView scrollContentStyle={styles.scrollContent}>
            <Pressable onPress={() => null} style={styles.contactChatCard}>
                <Text style={styles.text}>{t('contact:changeName')}</Text>
            </Pressable>
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    loadingWrapper: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
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
