import { injectable } from 'inversify';
import { NativeModules } from 'react-native';

const { MadaNotificationsModule } = NativeModules;

@injectable()
export class MadaNotificationsStore {
    public hasMadaApp(): Promise<boolean> {
        return MadaNotificationsModule.hasMadaApp();
    }
}
