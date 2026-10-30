package atlow.chemi.mymada.settings

import android.app.Activity
import android.app.Application
import android.os.Bundle
import atlow.chemi.mymada.helpers.LocalHelper
import java.util.Locale

/** Refresh every existing back-stack entry on return, not just the Settings activity. */
object LocaleRefresh : Application.ActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) {
        val selected = Locale.forLanguageTag(LocalHelper.getLanguage(activity)).language
        if (!activity.isFinishing && !activity.isChangingConfigurations &&
            activity.resources.configuration.locales[0].language != selected) activity.recreate()
    }
    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
