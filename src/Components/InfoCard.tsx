import { observer } from 'mobx-react-lite';
import { Image, View, StyleSheet } from 'react-native';
import { Text } from './Text';
import { colors } from '../consts';

export const InfoCard = observer<{ text: string }>(({ text }) => {
    return (
        <View style={styles.card}>
            <Image style={styles.infoIcon} source={require('../assets/icons/info.webp')} />
            <View style={styles.border} />
            <Text style={styles.infoTxt}>{text}</Text>
        </View>
    );
});
InfoCard.displayName = 'InfoCard';

const styles = StyleSheet.create({
    card: {
        padding: 14,
        elevation: 1,
        paddingStart: 7,
        marginBottom: 8,
        borderRadius: 15,
        marginHorizontal: 10,
        flexDirection: 'row',
        backgroundColor: colors.white,
    },
    infoIcon: {
        flexShrink: 0,
        height: 24,
        width: 24,
    },
    border: {
        flexShrink: 0,
        width: 1,
        height: '100%',
        backgroundColor: colors.buttonMaterialDark,
        marginStart: 2,
        marginEnd: 10,
    },
    infoTxt: {
        flexGrow: 1,
        flexWrap: 'wrap',
        overflow: 'hidden',
        fontSize: 16,
        maxWidth: '90%',
    },
});
