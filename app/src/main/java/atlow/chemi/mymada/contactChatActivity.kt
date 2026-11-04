package atlow.chemi.mymada

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ChatModel
import atlow.chemi.mymada.settings.DetectionControls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

class contactChatActivity : AppCompatActivity() {
    private lateinit var model: ChatModel
    private var detection: MenuItem? = null
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chat)
        setTitle(R.string.title_activity_contact)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[ChatModel::class.java]
        val ui = androidx.compose.runtime.mutableStateOf(model.state.value!!)
        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.chat_content).setContent {
            atlow.chemi.mymada.integrations.IntegrationTheme {
                var emptyMessage by rememberSaveable { mutableStateOf(false) }
                atlow.chemi.mymada.integrations.ChatScreen(ui.value, model.name, { askForName(true) }) { text ->
                    when {
                        model.name.isBlank() -> askForName(false)
                        text.isBlank() -> emptyMessage = true
                        else -> model.send(text)
                    }
                }
                if (emptyMessage) androidx.compose.material3.AlertDialog(onDismissRequest = { emptyMessage = false },
                    title = { androidx.compose.material3.Text(getString(R.string.err)) },
                    text = { androidx.compose.material3.Text(getString(R.string.emailMsgNone)) },
                    confirmButton = { androidx.compose.material3.TextButton(onClick = { emptyMessage = false }) { androidx.compose.material3.Text(getString(R.string.ok)) } })
                if (ui.value.error) androidx.compose.material3.AlertDialog(onDismissRequest = {},
                    text = { androidx.compose.material3.Text(getString(R.string.emailBad)) }, confirmButton = {
                        androidx.compose.material3.TextButton(onClick = { model.dismissError(); model.connect() }) { androidx.compose.material3.Text(getString(R.string.ok)) }
                    })
            }
        }
        model.state.observe(this) { state ->
            ui.value = state
        }
        if (savedInstanceState == null) {
            ChatDialog().show(supportFragmentManager, "chat-disclaimer")
            if (model.name.isBlank()) askForName(false)
        }
        model.connect()
    }
    fun askForName(cancelable: Boolean) {
        if (supportFragmentManager.findFragmentByTag("chat-name") != null) return
        ChatDialog().apply { arguments = Bundle().apply { putBoolean("name", true); putBoolean("cancelable", cancelable) } }
            .show(supportFragmentManager, "chat-name")
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
    class ChatDialog : DialogFragment() {
        private var submitted: String? = null
        private var draft by mutableStateOf("")
        private var invalid by mutableStateOf(false)
        private var state by mutableStateOf(ChatModel.State())
        private lateinit var model: ChatModel
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            submitted = savedInstanceState?.getString("submitted")
            isCancelable = arguments?.getBoolean("cancelable") == true
            model = ViewModelProvider(requireActivity())[ChatModel::class.java]
            draft = savedInstanceState?.getString("draft") ?: model.name
            model.state.observe(this) {
                state = it
                if (!it.busy && !it.error && submitted != null && model.name == submitted) dismiss()
            }
        }
        override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): View =
            androidx.compose.ui.platform.ComposeView(requireContext()).apply {
                setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    atlow.chemi.mymada.integrations.IntegrationTheme {
                        atlow.chemi.mymada.integrations.ChatDialogScreen(arguments?.getBoolean("name") == true,
                            draft, { draft = it }, invalid, state.ready && !state.busy) {
                            if (arguments?.getBoolean("name") != true) {
                                if (model.name.isBlank()) (requireActivity() as contactChatActivity).askForName(false)
                                dismiss()
                            } else {
                                val name = draft.trim()
                                invalid = name.isEmpty() || name.all(Char::isDigit)
                                if (!invalid && state.ready && !state.busy) { submitted = name; model.changeName(name) }
                            }
                        }
                    }
                }
            }
        override fun onSaveInstanceState(out: Bundle) {
            out.putString("submitted", submitted)
            out.putString("draft", draft)
            super.onSaveInstanceState(out)
        }
    }
}
