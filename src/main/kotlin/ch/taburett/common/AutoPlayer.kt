package ch.taburett.common

class AutoPlayer(
    val minDice: Int = 2,
    val minPoints: Int = 500,
    name: String,
    val delay: Long = 300,
    val out: (sdf: String) -> Unit
) : Player {
    override val name = "$name/${minPoints}/${minDice}"
    override fun toString(): String = name
    override fun move(dice: List<Int>, pointsBuffer: Int, pointLog: Map<Player, Int>): List<Int> {
        out("What shall I do with $dice?")
        if (delay > 0) Thread.sleep(delay)
        val oneOfive: (Int) -> Boolean = { it == 1 || it == 5 }
        val out = mutableListOf<Int>()
        val copy = dice.toMutableList()
        val (a, b) = testStraights(dice)
        if (a > 0) {
            copy.removeAll(b)
            out.addAll(b)
        }
        val (pp, pl) = testPasch(copy)
        if (pp >= minPoints || copy.none(oneOfive)) {
            copy.removeAll(pl)
            out.addAll(pl)
        }
        if (copy.isNotEmpty()) {
            if (copy.all(oneOfive)) {
                out.addAll(copy)
                copy.clear()
            }
            if (out.isEmpty()) {
                val singleDice = copy.filter(oneOfive).minOfOrNull { it }
                copy.remove(singleDice)
                singleDice?.let { out.add(it) }
            }

            if (copy.size <= minDice) {
                val singleDice = copy.filter(oneOfive)
                out.addAll(singleDice)
                copy.removeAll(singleDice)
            }
        }
        return out

    }

    override fun writePoints(numDice: Int, points: Int, pointLog: Map<Player, Int>): Boolean {
        out("To write or not to write...")
        if (delay > 0) Thread.sleep(delay)
        if (points > 1500) {
            return true
        }

        if (numDice <= minDice) {
            return true
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AutoPlayer) return false

        if (minDice != other.minDice) return false
        if (minPoints != other.minPoints) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minDice
        result = 31 * result + minPoints
        result = 31 * result + name.hashCode()
        return result
    }

}