package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.core.widget.doAfterTextChanged
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.integrations.DispatchConfirmation
import com.google.android.material.textfield.TextInputLayout

class MainActivity3 : Fragment(R.layout.activity_main3) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // MokedPage's legacy ViewPager is mirrored; preserve the matching child transform.
        if (SettingsPage.isRTL()) view.rotationY = 180f
        val input = view.findViewById<EditText>(R.id.yourMsg)
        val layout = view.findViewById<TextInputLayout>(R.id.freeMsg)
        input.doAfterTextChanged { if (!it.isNullOrBlank()) layout.error = null }
        view.findViewById<Button>(R.id.yourMsgBtnNew).apply {
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_send, 0)
            setOnClickListener {
                val body = input.text.toString()
                if (body.isBlank()) layout.error = getString(R.string.customMsgErr)
                else { layout.error = null; DispatchConfirmation.show(this@MainActivity3, body) }
            }
        }
    }
}
