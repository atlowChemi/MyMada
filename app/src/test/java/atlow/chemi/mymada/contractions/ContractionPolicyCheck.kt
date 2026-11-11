package atlow.chemi.mymada.contractions

/** JVM regression checks discovered by testDebugUnitTest. */
class ContractionPolicyCheck {
    @org.junit.Test fun awayBoundariesAndHistoricalIntervals() {
        val departure = ContractionAway(1_000_000, 10_000, 7)
        for (minutes in listOf(0, 15, 20)) {
            check(!departure.expired(departure.copy(elapsed = 10_000 + minutes * 60_000L)))
        }
        check(departure.expired(departure.copy(elapsed = 1_210_001)))
        // Same boot ignores wall-clock edits; another boot uses nonnegative wall duration.
        check(!departure.expired(departure.copy(wall = Long.MAX_VALUE, elapsed = 11_000)))
        check(departure.expired(ContractionAway(2_200_001, 1, 8)))
        check(!departure.expired(ContractionAway(1, 1, 8)))
        for (rn in listOf(false, true)) {
            val historical = ContractionEntry(2, 1000, 2000, 500)
            val session = ContractionSession(rn, listOf(historical))
            session.toggle(3000)
            check(session.entries.first() == historical)
            check(session.active == ContractionEntry(3, 3000, null, 1000))
            session.toggle(2999)
            check(session.entries.last().end == 3000L)
            session.clear()
            session.toggle(5000)
            check(session.active == ContractionEntry(1, 5000, null, null))
        }
        println("PASS: away boundaries, clock/boot changes, historical intervals, unified new rows and clear")
    }
}
