package atlow.chemi.mymada.calls

data class ParsedCall(val type: Int, val text: String, val time: String?)

/** Native classification precedence; disputed RN differences are recorded in README.md. */
object CallParser {
    @JvmStatic fun parse(enabled: Boolean, pack: String?, title: CharSequence?, body: CharSequence?): ParsedCall? {
        if (!enabled || pack == null || !(pack.contains("mdaemergency") ||
                pack.contains("mda.health") || pack == "atlow.mymadaadmin")) return null
        val text = body?.toString() ?: "תקלה"
        val heading = title?.toString() ?: return ParsedCall(1, text, null)
        val event = heading.contains("*אירוע חדש*") || heading.contains("*New Event*")
        val administrative = heading.contains("*הודעה מנהלתית חדשה*")
        if (!event && !administrative) return null
        val service = listOf("שירות", "כלכלה", "תפילה").any(text::contains)
        val legacy = text.contains("על מידע זה חל חיסיון רפואי")
        val type = if (event) when {
            service -> 6
            listOf("מנהלתי", "מינהלתי", "משמרת בנה").any(text::contains) -> 4
            text.contains("אירוע חדש") -> 1
            text.contains("הודעת קבוצת") -> 2
            text.contains("הודעת צוות") -> 3
            legacy -> 5
            else -> 1
        } else when {
            legacy -> 5
            service -> 6
            else -> 4
        }
        val marker = if (event) {
            if (heading.contains("אירוע חדש")) "*אירוע חדש*" else "*New Event*"
        } else "*הודעה מנהלתית חדשה*"
        return ParsedCall(type, text, heading.replace(marker, ""))
    }
}
