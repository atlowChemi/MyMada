package atlow.chemi.mymada.integrations

import atlow.chemi.mymada.R

/** Field order: ambulance, visa, address, patient form, commitment, amount, first, last, codes. */
object FormContract {
    fun errors(values: List<String>): List<Int> {
        require(values.size == 9)
        val errors = MutableList(9) { 0 }
        if (values[0].isBlank()) errors[0] = R.string.err_amb
        if (values[3].isBlank()) errors[3] = R.string.err_hn
        if (values[6].isBlank()) errors[6] = R.string.err_fn
        if (values[7].isBlank()) errors[7] = R.string.err_ln
        if (!values[8].matches(Regex("^[0-9]{1,4}(\\s*,\\s*[0-9]{1,4})*$"))) errors[8] = R.string.err_mc
        if (values[4].isNotBlank() || values[5].isNotBlank()) {
            if (values[4].isBlank()) errors[4] = R.string.err_ht
            if (values[5].isBlank()) errors[5] = R.string.err_sum
        }
        return errors
    }
    fun body(values: List<String>): String {
        require(errors(values).all { it == 0 })
        val trimmed = values.map { it.trim() }
        return buildString {
            append("אמב: ${values[0]}") // RN preserves ambulance whitespace.
            if (trimmed[1].isNotEmpty()) append("\nויזה: ${trimmed[1]}")
            if (trimmed[2].isNotEmpty()) append("\nכתובת: ${trimmed[2]}")
            append("\nחולה נפגע: ${trimmed[3]}")
            if (trimmed[4].isNotEmpty() && trimmed[5].isNotEmpty()) {
                append("\nהתחייבות: ${trimmed[4]}\nסכום: ${trimmed[5]}")
            }
            append("\nקוד: ${trimmed[8]}\nשם: ${trimmed[6]} ${trimmed[7]}")
        }
    }
}
