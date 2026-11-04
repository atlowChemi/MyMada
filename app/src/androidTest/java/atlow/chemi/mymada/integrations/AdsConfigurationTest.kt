package atlow.chemi.mymada.integrations

import androidx.test.platform.app.InstrumentationRegistry
import atlow.chemi.mymada.R
import org.junit.Assert.assertEquals
import org.junit.Test

class AdsConfigurationTest {
    @Test fun debugUsesDemoUnitsInBothAppLanguages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (language in listOf("en", "he")) {
            val config = android.content.res.Configuration(context.resources.configuration)
            config.setLocale(java.util.Locale.forLanguageTag(language))
            val localized = context.createConfigurationContext(config)
            assertEquals("ca-app-pub-3940256099942544/6300978111", localized.getString(R.string.banner_ad_unit_id))
            assertEquals("ca-app-pub-3940256099942544/1033173712", localized.getString(R.string.interstitial_ad_unit_id))
        }
    }
}
