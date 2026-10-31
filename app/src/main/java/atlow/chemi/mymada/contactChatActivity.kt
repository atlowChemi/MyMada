package atlow.chemi.mymada

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import atlow.chemi.mymada.adapters.ChatAdapter
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ChatModel
import atlow.chemi.mymada.settings.DetectionControls
import com.google.android.material.textfield.TextInputLayout

class contactChatActivity : AppCompatActivity() {
    private lateinit var model: ChatModel
    private var detection: MenuItem? = null
    private var error: AlertDialog? = null
    private var sent = 0L
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_chat)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[ChatModel::class.java]
        sent = savedInstanceState?.getLong("sent") ?: model.state.value!!.sent
        val adapter = ChatAdapter({ model.name }, { askForName(true) })
        val list = findViewById<RecyclerView>(R.id.chatMsg)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter
        list.itemAnimator = DefaultItemAnimator().apply { addDuration = 1000; removeDuration = 1000 }
        val spacing = (4 * resources.displayMetrics.density + .5f).toInt()
        list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(out: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                out.set(spacing, if (parent.getChildAdapterPosition(view) == 0) spacing else 0, spacing, spacing / 9)
            }
        })
        val input = findViewById<EditText>(R.id.chatT)
        val send = findViewById<Button>(R.id.sendChat)
        val font = Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")
        input.typeface = font
        findViewById<TextInputLayout>(R.id.chatTil).typeface = font
        send.typeface = font
        send.contentDescription = getString(R.string.send)
        send.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_send, 0)
        send.setOnClickListener {
            when {
                model.name.isBlank() -> askForName(false)
                input.text.toString().isBlank() -> AlertDialog.Builder(this, R.style.AlertDialogStyle)
                    .setTitle(R.string.err).setMessage(R.string.emailMsgNone).setNegativeButton(R.string.ok, null).show()
                else -> model.send(input.text.toString())
            }
        }
        model.state.observe(this) { state ->
            send.isEnabled = state.ready && !state.busy
            input.isEnabled = !state.busy
            if (sent != state.sent) { input.setText(""); sent = state.sent }
            val grew = state.messages.size > adapter.messages.size
            adapter.messages = state.messages
            adapter.notifyDataSetChanged()
            if (grew) list.smoothScrollToPosition(adapter.itemCount - 1)
            findViewById<TextView>(R.id.msgSeen).text = state.seen?.let {
                getString(if (it) R.string.msgSeen else R.string.msgNotSeen)
            }.orEmpty()
            if (state.error && error == null) {
                error = AlertDialog.Builder(this, R.style.AlertDialogStyle).setMessage(R.string.emailBad)
                    .setCancelable(false).setPositiveButton(R.string.ok) { _, _ ->
                        error = null; model.dismissError(); model.connect()
                    }.show()
            }
        }
        if (savedInstanceState == null) ChatDialog().show(supportFragmentManager, "chat-disclaimer")
        model.connect()
    }
    fun askForName(cancelable: Boolean) {
        if (supportFragmentManager.findFragmentByTag("chat-name") != null) return
        ChatDialog().apply { arguments = Bundle().apply { putBoolean("name", true); putBoolean("cancelable", cancelable) } }
            .show(supportFragmentManager, "chat-name")
    }
    override fun onSaveInstanceState(out: Bundle) { out.putLong("sent", sent); super.onSaveInstanceState(out) }
    override fun onDestroy() { error?.dismiss(); super.onDestroy() }
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
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            submitted = savedInstanceState?.getString("submitted")
            isCancelable = arguments?.getBoolean("cancelable") == true
            val model = ViewModelProvider(requireActivity())[ChatModel::class.java]
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            if (arguments?.getBoolean("name") != true) return builder.setTitle(R.string.plsNote)
                .setMessage(R.string.contactMsg).setNegativeButton(R.string.ok) { _, _ ->
                    if (model.name.isBlank()) (requireActivity() as contactChatActivity).askForName(false)
                }.create()
            val view = layoutInflater.inflate(R.layout.shabat_t, null)
            val field = view.findViewById<EditText>(R.id.shabatT)
            val til = view.findViewById<TextInputLayout>(R.id.shabatTil)
            til.hint = getString(R.string.name)
            field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            field.setText(savedInstanceState?.getString("draft") ?: model.name)
            return builder.setTitle(R.string.writeName).setView(view).setPositiveButton(R.string.ok, null).create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = field.text.toString().trim()
                        til.error = when {
                            name.isEmpty() -> getString(R.string.emailNameNone)
                            name.all(Char::isDigit) -> getString(R.string.chatNameNum)
                            else -> null
                        }
                        if (til.error == null && model.state.value!!.ready && !model.state.value!!.busy) {
                            submitted = name
                            model.changeName(name)
                        }
                    }
                    model.state.observe(this@ChatDialog) { state ->
                        getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = state.ready && !state.busy
                        if (!state.busy && !state.error && submitted != null && model.name == submitted) dismiss()
                    }
                }
            }
        }
        override fun onSaveInstanceState(out: Bundle) {
            out.putString("submitted", submitted)
            out.putString("draft", dialog?.findViewById<EditText>(R.id.shabatT)?.text?.toString())
            super.onSaveInstanceState(out)
        }
        override fun onStart() {
            super.onStart()
            val alert = requireDialog() as AlertDialog
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            alert.findViewById<TextView>(android.R.id.message)?.typeface = font
            alert.findViewById<TextView>(android.R.id.title)?.typeface = font
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).typeface = font
            // Published12822 keeps the lone action at the right in both locales.
            (alert.getButton(AlertDialog.BUTTON_POSITIVE).parent as View).layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }
}
