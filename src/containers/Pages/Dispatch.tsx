import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import { makeAutoObservable } from 'mobx';
import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import i18n from '../../localization';
import { colors } from '../../consts';
import { Loader } from '../../Components/Loader';
import { CustomMsg } from '../Dispatch/CustomMsg';
import { BaseDialog } from '../dialogs/BaseDialog';
import { TeamReport } from '../Dispatch/TeamReport';
import { DispatchTabBar } from '../Dispatch/TabBar';
import { DialogStore } from '../../services/DialogStore';
import { FormDictation } from '../Dispatch/FormDictation';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { MaterialTopTabScreenProps } from '@react-navigation/material-top-tabs';
import type { NavigatableScreenProps } from '../../App';

export type DispatchPage = 'Team' | 'Form' | 'General';
type DispatchNavigatorParamList = Record<DispatchPage, undefined>;
export type NavigatableDispatchScreenProps = MaterialTopTabScreenProps<DispatchNavigatorParamList, DispatchPage>;

const Tab = createMaterialTopTabNavigator<DispatchNavigatorParamList>();

@injectable()
class DispatchLocalStore {
    private hasNavigated = false;
    public showPagePromise = fromPromise(this.isAllowedToViewDispatchPage());

    constructor(private readonly dialogStore: DialogStore, private readonly navigationStore: NavigationStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, undefined, { autoBind: true });

        this.ensureDefaultDispatchSet();
    }

    public get titles() {
        return {
            Team: i18n.t('dispatch:team:title'),
            Form: i18n.t('dispatch:form:title'),
            General: i18n.t('dispatch:general:title'),
        };
    }

    private async isAllowedToViewDispatchPage() {
        const { realM } = await this.sharedPreferencesStore.settingsChannel;
        const isMarkedAsVolunteer = this.sharedPreferencesStore.makePrefBoolean(realM, false);
        if (isMarkedAsVolunteer) {
            return;
        }
        this.hasNavigated = true;
        this.navigationStore.navigateWithoutHistory('Home');
    }

    private async ensureDefaultDispatchSet() {
        const { pickedMoked } = await this.sharedPreferencesStore.settingsChannel;
        const hasUserSelectedDefaultDispatch = this.sharedPreferencesStore.makePrefBoolean(pickedMoked, false);
        if (this.hasNavigated || hasUserSelectedDefaultDispatch) {
            return;
        }
        const goToSettings = await this.dialogStore.openDialog(BaseDialog, {
            cancelText: i18n.t('common:skip'),
            okText: i18n.t('common:goToSettings'),
            negativeBtnColor: colors.colorPrimary,
            title: i18n.t('dispatch:pickDefaultDispatchTitle'),
            description: i18n.t('dispatch:pickDefaultDispatchText'),
        });
        if (goToSettings) {
            this.navigationStore.navigate('Settings', { setRegion: true });
        }
    }
}

export const Dispatch = observer<NavigatableScreenProps<'Dispatch'>>(({ route }) => {
    const localStore = useLocalStoreWithInjectedDependencies(DispatchLocalStore);

    if (localStore.showPagePromise.state === 'pending') {
        return (
            <BasePageView scrollType="none">
                <View style={styles.loadingWrapper}>
                    <Loader />
                </View>
            </BasePageView>
        );
    }

    return (
        <Tab.Navigator backBehavior="none" tabBar={DispatchTabBar} initialRouteName={route.params?.page ?? 'Team'} screenOptions={{ tabBarPressColor: colors.whiteOpacity }}>
            <Tab.Screen name="Form" options={{ title: localStore.titles.Form }} component={FormDictation} />
            <Tab.Screen name="Team" options={{ title: localStore.titles.Team }} component={TeamReport} />
            <Tab.Screen name="General" options={{ title: localStore.titles.General }} component={CustomMsg} />
        </Tab.Navigator>
    );
});
Dispatch.displayName = 'Dispatch';

const styles = StyleSheet.create({
    loadingWrapper: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
});
