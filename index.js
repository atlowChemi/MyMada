import 'reflect-metadata';
import './src/localization/index';
import 'react-native-gesture-handler';
import messaging from '@react-native-firebase/messaging';
import crashlytics from '@react-native-firebase/crashlytics';
import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { container } from './src/services/container';
import { MessagingStore } from './src/services/MessagingStore';
import { MetronomeStore } from './src/services/MetronomeStore';
import { MadaCallPage } from './src/containers/Pages/MadaCallPage';
import { SharedPreferencesStore } from './src/services/SharedPreferencesStore';
import { MadaNotificationsStore } from './src/services/MadaNotificationsStore';

const madaNotificationsStore = container.get(MadaNotificationsStore);
const messagingStore = container.get(MessagingStore);
const metronomeStore = container.get(MetronomeStore);
const sharedPreferencesStore = container.get(SharedPreferencesStore);
messaging().setBackgroundMessageHandler(messagingStore.onMessage);
AppRegistry.registerHeadlessTask('HeadlessTask', () => async (data) => {
    if (!data || !['metronomeToggle', 'madaCallReceived', 'appStart'].includes(data.type)) {
        console.warn('Unknown headless task type', data);
        return;
    }
    if (data.type === 'metronomeToggle') {
        await metronomeStore.toggleMetronome();
        return;
    }
    const { callDetection } = await sharedPreferencesStore.applicationSettings;
    if (data.type === 'madaCallReceived') {
        const calculatedData = await madaNotificationsStore.calculateCallData(callDetection, data);
        if (!calculatedData) {
            crashlytics().recordError(new Error('Failed to calculate call data'), 'ERROR_CALCULATING_CALL_DATA');
            return;
        }
        await madaNotificationsStore.broadcastCallData(calculatedData);
        return;
    }
    if (callDetection.isCallRecognitionOn) {
        await Promise.all([madaNotificationsStore.startCallRecognitionForegroundService(), madaNotificationsStore.restartAndRebindNotificationListener()]);
    }
});
AppRegistry.registerComponent('Root', () => App);
AppRegistry.registerComponent('MadaCallPage', () => MadaCallPage);
