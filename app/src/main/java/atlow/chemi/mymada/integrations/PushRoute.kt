package atlow.chemi.mymada.integrations

data class PushRoute(val kind: Kind, val title: String, val message: String, val code: String = "", val key: String = "") {
    enum class Kind { CHAT, ACTIVATION, REQUEST, GENERIC }
    companion object {
        fun parse(data: Map<String, String>): PushRoute? {
            val message = data["msg"].orEmpty()
            val title = data["title"].orEmpty()
            val code = data["code"].orEmpty()
            val key = (data["key"] ?: data["firebaseKey"]).orEmpty()
            return when {
                data["type"] == "newChat" || data["target"] == "ContactUs" -> PushRoute(Kind.CHAT, "הודעת צ'אט חדשה!", message)
                data["type"] == "newC" -> PushRoute(Kind.REQUEST, "בקשת קוד חדשה!", message.ifEmpty { title })
                data["type"] == "codeApproval" || code.isNotEmpty() ->
                    if (ActivationContract.validCode(code) && (key.isEmpty() || ActivationContract.validKey(key)))
                        PushRoute(Kind.ACTIVATION, "קוד ההפעלה אושר", "קוד ההפעלה שלך הינו: $code", code, key) else null
                message.isNotEmpty() && title.isNotEmpty() -> PushRoute(Kind.GENERIC, title, message)
                else -> null
            }
        }
    }
}
