import ch.taburett.containsEachInstance
import ch.taburett.testEachInstance
import ch.taburett.valuePasch
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainKtTest {
    @Test
    fun testTrue() {
        assertAll(
            { assertTrue(listOf("A", "A", "B").containsEachInstance(listOf("A", "A"))) },
            { assertTrue(listOf("A", "B", "C").containsEachInstance(listOf("C", "A"))) },
        )
    }

    @Test
    fun testFalse() {
        assertAll(
            { assertFalse(listOf("A", "C", "B").containsEachInstance(listOf("A", "A"))) },
            { assertFalse(listOf("A", "A", "B", "B").containsEachInstance(listOf("A", "A", "B", "B", "B"))) },
        )
    }

    @Test
    fun testElementsTrue() {
        assertAll(
            { assertTrue(emptyList<String>() == listOf("A", "A", "B").testEachInstance(listOf("A", "A"))) },
            { assertTrue(emptyList<String>() == listOf("A", "B", "C").testEachInstance(listOf("C", "A"))) },
        )
    }

    @Test
    fun testElementsFalse() {
        assertAll(
            { assertFalse(listOf("A", "C", "B").containsEachInstance(listOf("A", "A"))) },
            { assertFalse(listOf("A", "A", "B", "B").containsEachInstance(listOf("A", "A", "B", "B", "B"))) },
        )
    }

    @Test
    fun testValPasch() {
        assertAll(
            { assertTrue(valuePasch(3, 3) == 300) },
            { assertTrue(valuePasch(3, 4) == 600) },
            { assertTrue(valuePasch(1, 3) == 1000) },
            { assertTrue(valuePasch(1, 5) == 4000) },
        )
    }
}



