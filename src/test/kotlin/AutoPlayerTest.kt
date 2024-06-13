import ch.taburett.common.AutoPlayer
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class AutoPlayerTest {
    @Test
    fun testAllTakeable() {
        val player = AutoPlayer(minPoints = 500)
        val dice = listOf(1, 1, 5, 5, 5)
        val result = player.move(dice = dice, 100, mapOf(0 to 0, 0 to 0))
        assertThat(result).containsExactlyInAnyOrderElementsOf(dice)
    }
    @Test
    fun testTakeMost() {
        val player = AutoPlayer(minPoints = 500)
        val dice = listOf(3, 5, 5)
        val result = player.move(dice = dice, 100, mapOf(0 to 0, 0 to 0))
        assertThat(result).containsExactlyInAnyOrder(5,5)

    }
}