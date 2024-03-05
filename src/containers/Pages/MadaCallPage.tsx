import { observer } from 'mobx-react-lite';
import { StatusBar, StyleSheet, View } from 'react-native';
import { colors } from '../../consts';
import { Text } from '../../Components/Text';
import { DependencyInjectionProvider } from '../../services/container';
import type { FC } from 'react';

const MadaCallPageWithDI = observer((props: any) => {
    return (
        <>
            <StatusBar barStyle="light-content" backgroundColor={colors.colorPrimary} />
            <View style={styles.page}>
                <Text>LALA</Text>
            </View>
        </>
    );
});
MadaCallPageWithDI.displayName = 'MadaCallPageWithDI';

export const MadaCallPage: FC = (props: any) => (
    <DependencyInjectionProvider>
        <MadaCallPageWithDI {...props} />
    </DependencyInjectionProvider>
);
MadaCallPage.displayName = 'MadaCallPage';

const styles = StyleSheet.create({
    page: {
        flex: 1,
        backgroundColor: colors.colorPrimary,
    },
});
