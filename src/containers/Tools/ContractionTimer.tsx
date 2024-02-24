import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { useTranslation } from 'react-i18next';
import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { makeAutoObservable, reaction, runInAction } from 'mobx';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { BaseDialog } from '../dialogs/BaseDialog';
import { DialogStore } from '../../services/DialogStore';
import { ToolbarStore } from '../../services/ToolbarStore';
import { LinkedList, NodeOf } from '../../utils/linkedList';
import { Loader, PageLoader } from '../../Components/Loader';
import { BasePageView } from '../../Components/BasePageView';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { CircularProgressButton } from '../../Components/CircularProgressButton';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { FlatList, ListRenderItemInfo } from 'react-native';
import type { NavigatableToolsScreenProps } from '../Pages/Tools';

const TWENTY_MINUTES = 20 * 60_000;

interface Contraction {
    /** The unix timestamp when the contraction started. */
    start: number;
    /** The unix timestamp when the contraction ended. */
    end?: number;
    /** The 1 based index of the contraction. */
    number: number;
}

function printDifference(startDate: number, endDate: number) {
    let difference = endDate - startDate;

    const secondsInMilli = 1000;
    const minutesInMilli = secondsInMilli * 60;

    const elapsedMinutes = Math.floor(difference / minutesInMilli);
    difference = difference % minutesInMilli;

    const elapsedSeconds = Math.floor(difference / secondsInMilli);
    const minutes = elapsedMinutes > 0 && `${elapsedMinutes} ${i18n.t('tools:contraction:minutes')}`;
    return `${minutes || ''}${minutes ? ` ${i18n.t('tools:contraction:and')}` : ''}${elapsedSeconds} ${i18n.t('tools:contraction:seconds')}`;
}

@injectable()
class ContractionTimerLocalStore {
    private active = false;
    private isHandlingReload = false;
    private disposers: (() => void)[] = [];
    private flatList: FlatList<NodeOf<Contraction>> | null = null;
    public readonly contractions = new LinkedList<Contraction>(undefined, { double: true, deepObservable: true });
    public readonly loading = fromPromise(this.loadSavedContractions());

    constructor(private readonly dialogStore: DialogStore, private readonly sharedPreferencesStore: SharedPreferencesStore, private readonly toolbarStore: ToolbarStore) {
        makeAutoObservable(this, { loading: false }, { autoBind: true });
        this.disposers.push(
            reaction(
                () => this.contractions.isEmpty,
                val => runInAction(() => (this.toolbarStore.showTrashIcon = !val)),
            ),
            this.toolbarStore.setTrashAction(this.deleteData),
        );
    }

    public get isActive() {
        return this.active;
    }

    private async deleteData() {
        if (this.contractions.isEmpty) {
            return;
        }
        const shouldDelete = await this.dialogStore.openDialog(BaseDialog, {
            blockBackBtn: true,
            blockBackdropPress: true,
            okText: i18n.t('common:ok'),
            cancelText: i18n.t('common:notNow'),
            negativeBtnColor: colors.oldPrimary,
            description: i18n.t('tools:contraction:delete'),
        });

        if (!shouldDelete) {
            return;
        }
        try {
            runInAction(() => {
                this.contractions.clear();
                this.active = false;
            });
            await this.cleanupSavedData();
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
        }
    }

    private async cleanupSavedData() {
        return await Promise.all([this.sharedPreferencesStore.setItem('contractList', ''), this.sharedPreferencesStore.setItem('contractLong', '0')]);
    }

    private async loadSavedContractions() {
        this.isHandlingReload = true;
        try {
            const sharedPreferences = await this.sharedPreferencesStore.settingsChannel;
            const { contractList, contractLong } = sharedPreferences;
            if (!contractList || !this.contractions.isEmpty) {
                return undefined;
            }
            const oldContractions = Number.parseInt(contractLong || TWENTY_MINUTES.toString(), 10);
            const areOldContractionsLessThan20MinOld = Date.now() - oldContractions <= TWENTY_MINUTES;
            if (!areOldContractionsLessThan20MinOld) {
                // The saved contractions have breached the 20 min threshold - we can delete and ignore them.
                return await this.cleanupSavedData();
            }
            const shouldRestore = await this.dialogStore.openDialog(BaseDialog, {
                blockBackBtn: true,
                blockBackdropPress: true,
                okText: i18n.t('common:ok'),
                cancelText: i18n.t('common:notNow'),
                negativeBtnColor: colors.oldPrimary,
                description: i18n.t('tools:contraction:recoverDialogMsg'),
            });

            if (!shouldRestore) {
                return await this.cleanupSavedData();
            }
            const sanitized = this.sanitizeSavedContractions(contractList);
            if (!sanitized) {
                return undefined;
            }
            // We already ensured the list is empty, but let's be extra safe here.
            this.contractions.clear();
            this.contractions.add(sanitized);
            if (!this.contractions.tail?.value.end) {
                this.active = true;
            }
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
        } finally {
            this.isHandlingReload = false;
        }
    }

    private sanitizeSavedContractions(saved: string) {
        const savedContractions = JSON.parse(saved);
        if (!Array.isArray(savedContractions)) {
            return undefined;
        }
        const sanitizedContractions: Contraction[] = [];
        for (const maybeContraction of savedContractions) {
            if (!maybeContraction || typeof maybeContraction !== 'object') {
                continue;
            }
            let { start, number, end } = maybeContraction;
            if (typeof start === 'string') {
                start = new Date(start).getTime();
            }
            if (typeof end === 'string') {
                end = new Date(end).getTime();
            }
            if ([number, start].some(t => typeof t !== 'number' || Number.isNaN(t))) {
                break;
            }
            sanitizedContractions.push({ start, number, end });
        }
        return sanitizedContractions;
    }

    private startContraction() {
        this.active = true;
        const newContraction: Contraction = {
            start: Date.now(),
            number: this.contractions.length + 1,
        };
        this.contractions.add(newContraction);
    }

    private endContraction() {
        this.active = false;
        if (!this.contractions.tail?.value) {
            return;
        }
        this.contractions.tail.value.end = Date.now();
    }

    public toggleContraction = () => {
        if (this.active) {
            this.endContraction();
        } else {
            this.startContraction();
        }
        try {
            if (this.contractions.isEmpty || !this.flatList) {
                return;
            }
            this.flatList.scrollToIndex({ index: 0, animated: true });
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
        }
    };

    public setFlatList = (flatList: FlatList<NodeOf<Contraction>>) => {
        this.flatList = flatList;
    };

    public dispose() {
        this.disposers.forEach(disposer => disposer());
        if (this.isHandlingReload || this.contractions.isEmpty) {
            return;
        }
        Promise.all([this.sharedPreferencesStore.setItem('contractList', JSON.stringify(this.contractions.array)), this.sharedPreferencesStore.setItem('contractLong', Date.now().toString())]);
    }
}

const ContractionsTimerFooter = observer<{ localStore: ContractionTimerLocalStore }>(({ localStore }) => {
    const { t } = useTranslation();

    return (
        <View style={styles.bottomSection}>
            <CircularProgressButton
                animating={localStore.isActive}
                onPress={localStore.toggleContraction}
                backgroundColor={localStore.isActive ? colors.greyBtn : colors.white}
                title={t(`tools:contraction:${localStore.isActive ? 'end' : 'start'}`)}
            />
        </View>
    );
});
ContractionsTimerFooter.displayName = 'Tools.ContractionsTimerFooter';

const ContractionItem = observer<ListRenderItemInfo<NodeOf<Contraction>>>(({ item }) => {
    const localStore = useLocalObservable(() => ({
        item,
        get contractionLength() {
            const length = printDifference(this.item.value.start, this.item.value.end!);
            return i18n.t('tools:contraction:lengthMsg', { length });
        },
        get contractionDelta() {
            if (!this.item.prev) {
                return '';
            }
            const delta = printDifference(this.item.prev.value.start, this.item.value.start);
            return i18n.t('tools:contraction:delta', { delta });
        },
    }));
    return (
        <View style={styles.contractionItem}>
            <Text style={styles.contractionNumber}>{item.value.number}</Text>
            {item.value.end ? (
                <View style={styles.contractionDetails}>
                    <Text style={styles.contractionDetailsTitle}>{localStore.contractionLength}</Text>
                    <Text style={styles.contractionDetailsText}>{localStore.contractionDelta}</Text>
                </View>
            ) : (
                <View style={[styles.contractionDetails, styles.contractionsLoader]}>
                    <Loader size={10} bounceHeight={10} />
                </View>
            )}
        </View>
    );
});
ContractionItem.displayName = 'Tools.ContractionItem';

export const ContractionTimer = observer<NavigatableToolsScreenProps>(props => {
    useInnerNavigator('Tools', props);
    const localStore = useLocalStoreWithInjectedDependencies(ContractionTimerLocalStore);

    if (localStore.loading.state === 'pending') {
        return <PageLoader />;
    }

    // Handle landscape orientation
    return (
        <BasePageView
            scrollType="flat"
            ref={localStore.setFlatList}
            keyExtractor={item => `${item.value.number}`}
            data={localStore.contractions.reversedNodeArray}
            renderItem={itemProps => <ContractionItem {...itemProps} />}
            postScroll={<ContractionsTimerFooter localStore={localStore} />}
        />
    );
});
ContractionTimer.displayName = 'Tools.ContractionTimer';

const styles = StyleSheet.create({
    contractionItem: {
        margin: 5,
        height: 65,
        elevation: 4,
        borderRadius: 6,
        overflow: 'hidden',
        flexDirection: 'row',
        backgroundColor: colors.white,
    },
    contractionNumber: {
        width: 50,
        height: 65,
        fontSize: 20,
        flexShrink: 0,
        textAlign: 'center',
        textAlignVertical: 'center',
        backgroundColor: colors.colorAqua,
    },
    contractionsLoader: {
        justifyContent: 'center',
    },
    contractionDetails: {
        gap: 4,
        margin: 8,
        flexGrow: 1,
    },
    contractionDetailsTitle: {
        flexGrow: 1,
        fontSize: 19,
        color: colors.albumTitle,
    },
    contractionDetailsText: {
        flexGrow: 1,
        fontSize: 16,
    },
    bottomSection: {
        paddingVertical: 10,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: colors.buttonMaterialDark,
    },
    timerBtn: {
        width: 200,
        height: 200,
        borderRadius: 100,
        marginVertical: 10,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: colors.white,
    },
    btnTxt: {
        fontSize: 30,
        color: colors.ocean,
    },
});
