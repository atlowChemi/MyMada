package atlow.chemi.mymada.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import atlow.chemi.mymada.R
import atlow.chemi.mymada.tools.vitals.*
import com.google.firebase.database.*

class VitalsChecker : Fragment() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        requireActivity().setTitle(R.string.button_e)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        ComposeView(requireContext()).apply {
            id = R.id.vitalsResult
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var ranges by remember { mutableStateOf(nativeVitals) }
                DisposableEffect(Unit) {
                    val reference = FirebaseDatabase.getInstance().getReference("Vitals")
                    var active = true
                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!active) return
                            ranges = listOf("Old", "Child", "Baby").mapIndexed { index, age ->
                                val fallback = nativeVitals[index]
                                fun range(path: String, default: VitalRange): VitalRange {
                                    val node = snapshot.child(path)
                                    return remoteVitalRange(node.child("${age}_low").value,
                                        node.child("${age}_high").value, default)
                                }
                                VitalRanges(range("Pulse", fallback.pulse), range("Breathing", fallback.breath),
                                    range("BloodPressure/Systolic", fallback.systolic),
                                    range("BloodPressure/Diastolic", fallback.diastolic))
                            }
                        }
                        override fun onCancelled(error: DatabaseError) { /* Preserve offline defaults. */ }
                    }
                    reference.addListenerForSingleValueEvent(listener)
                    onDispose { active = false; reference.removeEventListener(listener) }
                }
                VitalsScreen(ranges)
            }
        }
}
