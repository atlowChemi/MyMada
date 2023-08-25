import { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Text } from '../../Components/Text';
import { NavigationStore } from '../../services/NavigationStore';
import { useDependencyInjector } from '../../services/container';
import type { NavigatableScreenProps } from '../../App';

export const Metronome = observer<NavigatableScreenProps>(({ navigation }) => {
    const navigationStore = useDependencyInjector(NavigationStore);

    useEffect(() => {
        return navigationStore.setToolsNavigator(navigation as any);
    }, [navigationStore, navigation]);
    return (
        <>
            <Text>Metil'e</Text>
            <Text>Metil'e</Text>
            <Text>Metil'e</Text>
        </>
    );
});
