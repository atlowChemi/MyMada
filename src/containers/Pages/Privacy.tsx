import React from 'react';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import { useTranslation } from 'react-i18next';
import { WebView } from 'react-native-webview';
import { colors } from '../../consts';
import type { NavigatableScreenProps } from '../../App';
import { action } from 'mobx';
import { ConnectionError } from '../ConnectionError';
import { BasePageView } from '../../Components/BasePageView';

const BASE_URL = 'https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o';
const errorCompBuilder = () => <ConnectionError />;
const loaderBuilder = () => (
    <View style={styles.loaderContainer}>
        <ActivityIndicator size="large" color={colors.colorPrimaryDark} />
    </View>
);

export const Privacy = observer<NavigatableScreenProps>(({ navigation }) => {
    const { t, i18n } = useTranslation();
    const state = useLocalObservable(() => ({
        language: i18n.language,
        get uri() {
            if (this.language === 'en') {
                return `${BASE_URL}/privacy-en.html?alt=media&token=20a59b60-69e2-45b5-b274-4b311442aa0d`;
            }
            return `${BASE_URL}/privacy.html?alt=media&token=a758a10d-14d0-431f-a9bc-4b61b6e25ee0`;
        },
        get source() {
            return { uri: this.uri };
        },
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
    React.useEffect(
        action(() => {
            state.language = i18n.language;
        }),
        [state, i18n.language],
    );

    return (
        <BasePageView navigation={navigation} toolbarTitle={t('privacy:toolbarTitle')} noScroll>
            <WebView source={state.source} startInLoadingState renderError={errorCompBuilder} renderLoading={loaderBuilder} />
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    loaderContainer: {
        flex: 1,
        position: 'absolute',
        top: 0,
        bottom: 0,
        right: 0,
        left: 0,
        backgroundColor: colors.bgShadowEnd,
        justifyContent: 'center',
    },
});
