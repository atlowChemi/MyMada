package atlow.chemi.mymada

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import atlow.chemi.mymada.integrations.ActivationScreen
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ActivationModel

class codeC : AppCompatActivity() {
    private lateinit var model: ActivationModel
    private var progress: ProgressDialog? = null
    private var message: AlertDialog? = null
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_c)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        model = ViewModelProvider(this)[ActivationModel::class.java]
        val busy = mutableStateOf(model.state.value!!.busy)
        findViewById<ComposeView>(R.id.activation_content).setContent {
            MaterialTheme { ActivationScreen(busy.value) { model.activate(it, null) } }
        }
        model.state.observe(this) { state ->
            busy.value = state.busy
            progress?.dismiss()
            progress = if (state.busy) ProgressDialog.show(this, null, getString(R.string.load), true, false) else null
            message?.dismiss()
            if (state.message != 0) {
                message = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setMessage(getString(state.message, state.code)).setCancelable(false)
                    .setNegativeButton(R.string.ok) { _, _ ->
                        model.dismiss()
                        if (state.activated) { startActivity(Intent(this, MokedPage::class.java)); finish() }
                    }.show()
            }
        }
        if (savedInstanceState == null && intent.getBooleanExtra("toCode", false)) model.enter(intent.getStringExtra("code"), intent.getStringExtra("fireKey"))
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("toCode", false)) model.enter(intent.getStringExtra("code"), intent.getStringExtra("fireKey"))
    }
    override fun onDestroy() {
        progress?.dismiss(); message?.dismiss()
        super.onDestroy()
    }
}
