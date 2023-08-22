import React from 'react';
import { SafeAreaView, ScrollView, StatusBar, StyleSheet, View } from 'react-native';
import { HomeBtn } from '../Components/HomeBtn';
import { Toolbar } from '../Components/Toolbar';
import { colors } from '../consts';
import type { useNavigationBuilder } from '@react-navigation/native';

type Navigation = ReturnType<typeof useNavigationBuilder>['navigation'];

export const HomePage: React.FC<{ navigation: Navigation }> = () => {
    return (
        <SafeAreaView style={styles.homePage}>
            <StatusBar barStyle="light-content" backgroundColor={colors.ColorPrimary} />
            <Toolbar showRoundBg />
            <ScrollView contentInsetAdjustmentBehavior="automatic" style={styles.scroller} contentContainerStyle={styles.scrollContent}>
                <View>
                    <HomeBtn big img={require('../assets/homeBtns/new_tol.png')} onPress={() => {}} />
                </View>
                <View style={styles.buttonRow}>
                    <HomeBtn img={require('../assets/homeBtns/new_mok.png')} onPress={() => {}} />
                    <HomeBtn img={require('../assets/homeBtns/new_tes.png')} onPress={() => {}} />
                </View>
                <View style={styles.buttonRow}>
                    <HomeBtn img={require('../assets/homeBtns/new_con.png')} onPress={() => {}} />
                    <HomeBtn img={require('../assets/homeBtns/new_inf.png')} onPress={() => {}} />
                </View>
            </ScrollView>
        </SafeAreaView>
    );
};

const styles = StyleSheet.create({
    homePage: {
        backgroundColor: colors.bgShadowEnd,
        height: '100%',
        flexGrow: 1,
    },
    scroller: {
        position: 'absolute',
        top: 70,
        bottom: 0,
        right: 0,
        left: 0,
    },
    scrollContent: {
        paddingBottom: 30,
        paddingHorizontal: 15,
        height: '100%',
        rowGap: 10,
    },
    buttonRow: {
        flexDirection: 'row',
        columnGap: 10,
    },
});
