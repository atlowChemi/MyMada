import { Linking } from 'react-native';
import { inject, injectable } from 'inversify';
import analytics from '@react-native-firebase/analytics';
import { DialogStore } from './DialogStore';
import { APP_PACKAGE_NAME } from '../consts';
import { SharedPreferencesStore } from './SharedPreferencesStore';
import { RateUsDialog } from '../containers/dialogs/RateUsDialog';

const MINUTE = 60_000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;
const DAYS_UNTIL_PROMPT = 3 * DAY;

const LAUNCHES_UNTIL_PROMPT = 5;

@injectable()
export class RateUsStore {
    @inject(DialogStore) private readonly dialogStore!: DialogStore;
    @inject(SharedPreferencesStore) private readonly sharedPreferencesStore!: SharedPreferencesStore;

    public async appLaunched() {
        const { dontshowagain, launch_count, date_first_launch } = await this.sharedPreferencesStore.loadChannel('rate_app');
        if (this.sharedPreferencesStore.makePrefBoolean(dontshowagain, false)) {
            return;
        }
        const launchCount = parseInt(launch_count || '0', 10) + 1;
        const firstLaunchDate = parseInt(date_first_launch?.padEnd(13, '0') || '0', 10);
        await this.sharedPreferencesStore.setItem('launch_count', launchCount.toString(), 'rate_app');
        if (!firstLaunchDate) {
            await this.sharedPreferencesStore.setItem('date_first_launch', Date.now().toString(), 'rate_app');
        }

        if (launchCount < LAUNCHES_UNTIL_PROMPT) {
            return;
        }
        analytics().logEvent('rate_us_prompt', { launchCount, firstLaunchDate });
        if (firstLaunchDate + DAYS_UNTIL_PROMPT > Date.now()) {
            return;
        }
        return this.showRateDialog();
    }

    private async showRateDialog() {
        if (this.dialogStore.hasDialogs) {
            return;
        }
        const result = await this.dialogStore.openDialog(RateUsDialog);
        // TODO: .setIcon(context.getApplicationInfo().icon)
        if (!result) {
            return;
        }
        if (['rateNow', 'never'].includes(result)) {
            await this.sharedPreferencesStore.setItem('dontshowagain', 'true', 'rate_app');
        }
        if (result === 'rateNow') {
            const MARKET_LINK = `market://details?id=${APP_PACKAGE_NAME}`;
            const PLAY_STORE_LINK = `https://play.google.com/store/apps/details?id=${APP_PACKAGE_NAME}`;
            if (await Linking.canOpenURL(MARKET_LINK)) {
                return await Linking.openURL(MARKET_LINK);
            }
            return Linking.openURL(PLAY_STORE_LINK);
        }
    }
}
