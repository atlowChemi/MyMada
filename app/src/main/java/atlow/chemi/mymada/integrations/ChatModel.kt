package atlow.chemi.mymada.integrations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import atlow.chemi.mymada.helpers.MyFirebaseMessagingService
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*

interface ChatBackend {
    fun identity(): Task<String>
    fun listen(identity: String, changed: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit
    fun send(identity: String, name: String, text: String): Task<Void>
    fun name(identity: String, name: String): Task<Void>
}

class FirebaseChatBackend(private val application: Application) : ChatBackend {
    override fun identity(): Task<String> {
        val saved = ChatContract.identity(MyFirebaseMessagingService.chatIdentity(application))
        if (saved != null) return Tasks.forResult(saved)
        return MyFirebaseMessagingService.getToken(application).continueWith { task ->
            if (!task.isSuccessful) throw task.exception ?: IllegalStateException("Token unavailable")
            MyFirebaseMessagingService.saveToken(application, task.result)
            requireNotNull(ChatContract.identity(MyFirebaseMessagingService.chatIdentity(application)))
        }
    }
    private fun root(identity: String): DatabaseReference {
        require(ChatContract.identity(identity) != null)
        return FirebaseDatabase.getInstance().getReference("chats").child(identity)
    }
    override fun listen(identity: String, changed: (String, Any?) -> Unit, failed: () -> Unit): () -> Unit {
        val ref = root(identity)
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previous: String?) = update(snapshot)
            override fun onChildChanged(snapshot: DataSnapshot, previous: String?) = update(snapshot)
            override fun onChildRemoved(snapshot: DataSnapshot) { snapshot.key?.let { changed(it, null) } }
            override fun onChildMoved(snapshot: DataSnapshot, previous: String?) = Unit
            override fun onCancelled(error: DatabaseError) = failed()
            private fun update(snapshot: DataSnapshot) { snapshot.key?.let { changed(it, snapshot.value) } }
        }
        ref.addChildEventListener(listener)
        return { ref.removeEventListener(listener) }
    }
    override fun send(identity: String, name: String, text: String): Task<Void> {
        val ref = root(identity)
        // One acknowledgement covers metadata and message; no partially successful split writes.
        return ref.updateChildren(ChatContract.payload(requireNotNull(ref.push().key), identity, name, text, System.currentTimeMillis()))
    }
    override fun name(identity: String, name: String) = root(identity).child("Name").setValue(name)
}

class ChatModel(application: Application) : AndroidViewModel(application) {
    internal var backend: ChatBackend = FirebaseChatBackend(application)
    data class State(val ready: Boolean = false, val busy: Boolean = false, val error: Boolean = false,
        val messages: List<ChatContract.Message> = emptyList(), val seen: Boolean? = null, val sent: Long = 0)
    val state = MutableLiveData(State())
    private val preferences = application.getSharedPreferences("Settings", 0)
    val name: String get() = preferences.getString("UserName", "").orEmpty()
    private var identity: String? = null
    private var connecting = false
    private var closed = false
    private var unsubscribe: (() -> Unit)? = null
    private val messages = sortedMapOf<String, ChatContract.Message>()
    fun connect() {
        if (connecting || unsubscribe != null) return
        connecting = true
        state.value = state.value!!.copy(error = false)
        backend.identity().addOnCompleteListener { task ->
            connecting = false
            if (closed) return@addOnCompleteListener
            val key = if (task.isSuccessful) ChatContract.identity(task.result) else null
            if (key == null) { state.value = state.value!!.copy(error = true); return@addOnCompleteListener }
            identity = key
            messages.clear()
            state.value = state.value!!.copy(ready = true, messages = emptyList(), seen = null)
            unsubscribe = backend.listen(key, { child, value ->
                if (!closed) {
                    if (child == "SeenByAdmin") state.value = state.value!!.copy(seen = value as? Boolean)
                    else if (child != "Name" && child != "LastSent") {
                        val message = ChatContract.message(child, value)
                        if (message == null) messages.remove(child) else messages[child] = message
                        state.value = state.value!!.copy(messages = messages.values.toList())
                    }
                }
            }, {
                unsubscribe?.invoke(); unsubscribe = null
                if (!closed) state.value = state.value!!.copy(ready = false, error = true)
            })
        }
    }
    fun dismissError() { state.value = state.value!!.copy(error = false) }
    fun send(text: String) {
        val key = identity ?: return
        if (state.value!!.busy || !state.value!!.ready || name.isBlank() || text.isBlank()) return
        state.value = state.value!!.copy(busy = true, error = false)
        backend.send(key, name, text).addOnCompleteListener { task ->
            if (!closed) state.value = state.value!!.copy(busy = false, error = !task.isSuccessful,
                sent = state.value!!.sent + if (task.isSuccessful) 1 else 0)
        }
    }
    fun changeName(name: String) {
        val key = identity ?: return
        val trimmed = name.trim()
        if (state.value!!.busy || !state.value!!.ready || trimmed.isEmpty() || trimmed.all(Char::isDigit)) return
        state.value = state.value!!.copy(busy = true, error = false)
        backend.name(key, trimmed).addOnCompleteListener { task ->
            if (task.isSuccessful) preferences.edit().putString("UserName", trimmed).apply()
            if (!closed) state.value = state.value!!.copy(busy = false, error = !task.isSuccessful)
        }
    }
    override fun onCleared() { closed = true; unsubscribe?.invoke(); unsubscribe = null }
}
