package atlow.chemi.mymada.calls

import org.junit.Assert.*
import org.junit.Test

class RecoveryWindowTest {
    @Test fun reconnectDoesNotResetClockAgeOrReviveItAfterRollback() {
        assertEquals(150_000L, RecoveryWindow.recoveryNow(120_000, 100_000, 1000, 51_000))
        assertFalse(RecoveryWindow.fresh(100_000, RecoveryWindow.recoveryNow(120_000, 100_000, 1000, 51_000)))
        assertEquals(160_000L, RecoveryWindow.recoveryNow(160_000, 100_000, 1000, 51_000))
        assertEquals(Long.MAX_VALUE, RecoveryWindow.recoveryNow(100_000, 100_000, 1000, 999))
    }
    @Test fun inclusiveFreshnessUsesBothOriginalTimestamps() {
        val now = 100_000L
        val window = RecoveryWindow(0)
        for (age in listOf(0L, 44_999L, 45_000L, 45_001L)) {
            assertEquals("source age $age", age <= 45_000,
                window.eligible("unseen", now, now - age, now))
            assertEquals("post age $age", age <= 45_000,
                window.eligible("unseen", now - age, now - age, now))
        }
        for (invalid in listOf(0L, -1L, now + 1, Long.MAX_VALUE, Long.MIN_VALUE)) {
            assertFalse(window.eligible("unseen", invalid, now, now))
            assertFalse(window.eligible("unseen", now, invalid, now))
        }
        assertFalse("A timestamp future at posting never becomes fresh by waiting",
            window.eligible("future-at-post", now - 1000, now - 500, now))
    }

    @Test fun baselineHandledUpdatesAndExpirationNeverReviveHistory() {
        val window = RecoveryWindow(100_000)
        assertFalse(window.eligible("existing", 100_000, 100_000, 100_001))
        window.remember("key+revision1", 100_001, 100_001)
        assertFalse(window.eligible("key+revision1", 100_001, 100_001, 100_002))
        assertTrue(window.eligible("key+revision2", 100_002, 100_002, 100_002))
        window.remember("later", 146_000, 146_000)
        assertFalse(window.eligible("key+revision1", 100_001, 100_001, 146_000))
        // Even clock rollback cannot revive an entry pruned from the durable ledger.
        assertFalse(window.eligible("key+revision1", 100_001, 100_001, 110_000))
        val restored = RecoveryWindow(window.excludedThrough, window.handled.toMutableMap())
        assertFalse(restored.eligible("later", 146_000, 146_000, 146_001))
    }

    @Test fun boundedOverflowExcludesEvictedHistoryRatherThanReplayingIt() {
        val window = RecoveryWindow(0)
        for (id in 1..1025) window.remember("$id", 100_000L + id, 102_000)
        assertEquals(1024, window.handled.size)
        assertFalse(window.eligible("1", 100_001, 100_001, 102_001))
        assertTrue(window.eligible("new", 102_002, 102_002, 102_002))
    }
}
