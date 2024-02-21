import { observer } from 'mobx-react-lite';
import { NavigatableSettingsScreenProps } from '../Pages/Settings';
import { useInnerNavigator } from '../../utils/useInnerNavigator';
import { BasePageView } from '../../Components/BasePageView';
import { Text } from '../../Components/Text';

export const Detection = observer<NavigatableSettingsScreenProps>(props => {
    useInnerNavigator('Settings', props);

    return (
        <BasePageView>
            <Text>Yosi</Text>
        </BasePageView>
    );
});
Detection.displayName = 'Settings.Detection';
