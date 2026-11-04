package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.mutableStateOf
import atlow.chemi.mymada.integrations.IntegrationTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import atlow.chemi.mymada.integrations.ActivationScreen
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ActivationModel

class codeC : AppCompatActivity() {
    private lateinit var model: ActivationModel
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_c)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        model = ViewModelProvider(this)[ActivationModel::class.java]
        val ui = mutableStateOf(model.state.value!!)
        findViewById<ComposeView>(R.id.activation_content).setContent {
            IntegrationTheme {
                val state = ui.value
                ActivationScreen(state.busy) { model.activate(it, null) }
                if (state.busy) AlertDialog(onDismissRequest = {}, confirmButton = {}, text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator()
                        Text(getString(R.string.load))
                    }
                })
                if (state.message != 0) AlertDialog(onDismissRequest = {},
                    text = { Text(getString(state.message, state.code)) }, confirmButton = {
                    TextButton(onClick = {
                        model.dismiss()
                        if (state.activated) { startActivity(Intent(this, MokedPage::class.java)); finish() }
                    }) { Text(getString(R.string.ok)) }
                })
            }
        }
        model.state.observe(this) { ui.value = it }
        if (savedInstanceState == null && intent.getBooleanExtra("toCode", false)) model.enter(intent.getStringExtra("code"), intent.getStringExtra("fireKey"))
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("toCode", false)) model.enter(intent.getStringExtra("code"), intent.getStringExtra("fireKey"))
    }
}
