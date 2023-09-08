package atlow.chemi.mymada;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;

import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import com.facebook.react.modules.i18nmanager.I18nUtil;
import com.facebook.soloader.SoLoader;
import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory;

import atlow.chemi.mymada.helpers.FontsOverride;
import atlow.chemi.mymada.helpers.LocalHelper;
import atlow.chemi.mymada.recieversAndServices.BootCompleteReceiver;
import atlow.chemi.mymada.recieversAndServices.GrobootRec;
import atlow.chemi.mymada.recieversAndServices.SMSMonitorService;

import static android.service.notification.NotificationListenerService.requestRebind;

import java.util.List;

public class Applicatio extends MultiDexApplication implements ReactApplication {
    private final ReactNativeHost mReactNativeHost =
            new DefaultReactNativeHost(this) {
                @Override
                public boolean getUseDeveloperSupport() {
                    return BuildConfig.DEBUG;
                }

                @Override
                protected List<ReactPackage> getPackages() {
                    @SuppressWarnings("UnnecessaryLocalVariable")
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
    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocalHelper.onAttach(base));
        MultiDex.install(this);
    }
    @Override
    public void onCreate() {
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits())
            return;
        super.onCreate();
        SoLoader.init(this, false);
        I18nUtil sharedI18nUtilInstance = I18nUtil.getInstance();
        sharedI18nUtilInstance.allowRTL(getApplicationContext(), true);
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            DefaultNewArchitectureEntryPoint.load();
        }
        ReactNativeFlipper.initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
        FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SERIF", "fonts/opensanshebrew.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/opensanshebrew.ttf");
        SharedPreferences sharedPreferences = getSharedPreferences("Settings",0);
        boolean mapp = sharedPreferences.getBoolean("Mapp", false);
        if (mapp) {
            Intent serviceIntent = new Intent(getApplicationContext(), SMSMonitorService.class);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
            try {
                tryConnectService(getApplicationContext());
            } catch (NoClassDefFoundError e) {
//                Crashlytics.log("Connecting service failed");
//                Crashlytics.logException(e);
            }
        }
        makeRec();
    }
    public static void tryConnectService(Context mContext) {
        toggleNotificationListenerService(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ComponentName componentName = new ComponentName(mContext, NotificationService.class);
            requestRebind(componentName);
        }
    }
    public static void toggleNotificationListenerService(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        ComponentName componentName = new ComponentName(mContext, NotificationService.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    private void makeRec() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver receiver;
        receiver = new GrobootRec();
        IntentFilter filter = new IntentFilter("MADA_APP");
        filter.addAction("atlow.chemi.MyMada.MADA_APP");
        broadcastManager.registerReceiver(receiver, filter);
        IntentFilter filter2 = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        filter2.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        filter2.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter2.addAction(Intent.ACTION_PACKAGE_REPLACED);
        BroadcastReceiver bcr = new BootCompleteReceiver();
        broadcastManager.registerReceiver(bcr, filter2);
    }
}