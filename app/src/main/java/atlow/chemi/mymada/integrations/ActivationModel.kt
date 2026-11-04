package atlow.chemi.mymada.integrations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import atlow.chemi.mymada.R
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import java.security.SecureRandom

/** Small SDK seam for deterministic failure/acknowledgement tests; no test switches in release UI. */
interface ActivationBackend {
    fun token(): Task<String>
    fun request(payload: Map<String, Any>): Task<String>
    fun activate(key: String, code: String): Task<ActivationContract.Result>
}

class FirebaseActivationBackend(private val application: Application) : ActivationBackend {
    override fun token() = MyFirebaseMessagingService.getToken(application)
    override fun request(payload: Map<String, Any>): Task<String> {
        val ref = FirebaseDatabase.getInstance().getReference("codes").push()
        return ref.setValue(payload).continueWith { task ->
            if (!task.isSuccessful) throw task.exception ?: IllegalStateException("Request failed")
            requireNotNull(ref.key)
        }
    }
    override fun activate(key: String, code: String): Task<ActivationContract.Result> {
        val ref = FirebaseDatabase.getInstance().getReference("codes").child(key)
        return ref.get().onSuccessTask { snapshot ->
            val result = ActivationContract.validate(code, snapshot.value as? Map<*, *>)
            if (result != ActivationContract.Result.READY) Tasks.forResult(result)
            else ref.child("used").setValue(true).continueWith { task ->
                if (!task.isSuccessful) throw task.exception ?: IllegalStateException("Activation failed")
                result
            }
        }
    }
}

/** Own pending SDK work across rotation without retaining a window or repeating a request. */
class ActivationModel(application: Application) : AndroidViewModel(application) {
    internal var backend: ActivationBackend = FirebaseActivationBackend(application)
    data class State(val busy: Boolean = false, val message: Int = 0, val activated: Boolean = false, val code: String = "")
    val state = MutableLiveData(State())
    private val preferences = application.getSharedPreferences("Settings", 0)
    private var handledEntry: Pair<String?, String?>? = null
    private val pendingEntries = ArrayDeque<Pair<String?, String?>>()

    fun dismiss() { state.value = State(); nextEntry() }
    fun enter(code: String?, key: String?) {
        val entry = code to key
        if (handledEntry == entry || entry in pendingEntries) return
        pendingEntries.addLast(entry)
        nextEntry()
    }
    private fun nextEntry() {
        if (state.value?.busy == true || state.value?.message != 0 || pendingEntries.isEmpty()) return
        val entry = pendingEntries.removeFirst()
        handledEntry = entry
        activate(entry.first, entry.second)
    }

    fun submit(code: String) {
        if (state.value?.busy == true) return
        if (code.length < 4) state.value = State(message = R.string.activationCodeIncomplete)
        else activate(code, null)
    }

    fun activate(code: String?, suppliedKey: String?) {
        if (state.value?.busy == true) return
        val key = suppliedKey?.takeIf { it.isNotEmpty() } ?: preferences.getString(ActivationContract.KEY, "").orEmpty()
        if (!ActivationContract.validCode(code)) { state.value = State(message = R.string.codeBad); return }
        if (!ActivationContract.validKey(key)) { state.value = State(message = R.string.codeNotExist); return }
        state.value = State(busy = true)
        backend.activate(key, code!!).addOnCompleteListener { task ->
            if (!task.isSuccessful) state.value = State(message = R.string.emailBad)
            else when (task.result) {
                ActivationContract.Result.READY -> {
                    // Do not grant local access until the existing backend write is acknowledged.
                    preferences.edit().putBoolean("realM", true).apply()
                    state.value = State(message = R.string.codeGood, activated = true)
                }
                ActivationContract.Result.USED -> state.value = State(message = R.string.codeUsed, code = code)
                ActivationContract.Result.NOT_ALLOWED -> state.value = State(message = R.string.codeNotAllowed, code = code)
                else -> state.value = State(message = R.string.codeBad)
            }
        }
    }

    fun request(email: String, name: String, reason: String) {
        if (state.value?.busy == true) return
        state.value = State(busy = true)
        val code = SecureRandom().nextInt(10000).toString().padStart(4, '0')
        backend.token().onSuccessTask { token ->
            backend.request(ActivationContract.request(email, name, reason, code, token, System.currentTimeMillis()))
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                preferences.edit().putString(ActivationContract.KEY, task.result).apply()
                state.value = State(message = R.string.requestGood)
            } else state.value = State(message = R.string.emailBad)
        }
    }
}
