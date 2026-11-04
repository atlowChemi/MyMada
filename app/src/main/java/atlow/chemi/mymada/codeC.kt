package atlow.chemi.mymada

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doAfterTextChanged
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
        val fields = listOf(R.id.code1, R.id.code2, R.id.code3, R.id.code4).map { findViewById<EditText>(it) }
        fields.forEachIndexed { index, field ->
            field.contentDescription = "${getString(R.string.title_code)} ${index + 1}"
            field.doAfterTextChanged {
                val next = if (it.isNullOrEmpty()) index - 1 else index + 1
                fields.getOrNull(next)?.requestFocus()
            }
        }
        val send = findViewById<Button>(R.id.okBBB)
        send.setOnClickListener { model.activate(fields.joinToString("") { it.text.toString() }, null) }
        model.state.observe(this) { state ->
            send.isEnabled = !state.busy
            fields.forEach { it.isEnabled = !state.busy }
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
