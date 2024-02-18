import { injectable } from 'inversify';
import { Linking, Platform } from 'react-native';
import crashlytics from '@react-native-firebase/crashlytics';
import i18n from '../localization';
import { DialogStore } from './DialogStore';
import { SmsApproval } from '../containers/dialogs/SmsApproval';
import { SharedPreferencesStore } from './SharedPreferencesStore';

export type Region = 'Jerusalem' | 'Ayalon' | 'Asher' | 'Gilboa' | 'Dan' | 'Yarden' | 'Yarkon' | 'Carmel' | 'Lachish' | 'Negev' | 'Sharon' | 'Moked 4';
const REGIONS: Region[] = ['Jerusalem', 'Ayalon', 'Asher', 'Gilboa', 'Dan', 'Yarden', 'Yarkon', 'Carmel', 'Lachish', 'Negev', 'Sharon', 'Moked 4'];
const REGION_PHONE_MAP = new Map<Region, String>([
    ['Jerusalem', '+972523993348'],
    ['Ayalon', '+972523993345'],
    ['Asher', '+972523993339'],
    ['Gilboa', '+972523993338'],
    ['Dan', '+972523993342'],
    ['Yarden', '+972523993337'],
    ['Yarkon', '+972523993343'],
    ['Carmel', '+972523993340'],
    ['Lachish', '+972523993346'],
    ['Negev', '+972523993356'],
    ['Sharon', '+972523993341'],
    ['Moked 4', '+972586309592'],
]);

@injectable()
export class DispatchService {
    constructor(private readonly dialogStore: DialogStore, private readonly sharedPreferencesStore: SharedPreferencesStore) {}
    public async getRegion() {
        const { merhav: regionIndexStr } = await this.sharedPreferencesStore.settingsChannel;
        const regionIndex = parseInt(regionIndexStr, 10);
        return REGIONS[regionIndex] ?? 'Jerusalem';
    }

    public async sendSms(msg: string) {
        const region = await this.getRegion();
        crashlytics().log(`Sending SMS to ${region}`);
        const phoneNumber = REGION_PHONE_MAP.get(region);
        if (!phoneNumber) {
            const err = new Error("Region's number not found");
            crashlytics().recordError(err);
            throw err;
        }
        const shouldContinue = await this.dialogStore.openDialog(SmsApproval, { region: i18n.t(`dispatch:regions:${region}`), msg });
        if (!shouldContinue) {
            return;
        }
        const operator = Platform.select({ ios: '&', android: '?' });
        return Linking.openURL(`sms:${phoneNumber}${operator}body=${msg}`);
    }
}
