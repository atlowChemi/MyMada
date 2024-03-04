package atlow.chemi.mymada.ReactModules;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.module.annotations.ReactModule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@ReactModule(name = LocalizationSettingsModule.NAME)
public class LocalizationSettingsModule extends ReactContextBaseJavaModule {
    private final ReactContext reactContext;
    private final SharedPreferences sharedPreferences;
    private final static String LANGUAGE_SP_KEY = "lng";
    private final static String FIRST_TIME_MIGRATION = "first_time_migration";
    private final static String STATUS_DONE = "status_done";
    public static final String NAME = "LocalizationSettingsModule";

    public LocalizationSettingsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.sharedPreferences = reactContext.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public String getName() {
        return NAME;
    }

    private void putString(String key, String value) {
        var editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getLanguageTag(String language) {
        var locale = Locale.forLanguageTag(language);

        if (!locale.getCountry().equals("")) return locale.toLanguageTag();
        return new Locale(locale.getLanguage(), Locale.getDefault().getCountry()).toLanguageTag();
    }

    private String getCurrentLanguage() {
        if (!sharedPreferences.getString(FIRST_TIME_MIGRATION, "").equals(STATUS_DONE)) {
            var langSaved = sharedPreferences.getString(LANGUAGE_SP_KEY, "he");
            var localeList = LocaleListCompat.forLanguageTags(langSaved);
            AppCompatDelegate.setApplicationLocales(localeList);
            putString(FIRST_TIME_MIGRATION, STATUS_DONE);
            return langSaved;
        }
        if (AppCompatDelegate.getApplicationLocales().isEmpty()) {
            var defaultLang = LocaleListCompat.getDefault();
            AppCompatDelegate.setApplicationLocales(defaultLang);
            return defaultLang.toLanguageTags().split("-")[0];
        }
        return Objects.requireNonNull(AppCompatDelegate.getApplicationLocales().get(0)).toLanguageTag().split("-")[0];
    }

    private void setCurrentLanguage(String language) {
        var localeList = LocaleListCompat.forLanguageTags(getLanguageTag(language));
        AppCompatDelegate.setApplicationLocales(localeList);
    }

    @ReactMethod
    public void getLanguage(Promise promise) {
        promise.resolve(getCurrentLanguage());
    }

    @ReactMethod
    public void setLanguage(String language) {
        UiThreadUtil.runOnUiThread(() -> setCurrentLanguage(language));
    }

    @Override
    public Map<String, Object> getConstants() {
        var constants = new HashMap<String, Object>();
        constants.put("language", getCurrentLanguage());
        return constants;
    }
}
