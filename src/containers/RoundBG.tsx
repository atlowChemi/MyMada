import { memo } from 'react';
import { StyleSheet } from 'react-native';
import { LinearGradient } from 'react-native-linear-gradient';
import { colors } from '../consts';

export const RoundBG = memo(() => <LinearGradient colors={['#1f425f', colors.colorPrimary, colors.colorPrimary]} angle={0} useAngle style={styles.roundBg} />);
RoundBG.displayName = 'RoundBG';

const styles = StyleSheet.create({
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
