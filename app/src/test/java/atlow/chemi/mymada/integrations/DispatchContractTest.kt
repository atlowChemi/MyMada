package atlow.chemi.mymada.integrations

import org.junit.Assert.*
import org.junit.Test

class DispatchContractTest {
    @Test fun preservesAllTwelveNativeAndRnRecipientsAndBounds() {
        assertEquals(listOf("+972523993348", "+972523993345", "+972523993339", "+972523993338",
            "+972523993342", "+972523993337", "+972523993343", "+972523993340", "+972523993346",
            "+972523993356", "+972523993341", "+972586309592"), DispatchContract.phones)
        assertEquals(12, DispatchContract.regions.size)
        assertEquals(0, DispatchContract.region(-1)); assertEquals(0, DispatchContract.region(12))
        DispatchContract.phones.indices.forEach { assertEquals(it, DispatchContract.region(it)) }
    }
}
