package atlow.chemi.mymada.integrations

import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import atlow.chemi.mymada.R

/** The published local-error window, owned by the caller's saved error state. */
@Composable
internal fun IntegrationErrorDialog(message: String, onConfirm: () -> Unit) {
    val context = LocalContext.current
    val confirm by rememberUpdatedState(onConfirm)
    DisposableEffect(context, message) {
        val dialog = AlertDialog.Builder(context, R.style.AlertDialogStyle)
            .setTitle(R.string.err)
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton(R.string.ok) { _, _ -> confirm() }
            .show()
        onDispose { dialog.dismiss() }
    }
}
