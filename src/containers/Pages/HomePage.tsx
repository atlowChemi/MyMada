import { StyleSheet, View } from 'react-native';
import { observer } from 'mobx-react-lite';
import { HomeBtn } from '../HomeBtn';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';

export const HomePage = observer<NavigatableScreenProps>(() => {
    const navigationStore = useDependencyInjector(NavigationStore);

    return (
        <BasePageView scrollViewStyle={styles.scroller} scrollContentStyle={styles.scrollContent}>
            <View>
                <HomeBtn big img={require('../../assets/homeBtns/new_tol.png')} onPress={() => {}} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_mok.png')} onPress={() => {}} />
                <HomeBtn img={require('../../assets/homeBtns/new_tes.png')} onPress={() => {}} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_con.png')} onPress={navigationStore.goToContactUs} />
                <HomeBtn img={require('../../assets/homeBtns/new_inf.png')} onPress={navigationStore.goToAboutTheApp} />
            </View>
        </BasePageView>
    );
});

const styles = StyleSheet.create({
    scroller: {
        position: 'absolute',
        top: 70,
        bottom: 0,
        right: 0,
        left: 0,
    },
    scrollContent: {
        paddingTop: undefined,
        rowGap: 10,
    },
    buttonRow: {
        flexDirection: 'row',
        columnGap: 10,
    },
});
