package atlow.chemi.mymada.oxygen

/** Approved app policy, not independent clinical validation. See docs/OXYGEN.md. */
internal object OxygenCalculation {
    const val MAX_ATM = 999
    const val MAX_PSI = MAX_ATM * 15
    fun validInput(value: String, maximum: Int) = value.isEmpty() ||
        value.all { it in '0'..'9' } && value.toIntOrNull()?.let { it in 0..maximum } == true

    fun atmFromPsi(psi: String): String? = psi.takeIf { validInput(it, MAX_PSI) }?.toIntOrNull()?.let { (it / 15).toString() }
    fun psiFromAtm(atm: String): String? = atm.takeIf { validInput(it, MAX_ATM) }?.toIntOrNull()?.let { (it * 15).toString() }
    fun minutes(atm: String, largeTank: Boolean): Int {
        val pressure = atm.toIntOrNull() ?: return 0
        require(pressure in 0..999)
        return (pressure * if (largeTank) 20.0 else 2.4).toInt() / 10
    }
}
