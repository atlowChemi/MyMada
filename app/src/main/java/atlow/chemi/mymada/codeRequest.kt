package atlow.chemi.mymada

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import atlow.chemi.mymada.helpers.LocalHelper
import atlow.chemi.mymada.integrations.ActivationModel
import atlow.chemi.mymada.settings.DetectionControls
import com.google.android.material.textfield.TextInputLayout

class codeRequest : AppCompatActivity() {
    private lateinit var model: ActivationModel
    private var progress: ProgressDialog? = null
    private var result: AlertDialog? = null
    private var detection: MenuItem? = null
    override fun attachBaseContext(base: Context) = super.attachBaseContext(LocalHelper.onAttach(base))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_request)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        model = ViewModelProvider(this)[ActivationModel::class.java]
        val font = Typeface.createFromAsset(assets, "fonts/opensanshebrew.ttf")
        findViewById<TextView>(R.id.declare).typeface = font
        val fields = listOf(R.id.codeA, R.id.codeN, R.id.codeT).map { findViewById<EditText>(it) }
        val layouts = listOf(R.id.codeATIL, R.id.codeNTIL, R.id.codeTTIL).map { findViewById<TextInputLayout>(it) }
        fun validate(index: Int): Boolean {
            val text = fields[index].text.toString().trim()
            val error = when {
                text.isEmpty() -> listOf(R.string.emailNone, R.string.emailNameNone, R.string.emailMsgNone)[index]
                index == 0 && !Patterns.EMAIL_ADDRESS.matcher(text).matches() -> R.string.emailInvalid
                else -> 0
            }
            layouts[index].error = if (error == 0) null else getString(error)
            return error == 0
        }
        fields.forEachIndexed { index, field ->
            field.typeface = font
            layouts[index].typeface = font
            field.doAfterTextChanged { validate(index) }
        }
        val send = findViewById<Button>(R.id.sendCR)
        send.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_send, 0)
        send.setOnClickListener {
            if (fields.indices.map { validate(it) }.all { it }) {
                RequestDialog().apply { arguments = Bundle().apply {
                    putString("email", fields[0].text.toString().trim())
                    putString("name", fields[1].text.toString().trim())
                    putString("reason", fields[2].text.toString().trim())
                } }.show(supportFragmentManager, "confirm-request")
            }
        }
        model.state.observe(this) { state ->
            send.isEnabled = !state.busy
            fields.forEach { it.isEnabled = !state.busy }
            progress?.dismiss()
            progress = if (state.busy) ProgressDialog.show(this, null, getString(R.string.load), true, false) else null
            result?.dismiss()
            if (state.message != 0) result = AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setMessage(state.message).setCancelable(false)
                .setNegativeButton(R.string.ok) { _, _ -> model.dismiss() }.show()
        }
        if (savedInstanceState == null) RequestDialog().show(supportFragmentManager, "request-disclaimer")
    }
    override fun onDestroy() { progress?.dismiss(); result?.dismiss(); super.onDestroy() }
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
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            isCancelable = false
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            val args = arguments
            if (args == null) return builder.setTitle(R.string.plsNote).setMessage(R.string.plsNoteT)
                .setPositiveButton(R.string.ok, null).create()
            val alert = getString(R.string.emailAlert) + "\n\n"
            val sender = alert + getString(R.string.sender) + args.getString("name") + " - " + args.getString("email") + "\n"
            val subject = SpannableString(sender + "\n" + getString(R.string.msg) + "\n" + args.getString("reason"))
            subject.setSpan(ForegroundColorSpan(Color.RED), 0, alert.length, 0)
            subject.setSpan(ForegroundColorSpan(Color.rgb(35, 139, 173)), alert.length, alert.length + getString(R.string.sender).length, 0)
            subject.setSpan(ForegroundColorSpan(Color.rgb(35, 139, 173)), sender.length + 1,
                sender.length + 1 + getString(R.string.msg).length, 0)
            return builder.setTitle(R.string.suc).setMessage(subject)
                .setNegativeButton(R.string.ret, null).setPositiveButton(R.string.ok) { _, _ ->
                    ViewModelProvider(requireActivity())[ActivationModel::class.java].request(
                        args.getString("email").orEmpty(), args.getString("name").orEmpty(), args.getString("reason").orEmpty())
                }.create()
        }
        override fun onStart() {
            super.onStart()
            if (arguments == null) return
            val dialog = requireDialog() as AlertDialog
            val font = Typeface.createFromAsset(requireContext().assets, "fonts/opensanshebrew.ttf")
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).typeface = font
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                typeface = font
                setTextColor(requireContext().getColor(R.color.OldPrimary))
            }
        }
    }
}
