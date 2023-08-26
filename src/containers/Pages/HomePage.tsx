import { observer } from 'mobx-react-lite';
import { StyleSheet, View } from 'react-native';
import { LinearGradient } from 'react-native-linear-gradient';
import { colors } from '../../consts';
import { HomeBtn } from '../HomeBtn';
import { BasePageView } from '../../Components/BasePageView';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';

export const HomePage = observer<NavigatableScreenProps>(() => {
    const navigationStore = useDependencyInjector(NavigationStore);

    return (
        <BasePageView
            scrollViewStyle={styles.scroller}
            scrollContentStyle={styles.scrollContent}
            preScroll={<LinearGradient colors={['#1f425f', colors.colorPrimary, colors.colorPrimary]} angle={0} useAngle style={styles.roundBg} />}>
            <View>
                <HomeBtn big img={require('../../assets/homeBtns/new_tol.png')} onPress={navigationStore.goToTools} />
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
        top: 20,
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
    roundBg: {
        position: 'relative',
        top: 0,
        bottom: 0,
        right: '-15%',
        left: '-15%',
        width: '130%',
        height: 100,
        borderTopStartRadius: 0,
        borderTopEndRadius: 0,
        borderBottomRightRadius: 130,
        borderBottomLeftRadius: 130,
    },
});
