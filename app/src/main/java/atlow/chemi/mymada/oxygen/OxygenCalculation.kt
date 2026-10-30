package atlow.chemi.mymada.oxygen

/** Provisional native policy, NOT a clinical specification. See #23 and docs/OXYGEN.md. */
internal object OxygenCalculation {
    fun validInput(value: String, digits: Int) = value.length <= digits && value.all { it in '0'..'9' }

    // Native's three-character ATM EditText also filters programmatic PSI conversions.
    // Retained pending an explicit pressure-bound/rounding decision, not a medical limit.
    fun atmFromPsi(psi: String): String? = psi.toIntOrNull()?.let { (it / 15).toString().take(3) }
    fun psiFromAtm(atm: String): String? = atm.toIntOrNull()?.let { (it * 15).toString() }
    fun minutes(atm: String, largeTank: Boolean): Int {
        val pressure = atm.toIntOrNull() ?: return 0
        require(pressure in 0..999)
        return (pressure * if (largeTank) 20.0 else 2.4).toInt() / 10
    }
}
