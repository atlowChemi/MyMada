package atlow.chemi.mymada.integrations

/** Wire names (including the historical preference typo) belong to the deployed backend. */
object ActivationContract {
    const val KEY = "firebaseGenratedKey"
    fun validCode(code: String?) = code?.matches(Regex("[0-9]{4}")) == true
    fun validKey(key: String?) = !key.isNullOrBlank() && key.none { it in ".#$[]/" || it.code < 32 || it.code == 127 }

    enum class Result { READY, INVALID, USED, NOT_ALLOWED }
    fun validate(code: String, record: Map<*, *>?): Result = when {
        !validCode(code) || record?.get("code") != code -> Result.INVALID
        record["used"] != false -> Result.USED
        record["allowed"] != true -> Result.NOT_ALLOWED
        else -> Result.READY
    }

    fun request(email: String, name: String, reason: String, code: String, token: String, now: Long): Map<String, Any> {
        require(validCode(code) && token.isNotBlank())
        require(email.isNotBlank() && name.isNotBlank() && reason.isNotBlank())
        return mapOf("email" to email.trim(), "name" to name.trim(), "reason" to reason.trim(),
            "code" to code, "token" to token, "allowed" to false, "used" to false,
            // Retain native seconds/string until backend fixtures settle #19's discrepancy.
            "time" to (now / 1000).toString())
    }
}
