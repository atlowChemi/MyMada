package atlow.chemi.mymada.integrations

import atlow.chemi.mymada.R

/** Field order: ambulance, visa, address, patient form, commitment, amount, first, last, codes. */
object FormContract {
    fun errors(values: List<String>): List<Int> {
        require(values.size == 9)
        val errors = MutableList(9) { 0 }
        if (values[0].isBlank()) errors[0] = R.string.err_amb
        if (values[3].isEmpty()) errors[3] = R.string.err_hn
        if (values[6].isEmpty()) errors[6] = R.string.err_fn
        if (values[7].isEmpty()) errors[7] = R.string.err_ln
        if (!values[8].matches(Regex("^[0-9]{1,4}(\\s*,\\s*[0-9]{1,4})*$"))) errors[8] = R.string.err_mc
        if (values[4].isNotEmpty() || values[5].isNotEmpty()) {
            if (values[4].isEmpty()) errors[4] = R.string.err_ht
            if (values[5].isEmpty()) errors[5] = R.string.err_sum
        }
        return errors
    }
    fun body(values: List<String>): String {
        require(errors(values).all { it == 0 })
        return buildString {
            append("אמב' ${values[0]}")
            if (values[1].length > 1) append("\nויזה: ${values[1]}")
            if (values[2].length > 1) append("\nכתובת: ${values[2]}")
            append("\nחולה נפגע ${values[3]}")
            if (values[4].isNotEmpty() || values[5].isNotEmpty()) {
                append("\nהתחייבות ${values[4]}\nסכום ${values[5]}")
            }
            append("\nקוד ${values[8]}\nשם: ${values[6]} ${values[7]}")
        }
    }
}
