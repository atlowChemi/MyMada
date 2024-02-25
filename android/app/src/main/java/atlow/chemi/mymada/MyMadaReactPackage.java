package atlow.chemi.mymada;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atlow.chemi.mymada.ReactModules.AudioGeneratorModule;
import atlow.chemi.mymada.ReactModules.LocalizationSettingsModule;
import atlow.chemi.mymada.ReactModules.MadaNotificationsModule;
import atlow.chemi.mymada.ReactModules.MetronomeModule;
import atlow.chemi.mymada.ReactModules.NotificationsModule;
import atlow.chemi.mymada.ReactModules.SharedPreferencesModule;
import atlow.chemi.mymada.ReactModules.TimePickerModule;

public class MyMadaReactPackage  implements ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext context) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new AudioGeneratorModule(context));
        modules.add(new NotificationsModule(context));
        modules.add(new MetronomeModule(context));
        modules.add(new MadaNotificationsModule(context));
        modules.add(new SharedPreferencesModule(context));
        modules.add(new LocalizationSettingsModule(context));
        modules.add(new TimePickerModule(context));
        return modules;
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactApplicationContext) {
        return Collections.emptyList();
    }
}
