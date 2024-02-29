import { injectable } from 'inversify';
import { NativeModules } from 'react-native';

const { MadaNotificationsModule } = NativeModules;

export const enum CallType {
    OnCall = 1,
    Group = 2,
    Shift = 3,
    Administrative = 4,
}

@injectable()
export class MadaNotificationsStore {
    public hasMadaApp(): Promise<boolean> {
        return MadaNotificationsModule.hasMadaApp();
    }

    public getAppData(): Promise<{ packageName: string; uid: number }> {
        return MadaNotificationsModule.getAppData();
    }

    public triggerTestCall(config: { callType: CallType; callText: string }): Promise<void> {
        return MadaNotificationsModule.triggerTestCall(config);
    }

    public hasOverlayPermission(): Promise<boolean> {
        return MadaNotificationsModule.hasOverlayPermission();
    }

    public requestOverlayPermission(): Promise<boolean> {
        return MadaNotificationsModule.requestOverlayPermission();
    }

    public getDefaultRingtone(): Promise<string> {
        return MadaNotificationsModule.getDefaultRingtone();
    }

    public getDefaultAppRingtone(): Promise<string> {
        return MadaNotificationsModule.getDefaultAppRingtone();
    }

    public selectRingtone(title: string, picked: string): Promise<string> {
        return MadaNotificationsModule.selectRingtone(title, picked);
    }
}
