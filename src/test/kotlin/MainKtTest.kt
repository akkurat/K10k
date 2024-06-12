import org.example.verifyDice
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MainKtTest {
    @Test
    fun testTrue() {
        assertAll(
            { assertTrue(verifyDice(listOf("A", "A", "B"), listOf("A", "A"))) },
            { assertTrue(verifyDice(listOf("A", "B", "C"), listOf("C", "A"))) },
        )
    }

    @Test
    fun testFalse() {
        assertAll(
            { assertFalse(verifyDice(listOf("A", "C", "B"), listOf("A", "A"))) },
            { assertFalse(verifyDice(listOf("A", "A", "B", "B"), listOf("A", "A", "B", "B", "B"))) },
        )
    }
}


