package atlow.chemi.mymada.helpers

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.core.app.LocaleManagerCompat
import com.google.android.play.core.splitcompat.SplitCompat
import java.util.Locale

/** Keep the installed native locale API while importing RN application locales at startup. */
object LocalHelper {
    @JvmStatic fun onAttach(context: Context): Context = onAttach(context, Locale.getDefault().language)

    @JvmStatic fun onAttach(context: Context, defaultLanguage: String): Context {
        SplitCompat.install(context)
        return localized(context, language(context, defaultLanguage))
    }

    @JvmStatic fun getLanguage(context: Context): String = language(context, Locale.getDefault().language)

    @JvmStatic fun setLocale(context: Context, language: String): Context {
        val tag = Locale.forLanguageTag(language).toLanguageTag()
        context.getSharedPreferences("Settings", 0).edit().putString("lng", tag).apply()
        if (Build.VERSION.SDK_INT >= 33) {
            context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tag)
        }
        return localized(context, tag)
    }

    private fun language(context: Context, defaultLanguage: String): String {
        if (Build.VERSION.SDK_INT >= 33) {
            LocaleManagerCompat.getApplicationLocales(context)[0]?.let { return it.toLanguageTag() }
        }
        return context.getSharedPreferences("Settings", 0).getString("lng", defaultLanguage) ?: defaultLanguage
    }

    private fun localized(context: Context, language: String): Context =
        context.createConfigurationContext(Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
        })
}
