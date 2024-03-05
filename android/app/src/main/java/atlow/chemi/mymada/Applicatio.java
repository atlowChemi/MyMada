package atlow.chemi.mymada;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.react.flipper.ReactNativeFlipper;
import com.facebook.react.modules.i18nmanager.I18nUtil;
import com.facebook.soloader.SoLoader;
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory;

import atlow.chemi.mymada.helpers.FontsOverride;
import atlow.chemi.mymada.helpers.HeadlessJsTaskWorker;
import atlow.chemi.mymada.recieversAndServices.BootCompleteReceiver;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;
import atlow.chemi.mymada.recieversAndServices.HeadlessJsService;

import static atlow.chemi.mymada.Constants.TEST_CALL_ACTION;

import java.util.List;

public class Applicatio extends MultiDexApplication implements ReactApplication {
    private final ReactNativeHost mReactNativeHost = new DefaultReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            List<ReactPackage> packages = new PackageList(this).getPackages();
             packages.add(new MyMadaReactPackage());
            return packages;
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }

        @Override
        protected boolean isNewArchEnabled() {
            return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
        }

        @Override
        protected Boolean isHermesEnabled() {
            return BuildConfig.IS_HERMES_ENABLED;
        }
    };
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @NonNull
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits())
            return;
        super.onCreate();
        try {
            var headlessService = new Intent(getApplicationContext(), HeadlessJsService.class);
            headlessService.putExtra("type", "appStart");
            startService(headlessService);
        } catch (IllegalStateException e) {
            var work = OneTimeWorkRequest.from(HeadlessJsTaskWorker.class);
            WorkManager.getInstance(getApplicationContext()).enqueue(work);
            // TODO: Crashlytics, maybe just move this to App.jsx?
        }
        try {
            SoLoader.init(this, false);
            var sharedI18nUtilInstance = I18nUtil.getInstance();
            sharedI18nUtilInstance.allowRTL(getApplicationContext(), true);
            if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
                // If you opted-in for the New Architecture, we load the native entry point for this app.
                DefaultNewArchitectureEntryPoint.load();
            }
            ReactNativeFlipper.initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
        } catch (Exception e) {
            e.printStackTrace();
        }
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/opensanshebrew.ttf");

        setupBroadcastReceivers();
    }
    private void setupBroadcastReceivers() {
        var broadcastManager = LocalBroadcastManager.getInstance(this);
        var receiver = new GrobootRec();
        var filter = new IntentFilter("MADA_APP");
        filter.addAction(TEST_CALL_ACTION);
        broadcastManager.registerReceiver(receiver, filter);
        var filter2 = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter2.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter2.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter2.addAction(Intent.ACTION_PACKAGE_REPLACED);
        var bcr = new BootCompleteReceiver();
        broadcastManager.registerReceiver(bcr, filter2);
    }
}