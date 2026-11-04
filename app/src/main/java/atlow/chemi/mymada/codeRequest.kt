package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ActivationModel
import atlow.chemi.mymada.integrations.IntegrationErrorDialog
import atlow.chemi.mymada.settings.DetectionControls

class codeRequest : AppCompatActivity() {
    private lateinit var model: ActivationModel
    private var detection: MenuItem? = null
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.title_activity_code)
        setContentView(R.layout.activity_code_request)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[ActivationModel::class.java]
        val ui = androidx.compose.runtime.mutableStateOf(model.state.value!!)
        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.activation_request_content).setContent {
            atlow.chemi.mymada.integrations.IntegrationTheme {
                val state = ui.value
                atlow.chemi.mymada.integrations.ActivationRequestScreen(state.busy) { email, name, reason ->
                    if (supportFragmentManager.findFragmentByTag("confirm-request") == null) {
                        RequestDialog().apply { arguments = Bundle().apply {
                            putString("email", email); putString("name", name); putString("reason", reason)
                        } }.show(supportFragmentManager, "confirm-request")
                    }
                }
                if (state.busy) androidx.compose.material3.AlertDialog(onDismissRequest = {}, confirmButton = {}, text = {
                    androidx.compose.material3.Text(getString(R.string.load))
                })
                if (state.message == R.string.emailBad) IntegrationErrorDialog(getString(state.message)) { model.dismiss() }
                else if (state.message != 0) androidx.compose.material3.AlertDialog(onDismissRequest = {},
                    text = { androidx.compose.material3.Text(getString(state.message)) }, confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { model.dismiss() }) { androidx.compose.material3.Text(getString(R.string.ok)) }
                    })
            }
        }
        model.state.observe(this) { ui.value = it }
        if (savedInstanceState == null) RequestDialog().show(supportFragmentManager, "request-disclaimer")
    }
    override fun onResume() {
        super.onResume()
        detection?.let { DetectionControls.render(it, getSharedPreferences("Settings", 0).getBoolean("Mapp", false)) }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.popup_menu, menu)
        detection = menu.findItem(R.id.one).apply {
            isCheckable = true
            DetectionControls.render(this, getSharedPreferences("Settings", 0).getBoolean("Mapp", false))
        }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { finish(); true }
        R.id.one -> { DetectionControls.toggle(this, item); true }
        R.id.action_settings -> { startActivity(Intent(this, SettingsPage::class.java)); true }
        else -> super.onOptionsItemSelected(item)
    }

    class RequestDialog : DialogFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            isCancelable = false
            setStyle(STYLE_NORMAL, R.style.AlertDialogStyle)
        }
        override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View =
            androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    atlow.chemi.mymada.integrations.IntegrationTheme {
                        atlow.chemi.mymada.integrations.ActivationRequestDialog(arguments, onCancel = { dismiss() }, onConfirm = {
                            arguments?.let { args ->
                                ViewModelProvider(requireActivity())[ActivationModel::class.java].request(
                                    args.getString("email").orEmpty(), args.getString("name").orEmpty(), args.getString("reason").orEmpty())
                            }
                            dismiss()
                        })
                    }
                }
            }
    }
}
