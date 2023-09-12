import { injectable } from 'inversify';
import { computedFn } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { StyleSheet, TouchableOpacity } from 'react-native';
import { computed, makeAutoObservable, observable, runInAction } from 'mobx';
import Dialog from 'react-native-dialog';
import firestore from '@react-native-firebase/firestore';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { Snackbar } from '../../Components/Snackbar';
import { ConnectionError } from '../ConnectionError';
import { BasePageView } from '../../Components/BasePageView';
import { DictionaryForm } from '../../Components/DictionaryForm';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import { SnackbarStore } from '../../services/SnackbarStore';
import type { ListRenderItemInfo } from 'react-native';
import type { FirebaseFirestoreTypes } from '@react-native-firebase/firestore';

export interface DictionaryEntry {
    key: string;
    name: string;
    newName?: string;
    definition: string;
    newDefinition?: string;
    approved: boolean;
}

@injectable()
class DictionaryLocalStore {
    public hasError = false;
    public isEditing = false;
    private _selectedItem?: string = undefined;
    private readonly dictionaryOrder = observable.map<string, string>();
    private readonly dictionary = observable.map<string, DictionaryEntry>();
    private readonly dictionaryCollection = firestore().collection<DictionaryEntry>('dictionary');
    private disposers: (() => void)[] = [];

    constructor(private readonly snackbarStore: SnackbarStore) {
        makeAutoObservable<DictionaryLocalStore, 'selectItem'>(this, {
            selectItem: false,
            dictionaryValues: computed.struct,
        });
        this.disposers.push(this.dictionaryCollection.where('approved', '==', true).onSnapshot(this.onSnapshot, this.onError));
    }

    private get dictionaryKeysOrder() {
        return Array.from(this.dictionaryOrder.entries())
            .sort(([, a], [, b]) => Intl.Collator().compare(a, b))
            .map(([key]) => key);
        // filter by search prior to sorting
    }

    public get dictionaryValues() {
        return Array.from(this.dictionaryKeysOrder, key => this.dictionary.get(key)!);
    }

    public get selectedItem() {
        if (!this._selectedItem || !this.dictionary.has(this._selectedItem)) {
            return undefined;
        }
        return this.dictionary.get(this._selectedItem);
    }

    public get selectItem() {
        return computedFn(
            (key?: string) => () =>
                runInAction(() => {
                    this._selectedItem = key;
                }),
        );
    }

    public dispose() {
        this.disposers.forEach(disposer => disposer());
    }

    public edit = () => {
        this.isEditing = true;
    };

    public onEditorClose = async (item?: DictionaryEntry) => {
        try {
            if (!item) {
                return true;
            }
            const doc = this.dictionaryCollection.doc(item.key);
            const { name, definition } = (await doc.get()).data() || {};
            await doc.update({
                ...(name !== item.name && { newName: item.name }),
                ...(definition !== item.definition && { newDefinition: item.definition }),
            });
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:edit:success'), timeout: 5000 });
            return true;
        } catch (error) {
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:edit:success'), timeout: 5000 });
            // Crashlytics
            return false;
        } finally {
            runInAction(() => {
                this.isEditing = false;
                this._selectedItem = undefined;
            });
        }
    };

    private onSnapshot = (snapshot: FirebaseFirestoreTypes.QuerySnapshot<DictionaryEntry>) => {
        if (snapshot.empty) {
            return; // Is this OK?
        }
        snapshot.docChanges().forEach(change => {
            const {
                doc: { id },
                type,
            } = change;
            if (type === 'removed') {
                this.dictionary.delete(id);
                this.dictionaryOrder.delete(id);
            }
            const data = change.doc.data();
            if (!data.approved || !data.name) {
                return;
            }
            this.dictionary.set(id, data);
            this.dictionaryOrder.set(id, data.name);
        });
    };

    private onError = (error: Error) => {
        this.hasError = true;
        console.log(error);
    };
}

const DictionaryEntryDialog = observer<{ localStore: DictionaryLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <Dialog.Container visible onBackdropPress={localStore.selectItem()} onRequestClose={localStore.selectItem()}>
            <Dialog.Title style={[styles.font, styles.dialogColorDark]}>{localStore.selectedItem?.name}</Dialog.Title>
            <Dialog.Description style={[styles.font, styles.dialogColorDark]}>{localStore.selectedItem?.definition}</Dialog.Description>
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('common:cancel')} onPress={localStore.selectItem()} />
            <Dialog.Button style={[styles.font, styles.dialogColor]} label={t('tools:dictionary:edit:btn')} onPress={localStore.edit} />
        </Dialog.Container>
    );
});

const Item = observer<ListRenderItemInfo<DictionaryEntry> & { localStore: DictionaryLocalStore }>(({ item, localStore }) => {
    return (
        <TouchableOpacity style={styles.item} activeOpacity={0.7} onPress={localStore.selectItem(item.key)}>
            <Text style={styles.itemTxt}>{item.name}</Text>
        </TouchableOpacity>
    );
});

export const Dictionary = observer(() => {
    const localStore = useLocalStoreWithInjectedDependencies(DictionaryLocalStore);

    if (localStore.hasError) {
        return <ConnectionError />;
    }

    return (
        <>
            {localStore.selectedItem && !localStore.isEditing && <DictionaryEntryDialog localStore={localStore} />}
            {localStore.selectedItem && localStore.isEditing && <DictionaryForm item={localStore.selectedItem} onClose={localStore.onEditorClose} />}
            <BasePageView scrollType="flat" data={localStore.dictionaryValues} renderItem={props => <Item localStore={localStore} {...props} />} keyExtractor={item => item.key} />
        </>
    );
});

const styles = StyleSheet.create({
    item: {
        height: 48,
        paddingHorizontal: 8,
        alignItems: 'flex-start',
        justifyContent: 'center',
        backgroundColor: 'white',
    },
    itemTxt: {
        fontSize: 16,
        fontWeight: 'bold',
    },
    font: {
        fontFamily: 'opensanshebrew',
    },
    dialogColor: {
        color: colors.colorNice,
    },
    dialogColorDark: {
        color: colors.colorPrimaryDark,
    },
});
