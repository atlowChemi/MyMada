package atlow.chemi.mymada.integrations

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import atlow.chemi.mymada.*
import com.google.firebase.analytics.FirebaseAnalytics

/** RN NavigationStore's top-level routes; recreation/subpages do not change route. */
class SdkAnalytics(private val log: (String, Bundle) -> Unit) : Application.ActivityLifecycleCallbacks {
    private var previous = "Home"
    override fun onActivityResumed(activity: Activity) {
        val page = when (activity) {
            is HomePage -> "Home"
            is UtillitiesAndTools -> "Tools"
            is lockPage -> "Lock"
            is MokedPage -> "Dispatch"
            is contactChatActivity -> "ContactUs"
            is codeRequest -> "RequestCode"
            is codeC -> "ActivateCode"
            is InfoActivityKt -> "About"
            is PrivacyActivity -> "Privacy"
            is SettingsPage -> "Settings"
            else -> return
        }
        if (page == previous) return
        previous = page
        log(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, page)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, page)
        })
    }
    override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    companion object {
        internal fun rateParameters(launchCount: Long, firstLaunchDate: Long) = Bundle().apply {
            putLong("launchCount", launchCount)
            putLong("firstLaunchDate", firstLaunchDate)
        }
        @JvmStatic fun ratePrompt(context: Context, launchCount: Long, firstLaunchDate: Long) {
            FirebaseAnalytics.getInstance(context).logEvent("rate_us_prompt", rateParameters(launchCount, firstLaunchDate))
        }
    }
}
