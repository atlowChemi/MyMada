package atlow.chemi.mymada.calls

/** Display extraction is independent of classification; missing delimiters never slice arbitrary text. */
data class CallText(val address: String, val time: String, val dispatchCode: String = "", val medicalCode: String = "", val plain: Boolean = false) {
    companion object {
        fun parse(type: Int, text: String, fallbackTime: String): CallText {
            fun between(value: String, from: String, to: String): String? {
                val start = value.indexOf(from).takeIf { it >= 0 }?.plus(from.length) ?: return null
                val end = value.indexOf(to, start).takeIf { it >= start } ?: return null
                return value.substring(start, end)
            }
            if (type == 5) {
                val body = text.substringBefore("//")
                return CallText(body, between(body, "בשעה ", " ") ?: fallbackTime, plain = true)
            }
            if (listOf("אירוע חדש", "הודעת קבוצת התראה צוות", "הודעת צוות").none(text::contains)) {
                return CallText(text, between(text, "", "מונה:") ?: between(text, "בשעה ", " ") ?: fallbackTime, plain = true)
            }
            val dispatch = between(text, "קוד הזנקה: ", " קוד רפואי") ?: between(text, "Dispatch Code: ", "Medical Code") ?: ""
            val medical = when {
                text.contains("קוד רפואי: ") -> text.substringAfter("קוד רפואי: ")
                text.contains("Medical Code: ") -> text.substringAfter("Medical Code: ")
                else -> ""
            }
            val beforeCode = text.substringBefore(if (text.contains("קוד הזנקה")) "קוד הזנקה:" else "Dispatch Code:")
            val address = between(text, "כתובת: ", " קוד") ?: between(text, "Address: ", "Dispatch Code")
            val time = between(text, ". ", if (text.contains("מונה")) "מונה:" else "Incident number:") ?: fallbackTime
            return CallText(address?.takeIf { it.isNotEmpty() } ?: beforeCode, time, dispatch, medical)
        }
    }
}
