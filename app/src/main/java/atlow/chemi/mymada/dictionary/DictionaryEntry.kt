package atlow.chemi.mymada.dictionary

internal data class DictionaryEntry(val key: String, val name: String, val definition: String)

/** Document identity comes from Firestore, never from an untrusted embedded key. */
internal fun dictionaryEntry(key: String, data: Map<String, Any?>): DictionaryEntry? {
    if (data["approved"] != true) return null
    val name = data["name"] as? String ?: return null
    val definition = data["definition"] as? String ?: return null
    return DictionaryEntry(key, name, definition)
}

internal fun dictionarySearch(entries: List<DictionaryEntry>, query: String): List<DictionaryEntry> =
    entries.filter { it.name.contains(query, ignoreCase = true) || it.definition.contains(query, ignoreCase = true) }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

/** Preserve moderation: never replace approved text or set approved=true from the client. */
internal fun dictionarySubmission(key: String, original: DictionaryEntry?, name: String, definition: String): Map<String, Any> {
    val term = name.trim()
    val meaning = definition.trim()
    require(term.isNotEmpty() && meaning.isNotEmpty())
    if (original == null) return mapOf("key" to key, "name" to term, "definition" to meaning, "approved" to false)
    require(key == original.key)
    return buildMap {
        if (term != original.name) put("newName", term)
        if (meaning != original.definition) put("newDefinition", meaning)
    }.also { require(it.isNotEmpty()) }
}
