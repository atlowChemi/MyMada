import { StyleSheet, View } from 'react-native';
import { observer, useLocalObservable } from 'mobx-react-lite';
import { HomeBtn } from '../HomeBtn';
import { BasePageView } from '../../Components/BasePageView';
import type { NavigatableScreenProps } from '../../App';

export const HomePage = observer<NavigatableScreenProps>(({ navigation }) => {
    const state = useLocalObservable(() => ({
        goToContactUs() {
            // TODO: show add popup
            // TODO: if is not detected as volunteer, show popup offering to request activation code.
            navigation.navigate('ContactUs');
        },
        goToInfo() {
            // TODO: show add popup
            navigation.navigate('About');
        },
    }));

    return (
        <BasePageView scrollViewStyle={styles.scroller} scrollContentStyle={styles.scrollContent} navigation={navigation}>
            <View>
                <HomeBtn big img={require('../../assets/homeBtns/new_tol.png')} onPress={() => {}} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_mok.png')} onPress={() => {}} />
                <HomeBtn img={require('../../assets/homeBtns/new_tes.png')} onPress={() => {}} />
            </View>
            <View style={styles.buttonRow}>
                <HomeBtn img={require('../../assets/homeBtns/new_con.png')} onPress={state.goToContactUs} />
                <HomeBtn img={require('../../assets/homeBtns/new_inf.png')} onPress={state.goToInfo} />
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
