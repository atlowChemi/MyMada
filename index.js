import 'reflect-metadata';
import './src/localization/index';
import 'react-native-gesture-handler';
import messaging from '@react-native-firebase/messaging';
import { AppRegistry } from 'react-native';
import { App } from './src/App';
import { container } from './src/services/container';
import { MessagingStore } from './src/services/MessagingStore';

const messagingStore = container.get(MessagingStore);
messaging().setBackgroundMessageHandler(messagingStore.onMessage);
AppRegistry.registerComponent('Root', () => App);
