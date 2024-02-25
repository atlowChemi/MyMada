import Color from 'color';
import { memo } from 'react';
import { StyleSheet } from 'react-native';
import { useTranslation } from 'react-i18next';
import { Text } from './Text';
import { colors } from '../consts';
import { PressableRippleWrapper } from './PressableRippleWrapper';
import type { TranslationKeys } from '../localization';

const rippleColor = Color(colors.white).darken(0.4).string();

interface SettingsCardLinkProps {
    label: TranslationKeys;
    onPress(): void;
}
export const SettingsCardLink = memo<SettingsCardLinkProps>(({ label, onPress }) => {
    const { t, i18n } = useTranslation();
    return (
        <PressableRippleWrapper onPress={onPress} android_ripple={{ color: rippleColor, borderless: true }} style={styles.cardContent} wrapperStyle={styles.card}>
            <Text style={styles.text}>{t(label)}</Text>
            <Text style={[styles.caret, i18n.dir() === 'rtl' && styles.caretRTL]}>{'>'}</Text>
        </PressableRippleWrapper>
    );
});
SettingsCardLink.displayName = 'SettingsCardLink';

const styles = StyleSheet.create({
    card: {
        elevation: 7,
        marginTop: 10,
        borderRadius: 5,
        marginHorizontal: 15,
        justifyContent: 'center',
        backgroundColor: colors.white,
    },
    cardContent: {
        paddingHorizontal: 15,
        paddingVertical: 10,
        flexDirection: 'row',
    },
    text: {
        flex: 1,
        fontSize: 22,
        verticalAlign: 'middle',
    },
    caret: {
        fontSize: 30,
    },
    caretRTL: {
        transform: [{ rotateY: '180deg' }],
    },
});
