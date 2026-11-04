package atlow.chemi.mymada.integrations

import atlow.chemi.mymada.classes.Volunteer

object TeamContract {
    val roles = listOf("נהג", "חובש", "פראמדיק", "רופא", "מתנדב", "משתלם", "שירות לאומי")
    const val LIMIT = 4 // Both native and RN hide Add after the fourth member.
    fun body(ambulance: String, region: Int, members: List<Volunteer>): String {
        require(ambulance.isNotBlank() && members.size in 1..LIMIT)
        require(members.all { it.name.isNotBlank() && it.job in roles.indices })
        val selected = DispatchContract.region(region)
        val name = if (selected == 0) "י-ם" else DispatchContract.regions[selected]
        // Native wording remains provisional pending the explicit RN/native payload decision.
        return "צוות אמבולנס $name $ambulance:\n" + members.joinToString("\n") { "${roles[it.job]} - ${it.name}" }
    }
}
