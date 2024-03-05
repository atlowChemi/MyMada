import { injectable } from 'inversify';
import { computedFn } from 'mobx-utils';
import { observer } from 'mobx-react-lite';
import { StyleSheet, TouchableOpacity } from 'react-native';
import { computed, makeAutoObservable, observable } from 'mobx';
import firestore from '@react-native-firebase/firestore';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BaseDialog } from '../dialogs/BaseDialog';
import { Snackbar } from '../../Components/Snackbar';
import { ConnectionError } from '../ConnectionError';
import { DictionaryForm } from '../dialogs/DictionaryForm';
import { DialogStore } from '../../services/DialogStore';
import { ToolbarStore } from '../../services/ToolbarStore';
import { BasePageView } from '../../Components/BasePageView';
import { SnackbarStore } from '../../services/SnackbarStore';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { ListRenderItemInfo } from 'react-native';
import type { FirebaseFirestoreTypes } from '@react-native-firebase/firestore';
import type { NavigatableToolsScreenProps } from '../Pages/Tools';

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
    public selectedItem?: DictionaryEntry = undefined;
    private readonly dictionaryOrder = observable.map<string, string>();
    private readonly dictionary = observable.map<string, DictionaryEntry>();
    private readonly dictionaryCollection = firestore().collection<DictionaryEntry>('dictionary');
    private disposers: (() => void)[] = [];

    constructor(private readonly dialogStore: DialogStore, private readonly snackbarStore: SnackbarStore, private readonly toolbarStore: ToolbarStore) {
        makeAutoObservable<DictionaryLocalStore, 'selectItem'>(this, {
            selectItem: false,
            dictionaryValues: computed.struct,
        });
        this.disposers.push(this.dictionaryCollection.where('approved', '==', true).onSnapshot(this.onSnapshot, this.onError), this.toolbarStore.setDictionaryItemAdder(this.create));
    }

    private get dictionaryKeysOrder() {
        return Array.from(this.dictionaryOrder.entries())
            .sort(([, a], [, b]) => Intl.Collator().compare(a, b))
            .filter(([, name]) => name.toLowerCase().includes(this.toolbarStore.searchQuery))
            .map(([key]) => key);
        // filter by search prior to sorting
    }

    public get dictionaryValues() {
        return Array.from(this.dictionaryKeysOrder, key => this.dictionary.get(key)!);
    }

    public get selectItem() {
        return computedFn((key: string) => async () => {
            if (!this.dictionary.has(key)) {
                return;
            }
            const item = this.dictionary.get(key)!;
            const shouldEdit = await this.dialogStore.openDialog(BaseDialog, {
                title: item.name,
                description: item.definition,
                okText: i18n.t('tools:dictionary:edit:btn'),
                cancelText: i18n.t('common:cancel'),
            });
            if (shouldEdit) {
                this.edit(item);
            }
        });
    }

    public dispose() {
        this.disposers.forEach(disposer => disposer());
    }

    public create = async () => {
        const item = await this.dialogStore.openDialog(DictionaryForm);
        if (!item) {
            return;
        }
        try {
            const doc = this.dictionaryCollection.doc();
            await doc.set({
                key: doc.id,
                approved: false,
                name: item.name,
                definition: item.definition,
            });
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:create:success'), timeout: 5000 });
        } catch (error) {
            error instanceof Error && crashlytics().recordError(error);
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:create:failure'), timeout: 5000 });
        }
    };

    public edit = async (item: DictionaryEntry) => {
        const edited = await this.dialogStore.openDialog(DictionaryForm, { item });
        if (!edited) {
            return;
        }
        try {
            const doc = this.dictionaryCollection.doc(edited.key);
            const { name, definition } = (await doc.get()).data() || {};
            await doc.update({
                ...(name !== edited.name && { newName: edited.name }),
                ...(definition !== edited.definition && { newDefinition: edited.definition }),
            });
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:edit:success'), timeout: 5000 });
        } catch (error) {
            error instanceof Error && crashlytics().recordError(error);
            this.snackbarStore.openSnack(Snackbar, { msg: i18n.t('tools:dictionary:edit:failure'), timeout: 5000 });
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
        error instanceof Error && crashlytics().recordError(error);
    };
}

const Item = observer<ListRenderItemInfo<DictionaryEntry> & { localStore: DictionaryLocalStore }>(({ item, localStore }) => {
    return (
        <TouchableOpacity style={styles.item} activeOpacity={0.7} onPress={localStore.selectItem(item.key)}>
            <Text style={styles.itemTxt}>{item.name}</Text>
        </TouchableOpacity>
    );
});
Item.displayName = 'Tools.DictionaryItem';

export const Dictionary = observer<NavigatableToolsScreenProps<'Dictionary'>>(props => {
    useInnerNavigator('Tools', props);
    const localStore = useLocalStoreWithInjectedDependencies(DictionaryLocalStore);
    if (localStore.hasError) {
        return <ConnectionError />;
    }

    return <BasePageView scrollType="flat" data={localStore.dictionaryValues} renderItem={itemProps => <Item localStore={localStore} {...itemProps} />} keyExtractor={item => item.key} />;
});
Dictionary.displayName = 'Tools.Dictionary';

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
