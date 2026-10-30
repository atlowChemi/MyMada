package atlow.chemi.mymada.dictionary

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges

/** A fixture source exercises the same lifecycle and submission callbacks without live writes. */
internal interface DictionarySource {
    fun newKey(): String
    fun listen(result: (List<DictionaryEntry>?, Boolean, Exception?) -> Unit): () -> Unit
    fun submit(key: String, edit: Boolean, fields: Map<String, Any>, result: (Exception?) -> Unit)
}

internal class FirestoreDictionarySource : DictionarySource {
    private val collection get() = FirebaseFirestore.getInstance().collection("dictionary")
    override fun newKey(): String = collection.document().id
    override fun listen(result: (List<DictionaryEntry>?, Boolean, Exception?) -> Unit): () -> Unit {
        val registration = collection.whereEqualTo("approved", true)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                result(snapshot?.documents?.mapNotNull { dictionaryEntry(it.id, it.data.orEmpty()) },
                    snapshot?.metadata?.isFromCache ?: false, error)
            }
        return { registration.remove() }
    }
    override fun submit(key: String, edit: Boolean, fields: Map<String, Any>, result: (Exception?) -> Unit) {
        val document = collection.document(key)
        val task = if (edit) document.update(fields) else document.set(fields)
        task.addOnSuccessListener { result(null) }.addOnFailureListener { result(it) }
    }
}

internal class DictionaryModel(private val source: DictionarySource = FirestoreDictionarySource()) : ViewModel() {
    var entries by mutableStateOf(emptyList<DictionaryEntry>()); private set
    var loading by mutableStateOf(true); private set
    var cached by mutableStateOf(false); private set
    var readError by mutableStateOf(false); private set
    var sending by mutableStateOf(false); private set
    var writeError by mutableStateOf(false); private set
    var success by mutableStateOf<String?>(null); private set
    var submittedEdit = false; private set
    private var removeListener: (() -> Unit)? = null
    private var generation = 0

    fun newKey(): String = source.newKey()

    fun start() {
        if (removeListener != null) return
        val current = ++generation
        loading = true
        readError = false
        removeListener = source.listen { words, fromCache, error ->
            if (current == generation) {
                loading = false
                readError = error != null
                if (words != null) {
                    // Full snapshots correctly clear empty results and removed/unapproved entries.
                    entries = words
                    cached = fromCache
                }
            }
        }
    }
    fun stop() { generation++; removeListener?.invoke(); removeListener = null }
    fun retry() { stop(); start() }
    fun submit(key: String, original: DictionaryEntry?, name: String, definition: String) {
        if (sending) return
        val fields = dictionarySubmission(key, original, name, definition)
        sending = true
        submittedEdit = original != null
        writeError = false
        success = null
        source.submit(key, original != null, fields) { error ->
            sending = false
            writeError = error != null
            if (error == null) success = key
        }
    }
    fun consumeSuccess() { success = null }
    fun clearWriteError() { writeError = false }
    override fun onCleared() = stop()
}
