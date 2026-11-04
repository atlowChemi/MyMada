package atlow.chemi.mymada.integrations

object ChatContract {
    data class Message(val key: String, val sender: String, val text: String, val time: Long?, val whatsApp: Boolean)
    fun identity(value: String?): String? = value?.takeIf {
        it.isNotBlank() && it != "empty" && it.none { c -> c in ".#$[]/" || c.code < 32 || c.code == 127 }
    }
    fun message(key: String, value: Any?): Message? {
        val map = value as? Map<*, *> ?: return null
        val sender = map["sender"] as? String ?: return null
        val text = map["msg"] as? String ?: return null
        return Message(key, sender, text, (map["time"] as? Number)?.toLong(), map["WhatsApp"] == true)
    }
    fun payload(key: String, identity: String, name: String, text: String, time: Long): Map<String, Any> {
        require(identity(identity) != null && identity(key) != null)
        require(name.isNotBlank() && text.isNotBlank())
        return mapOf("Name" to name, "LastSent" to time, "SeenByAdmin" to false,
            key to mapOf("sender" to identity, "msg" to text.trim(), "time" to time))
    }
}
