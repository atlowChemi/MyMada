package atlow.chemi.mymada.integrations

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R

/** Snapshot the approved recipient/body across rotation; only opens the user's SMS composer. */
class DispatchConfirmation : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val body = requireArguments().getString("body").orEmpty()
        val region = DispatchContract.region(requireArguments().getInt("region"))
        val target = getString(R.string.toDispatch) + DispatchContract.regions[region] + "."
        // ponytail: retain the published 70-character estimate; actual carrier segmentation is separate.
        val count = "${body.length} ${getString(R.string.smsCountChar)} ${1 + body.length / 70} ${getString(R.string.smsCountMsg)}"
        val text = buildAnnotatedString {
            withStyle(SpanStyle(color = Color.Blue)) { append(target) }
            append("\n\n$body\n\n")
            withStyle(SpanStyle(color = Color.Gray)) { append(count) }
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    Surface {
                        Column(Modifier.padding(24.dp)) {
                            Text(getString(R.string.suc), fontSize = 20.sp)
                            Text(text, modifier = Modifier.weight(1f, fill = false).padding(vertical = 16.dp).verticalScroll(rememberScrollState()).testTag("dispatch-summary"))
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { dismiss() }, modifier = Modifier.testTag("dispatch-cancel")) { Text(getString(R.string.ret), color = colorResource(R.color.OldPrimary)) }
                                    TextButton(onClick = {
                                        try { startActivity(intent(region, body)) }
                                        catch (_: ActivityNotFoundException) { Toast.makeText(requireContext(), R.string.smsAppUnavailable, Toast.LENGTH_LONG).show() }
                                        dismiss()
                                    }, modifier = Modifier.testTag("dispatch-confirm")) { Text(getString(R.string.ok)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    companion object {
        fun intent(region: Int, body: String): Intent {
            require(body.isNotBlank())
            return Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", DispatchContract.phones[DispatchContract.region(region)], null))
                .putExtra("sms_body", body)
        }
        @JvmStatic fun show(fragment: Fragment, body: String) {
            if (body.isBlank() || fragment.parentFragmentManager.findFragmentByTag("dispatch-confirmation") != null) return
            val region = fragment.requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE).getInt("merhav", 0)
            DispatchConfirmation().apply { arguments = Bundle().apply { putInt("region", region); putString("body", body) } }
                .show(fragment.parentFragmentManager, "dispatch-confirmation")
        }
    }
}
