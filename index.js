import 'reflect-metadata';
import './src/localization/index';
import 'react-native-gesture-handler';
import messaging from '@react-native-firebase/messaging';
import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { container } from './src/services/container';
import { MessagingStore } from './src/services/MessagingStore';
import { MetronomeStore } from './src/services/MetronomeStore';
import { MadaCallPage } from './src/containers/Pages/MadaCallPage';
import { SharedPreferencesStore } from './src/services/SharedPreferencesStore';
import { MadaNotificationsStore } from './src/services/MadaNotificationsStore';

const messagingStore = container.get(MessagingStore);
const metronomeStore = container.get(MetronomeStore);
const sharedPreferencesStore = container.get(SharedPreferencesStore);
const madaNotificationsStore = container.get(MadaNotificationsStore);
messaging().setBackgroundMessageHandler(messagingStore.onMessage);
AppRegistry.registerHeadlessTask('HeadlessTask', () => async (data) => {
    if (data?.type === 'metronomeToggle') {
        await metronomeStore.toggleMetronome();
        return;
    }
    if (data?.type !== 'appStart') {
        return;
    }
    console.log('HeadlessTask', data);
    const { callDetection: { isCallRecognitionOn } } = await sharedPreferencesStore.applicationSettings;
    if (isCallRecognitionOn) {
        await Promise.all([madaNotificationsStore.startCallRecognitionForegroundService(), madaNotificationsStore.restartAndRebindNotificationListener()]);
    }
});
AppRegistry.registerComponent('Root', () => App);
AppRegistry.registerComponent('MadaCallPage', () => MadaCallPage);
