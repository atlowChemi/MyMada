package atlow.chemi.mymada

import org.junit.Assert.fail
import org.junit.Test

/** Temporary negative control for #98; removed after hosted failure verification. */
class CiFailureProbeTest {
    @Test fun reportsIntentionalFailure() {
        fail("Intentional CI failure probe for #98")
    }
}
