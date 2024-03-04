import 'reflect-metadata';
import './src/localization/index';
import 'react-native-gesture-handler';
import messaging from '@react-native-firebase/messaging';
import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { container } from './src/services/container';
import { MessagingStore } from './src/services/MessagingStore';
import { SharedPreferencesStore } from './src/services/SharedPreferencesStore';
import { MadaNotificationsStore } from './src/services/MadaNotificationsStore';

const messagingStore = container.get(MessagingStore);
const sharedPreferencesStore = container.get(SharedPreferencesStore);
const madaNotificationsStore = container.get(MadaNotificationsStore);
messaging().setBackgroundMessageHandler(messagingStore.onMessage);
AppRegistry.registerComponent('Root', () => App);
AppRegistry.registerHeadlessTask('HeadlessTask', () => async (data) => {
    if (data?.type !== 'appStart') {
        return;
    }
    const { callDetection: { isCallRecognitionOn } } = await sharedPreferencesStore.applicationSettings;
    if (isCallRecognitionOn) {
        await Promise.all([madaNotificationsStore.startCallRecognitionForegroundService(), madaNotificationsStore.restartAndRebindNotificationListener()]);
    }
});
