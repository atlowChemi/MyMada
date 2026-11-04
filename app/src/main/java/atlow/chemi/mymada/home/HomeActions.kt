package atlow.chemi.mymada.home

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.PendingIntentCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import atlow.chemi.mymada.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.lang.ref.WeakReference

class HomeActions(private val activity: FragmentActivity) {
    private var ad: InterstitialAd? = null
    init {
        MobileAds.initialize(activity.applicationContext) {}
        loadAd(this)
    }

    fun open(destination: String) {
        when (destination) {
            "tol" -> launch(activity, UtillitiesAndTools::class.java)
            "tes" -> ActionPrompt.show(activity, "test")
            "mok" -> if (BuildConfig.DEBUG) ActionPrompt.show(activity, "dispatch") else {
                val volunteer = isVolunteer(activity)
                if (volunteer) activity.getSharedPreferences("Settings", 0).edit().putBoolean("realM", true).apply()
                launch(activity, if (volunteer) MokedPage::class.java else lockPage::class.java)
            }
            "con", "inf" -> {
                val current = ad
                ad = null
                if (current == null) afterAd(destination) else {
                    val reference = WeakReference(this)
                    current.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(error: AdError) = onAdDismissedFullScreenContent()
                        override fun onAdDismissedFullScreenContent() {
                            reference.get()?.takeUnless { it.activity.isDestroyed || it.activity.isFinishing }?.let {
                                loadAd(it)
                                it.afterAd(destination)
                            }
                        }
                    }
                    current.show(activity)
                }
            }
        }
    }

    private fun afterAd(destination: String) {
        if (destination == "inf") launch(activity, InfoActivityKt::class.java)
        else if (isVolunteer(activity)) launch(activity, contactChatActivity::class.java)
        else ActionPrompt.show(activity, "contact")
    }

    class ActionPrompt : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = requireActivity()
            return AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle).apply {
                when (requireArguments().getString("kind")) {
                    "test" -> setMessage(R.string.TestCallMsg)
                        .setPositiveButton(R.string.TestCallSMS) { _, _ -> testCall(activity, 3) }
                        .setNegativeButton(R.string.TestCallApp) { _, _ -> testCall(activity, 1) }
                    "contact" -> setTitle(R.string.plsNote).setMessage(R.string.plsNoteCon)
                        .setNegativeButton(R.string.plsNoteConCode) { _, _ -> launch(activity, codeRequest::class.java) }
                        .setPositiveButton(R.string.title_activity_contact) { _, _ -> launch(activity, contactChatActivity::class.java) }
                    "dispatch" -> setMessage("זוהי גרסה לבדיקה. עמוד חסימה או עמוד דיווחים?")
                        .setPositiveButton("עמוד חסימה") { _, _ ->
                            activity.getSharedPreferences("Settings", 0).edit().putBoolean("realM", false).apply()
                            launch(activity, lockPage::class.java)
                        }.setNegativeButton("עמוד דיווחים") { _, _ ->
                            activity.getSharedPreferences("Settings", 0).edit().putBoolean("realM", true).apply()
                            launch(activity, MokedPage::class.java)
                        }
                }
            }.create()
        }
        override fun onStart() {
            super.onStart()
            val dialog = requireDialog() as AlertDialog
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            dialog.findViewById<TextView>(android.R.id.message)?.typeface = font
            dialog.findViewById<TextView>(android.R.id.title)?.typeface = font
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                typeface = font
                if (requireArguments().getString("kind") != "dispatch") setTextColor(ContextCompat.getColor(context, R.color.OldPrimary))
            }
            (dialog.getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = resources.configuration.layoutDirection
        }
        companion object {
            fun show(activity: FragmentActivity, kind: String) {
                ActionPrompt().apply { arguments = Bundle().apply { putString("kind", kind) } }
                    .show(activity.supportFragmentManager, "home-action:$kind")
            }
        }
    }

    companion object {
        private fun loadAd(owner: HomeActions) {
            val reference = WeakReference(owner)
            InterstitialAd.load(owner.activity.applicationContext, owner.activity.getString(R.string.interstitial_ad_unit_id),
                AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        reference.get()?.takeUnless { it.activity.isDestroyed }?.ad = ad
                    }
                })
        }
        private fun launch(activity: FragmentActivity, target: Class<*>) {
            activity.startActivity(Intent(activity, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        private fun isVolunteer(activity: FragmentActivity): Boolean =
            activity.getSharedPreferences("Settings", 0).getBoolean("realM", false) ||
                activity.packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency")?.resolveActivity(activity.packageManager) != null

        private fun testCall(activity: FragmentActivity, type: Int) {
            val preferences = activity.getSharedPreferences("Settings", 0)
            val error = when {
                !preferences.getBoolean("Mapp", false) ||
                    !(preferences.getBoolean("not", true) || preferences.getBoolean("win", true)) -> R.string.APP_reco_off
                preferences.getBoolean("cut", false) && !preferences.getBoolean("callT$type", true) ->
                    if (type == 3) R.string.MISHM_reco_off else R.string.CONAN_reco_off
                else -> null
            }
            if (error != null) { Toast.makeText(activity, error, Toast.LENGTH_SHORT).show(); return }
            val pending = activity.packageManager.getLaunchIntentForPackage("com.groboot.mdaemergency")?.let {
                PendingIntentCompat.getActivity(activity, System.currentTimeMillis().toInt(),
                    it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS), 0, false)
            }
            LocalBroadcastManager.getInstance(activity).sendBroadcast(Intent("atlow.chemi.MyMada.MADA_APP")
                .putExtra("sourceIntent", pending).putExtra("callT", type)
                .putExtra("text", activity.getString(if (type == 3) R.string.TEAMTest else R.string.CONANTest)))
        }
    }
}
