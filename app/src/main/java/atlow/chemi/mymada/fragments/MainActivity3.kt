package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import atlow.chemi.mymada.integrations.IntegrationTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.SettingsPage
import atlow.chemi.mymada.integrations.CustomDispatchScreen
import atlow.chemi.mymada.integrations.DispatchConfirmation

class MainActivity3 : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.main3 // Stable registry key for rememberSaveable across view/process recreation.
            if (SettingsPage.isRTL()) rotationY = 180f // Counter the legacy mirrored ViewPager.
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { IntegrationTheme { CustomDispatchScreen { DispatchConfirmation.show(this@MainActivity3, it) } } }
        }
}
