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
import { TeamReport } from '../Dispatch/TeamReport';
import { DispatchTabBar } from '../Dispatch/TabBar';
import { FormDictation } from '../Dispatch/FormDictation';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { SharedPreferencesStore } from '../../services/SharedPreferencesStore';
import { useLocalStoreWithInjectedDependencies } from '../../services/container';
import type { MaterialTopTabScreenProps } from '@react-navigation/material-top-tabs';
import type { NavigatableScreenProps } from '../../App';

type DispatchPage = 'Team' | 'Form' | 'General';
type DispatchNavigatorParamList = Record<DispatchPage, undefined>;
export type NavigatableDispatchScreenProps = MaterialTopTabScreenProps<DispatchNavigatorParamList, DispatchPage>;

const Tab = createMaterialTopTabNavigator<DispatchNavigatorParamList>();

@injectable()
class DispatchLocalStore {
    public showPagePromise = fromPromise(this.isAllowedToViewDispatchPage());

    constructor(private readonly navigationStore: NavigationStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {
        makeAutoObservable(this, undefined, { autoBind: true });

        // TODO: If there isn't a default dispatch set - show popup to select one.
    }

    public get titles() {
        return {
            Team: i18n.t('dispatch:team:title'),
            Form: i18n.t('dispatch:form:title'),
            General: i18n.t('dispatch:general:title'),
        };
    }

    private async isAllowedToViewDispatchPage() {
        this.sharedPreferencesStore.name = 'Settings';
        const { realM } = await this.sharedPreferencesStore.activeChannel;
        const isMarkedAsVolunteer = this.sharedPreferencesStore.makePrefBoolean(realM, false);
        if (!isMarkedAsVolunteer) {
            this.navigationStore.navigateWithoutHistory('Home');
        }
    }
}

export const Dispatch = observer<NavigatableScreenProps>(() => {
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
        <Tab.Navigator backBehavior="none" tabBar={DispatchTabBar} initialRouteName="Team" screenOptions={{ tabBarPressColor: colors.whiteOpacity }}>
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
