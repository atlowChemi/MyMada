package atlow.chemi.mymada.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;

import java.util.Locale;

public class LocalHelper {
    private static String SELECTED_LANGUAGE = "lng";
    public static Context onAttach(Context ctx) {
        String lang = getPersistedData(ctx, Locale.getDefault().getLanguage());
        SplitCompat.install(ctx);
        return setLocale(ctx, lang);
    }
    public static Context onAttach(Context ctx, String defLang) {
        String lang = getPersistedData(ctx, defLang);
        SplitCompat.install(ctx);
        return setLocale(ctx, lang);
    }
    public static String getLanguage(Context ctx) {
        return  getPersistedData(ctx, Locale.getDefault().getLanguage());
    }
    public static Context setLocale(Context ctx, String lang) {
        persist(ctx, lang);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return updateResources(ctx, lang);
        return updateResourcesLegacy(ctx, lang);
    }
    private static String getPersistedData(Context ctx, String defLang) {
        SharedPreferences preferences = ctx.getSharedPreferences("Settings", 0);
        return  preferences.getString(SELECTED_LANGUAGE, defLang);
    }
    private static void persist(Context ctx, String lang) {
        SharedPreferences preferences = ctx.getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_LANGUAGE, lang);
        editor.apply();
    }
    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context ctx, String lang) {
        Configuration configuration = ctx.getResources().getConfiguration();
        configuration.setLocale(Locale.forLanguageTag(lang));
        return ctx.createConfigurationContext(configuration);
    }
    private static Context updateResourcesLegacy(Context ctx, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = ctx.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return ctx;
    }
}
