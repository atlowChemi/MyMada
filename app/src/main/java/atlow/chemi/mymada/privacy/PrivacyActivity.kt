package atlow.chemi.mymada.privacy

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import atlow.chemi.mymada.R
import atlow.chemi.mymada.ui.ScreenToolbar

internal fun policyUrl(language: String): String {
    val document = if (language == "en")
        "privacy-en.html?alt=media&token=20a59b60-69e2-45b5-b274-4b311442aa0d"
    else "privacy.html?alt=media&token=a758a10d-14d0-431f-a9bc-4b61b6e25ee0"
    return "https://firebasestorage.googleapis.com/v0/b/project-3918384647339420127.appspot.com/o/$document"
}

open class PrivacyActivity : PrivacyDetectionActivity() {
    private var webView: WebView? = null
    private var loading by mutableStateOf(true)
    private var detecting by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_privacy)
        val preferences = getSharedPreferences("Settings", MODE_PRIVATE)
        // Keep the legacy service/permission behavior behind its existing MenuItem contract.
        val menu = PopupMenu(this, View(this)).menu
        onCreateOptionsMenu(menu)
        val detection = menu.findItem(R.id.one)
        setContent {
            LifecycleResumeEffect(Unit) {
                detecting = preferences.getBoolean("Mapp", false)
                detection.isChecked = detecting
                webView?.onResume()
                onPauseOrDispose { webView?.onPause() }
            }
            MaterialTheme {
                PrivacyScreen(loading, detecting, ::finish, {
                    onOptionsItemSelected(detection)
                    detecting = detection.isChecked
                }, { onOptionsItemSelected(menu.findItem(R.id.action_settings)) }) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { createWebView(savedInstanceState?.getBundle("policyWebView")) },
                    )
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(savedState: Bundle?): WebView = WebView(this).also { view ->
        webView = view
        // WebView uses its LayoutParams to decide whether its HTML viewport may shrink to content.
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val language = resources.configuration.locales[0].language
        val errorUrl = "file:///android_asset/err.html" + if (language == "en") "?lng=en" else ""
        view.settings.javaScriptEnabled = true
        view.settings.builtInZoomControls = false
        view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                loading = false
                view.visibility = View.VISIBLE
                // Preserve the native acknowledgment contract, including the offline error page.
                getSharedPreferences("Settings", MODE_PRIVATE).edit().putBoolean("privacy", true).apply()
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame && request.url.toString() != errorUrl) view.loadUrl(errorUrl)
            }
        }
        view.visibility = View.INVISIBLE
        if (savedState == null || view.restoreState(savedState) == null) view.loadUrl(policyUrl(language))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.let { view -> outState.putBundle("policyWebView", Bundle().also { view.saveState(it) }) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}

@Composable
internal fun PrivacyScreen(
    loading: Boolean,
    detecting: Boolean,
    onBack: () -> Unit,
    onDetection: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(containerColor = Color.Transparent, topBar = {
        ScreenToolbar(stringResource(R.string.title_activity_privacy), detecting, onBack, onDetection, onSettings)
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            content()
            if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}

@Preview(name = "English", locale = "en", showBackground = true)
@Preview(name = "Hebrew RTL large text", locale = "he", fontScale = 1.5f, showBackground = true)
@Composable
private fun PrivacyPreview() {
    MaterialTheme { PrivacyScreen(false, true, {}, {}, {}) { Text("Privacy policy / מדיניות פרטיות") } }
}

@Preview(name = "Loading", locale = "en", showBackground = true)
@Composable
private fun PrivacyLoadingPreview() {
    MaterialTheme { PrivacyScreen(true, false, {}, {}, {}) {} }
}
