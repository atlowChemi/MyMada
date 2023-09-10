import { useEffect } from 'react';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import PDFView from 'react-native-view-pdf';
import storage from '@react-native-firebase/storage';
import { colors } from '../../consts';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';
import { ConnectionError } from '../ConnectionError';
import { fromPromise } from 'mobx-utils';

const allowedDocs = ['bls', 'als', 'kuntres'] as const;
type AllowedDoc = (typeof allowedDocs)[number];

export const DocsViewer = observer<NavigatableScreenProps>(({ navigation, route }) => {
    const navigationStore = useDependencyInjector(NavigationStore);
    const localStore = useLocalObservable(() => ({
        type: undefined as AllowedDoc | undefined,
        isLoaded: false,
        hasErrored: false,
        onLoad() {
            this.isLoaded = true;
        },
        onError() {
            this.hasErrored = true;
        },
        setType(type: AllowedDoc | undefined) {
            this.type = type;
        },
        async calculateDocUrl(type: AllowedDoc | undefined) {
            if (!type || !allowedDocs.includes(type)) {
                throw new Error(`Invalid doc type: "${type}"`);
            }
            const docMap: Record<AllowedDoc, string> = {
                als: 'als[1].pdf',
                bls: 'bls[1].pdf',
                kuntres: 'kuntres.pdf',
            };
            const reference = storage().ref(docMap[type]);
            return reference.getDownloadURL();
        },
        get docUrl() {
            return fromPromise(this.calculateDocUrl(this.type));
        },
    }));

    useEffect(() => {
        const type = (route.params as any)?.type;
        localStore.setType(type);
    }, [route.params, localStore]);
    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);

    if (localStore.docUrl.state === 'rejected' || localStore.hasErrored) {
        return <ConnectionError />;
    }

    return (
        <BasePageView noScroll>
            {localStore.docUrl.state === 'fulfilled' && (
                <PDFView style={styles.pdf} fadeInDuration={250.0} onLoad={localStore.onLoad} onError={localStore.onError} resource={localStore.docUrl.value} />
            )}
            {(!localStore.isLoaded || localStore.docUrl.state === 'pending') && (
                <View style={styles.loadingWrapper}>
                    <ActivityIndicator size={60} color={colors.colorPrimary} />
                </View>
            )}
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    loadingWrapper: {
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        zIndex: 3,
        position: 'absolute',
        alignItems: 'center',
        justifyContent: 'center',
    },
    pdf: {
        flex: 1,
    },
});
