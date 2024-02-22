import Color from 'color';
import { StyleSheet } from 'react-native';
import { observer } from 'mobx-react-lite';
import { useTranslation } from 'react-i18next';
import { colors } from '../consts';
import { Text } from '../Components/Text';
import { Icon, type IconName } from '../Components/Icon';
import { PressableRippleWrapper } from '../Components/PressableRippleWrapper';
import type { GestureResponderEvent, StyleProp, ViewStyle } from 'react-native';

interface HomeBtnProps {
    big?: boolean;
    icon: IconName;
    size?: number;
    style?: StyleProp<ViewStyle>;
    textStyle?: StyleProp<ViewStyle>;
    wrapperStyle?: StyleProp<ViewStyle>;
    onPress(event: GestureResponderEvent): void;
}

const rippleColor = Color(colors.white).darken(0.3).string();

export const HomeBtn = observer<HomeBtnProps>(({ big, icon, size = 80, textStyle, style, wrapperStyle, onPress }) => {
    const { t, i18n } = useTranslation();

    return (
        <PressableRippleWrapper
            onPress={onPress}
            style={[styles.btnCard, style]}
            android_ripple={{ color: rippleColor, borderless: true }}
            wrapperStyle={[styles.btnWrapper, !big && styles.bigBtn, wrapperStyle]}>
            <Icon name={icon} size={size} color={colors.oldPrimary} />
            <Text style={[styles.name, i18n.language === 'en' && styles.englishName, textStyle]}>{t(`home:${icon}`)}</Text>
        </PressableRippleWrapper>
    );
});
HomeBtn.displayName = 'HomeBtn';

const styles = StyleSheet.create({
    btnWrapper: {
        height: 150,
        elevation: 5,
        flexShrink: 1,
        borderRadius: 10,
        backgroundColor: colors.white,
    },
    btnCard: {
        flex: 1,
        padding: 'auto',
        alignItems: 'center',
        alignContent: 'center',
        justifyContent: 'center',
        textAlignVertical: 'center',
    },
    bigBtn: {
        flexBasis: '50%',
    },
    image: {
        maxWidth: '100%',
        height: '100%',
        resizeMode: 'cover',
    },
    name: {
        fontSize: 28,
        fontWeight: '900',
        textAlign: 'center',
    },
    englishName: {
        letterSpacing: -2,
    },
});
