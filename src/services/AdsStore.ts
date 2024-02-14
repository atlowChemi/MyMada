import { injectable } from 'inversify';
import { fromPromise } from 'mobx-utils';
import crashlytics from '@react-native-firebase/crashlytics';
import mobileAds, { AdEventType, InterstitialAd, TestIds } from 'react-native-google-mobile-ads';
import { createDeferredPromise } from '../utils';

const BANNER_AD_UNIT_ID = 'ca-app-pub-7600269766434003/5732229370';
const INTERSTITIAL_AD_UNIT_ID = 'ca-app-pub-7600269766434003/4483563620';

@injectable()
export class AdsStore {
    private isLoading: Promise<void> | undefined = undefined;
    public readonly bannerAdUnitId = __DEV__ ? TestIds.BANNER : BANNER_AD_UNIT_ID;
    public readonly initialization = fromPromise(mobileAds().initialize());
    private readonly interstitialAd = InterstitialAd.createForAdRequest(__DEV__ ? TestIds.INTERSTITIAL : INTERSTITIAL_AD_UNIT_ID);

    constructor() {
        this.initialization.then(() => this.loadInterstitialAd());
        this.interstitialAd.addAdEventListener(AdEventType.CLOSED, () => this.loadInterstitialAd());
    }

    private async loadInterstitialAd() {
        await this.isLoading;
        if (this.interstitialAd.loaded) {
            return true;
        }
        const { promise, resolve, reject } = createDeferredPromise();
        const unsubscribeLoad = this.interstitialAd.addAdEventListener(AdEventType.LOADED, resolve);
        const unsubscribeError = this.interstitialAd.addAdEventListener(AdEventType.ERROR, reject);
        this.isLoading = promise;
        this.interstitialAd.load();
        try {
            await promise;
            return true;
        } catch (e) {
            e instanceof Error && crashlytics().recordError(e);
            return false;
        } finally {
            this.isLoading = undefined;
            unsubscribeLoad();
            unsubscribeError();
        }
    }

    public async showInterstitialAd() {
        if (!(await this.loadInterstitialAd())) {
            return;
        }
        return this.interstitialAd.show({ immersiveModeEnabled: true });
    }

    public async showAdInspector() {
        await mobileAds().openAdInspector();
    }
}
