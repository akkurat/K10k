package ch.taburett.common

import ch.taburett.battle.removeAllOnlyFirstInstance
import kotlin.math.pow
import kotlin.random.Random

const val NUM_DICE = 6


class Game(val out: (sdf: Any) -> Unit, handlers: List<Player>? = null) {

    val playerhandlers: List<Player> = handlers ?: listOf(
        AutoPlayer(2, 300, "Avg", 0, out),
        AutoPlayer(2, 400, "Avg", 0, out),
        AutoPlayer(2, 500, "Avg", 0, out),
        AutoPlayer(2, 600, "Avg", 0, out),
        AutoPlayer(2, 1000, "Avg", 0, out),
        AutoPlayer(3, 300, "Coward", 0, out),
        AutoPlayer(3, 1000, "Coward", 0, out),
        AutoPlayer(1, 300, "Hc", 0, out),
        AutoPlayer(1, 1000, "Testo", 0, out),
        AutoPlayer(1, 1500, "Tc", 0, out),
    )

    fun start(): Map<Player, Int> {

        // todo: param

        val numPlayers = playerhandlers.size
        var currentIdx = Random.nextInt(numPlayers)
        val pointLog = playerhandlers.associateWith { 0 }.toMutableMap()

        while (true) { // player changed
            var dice = shuffleDice(NUM_DICE)
            var pointsBuffer = 0
            val patternsBuff = mutableListOf<List<Int>>()
            val playerHandler = playerhandlers[currentIdx]
            val playerName = playerHandler.name
            while (true) {// player loop
                val selectedDice = playerHandler.move(dice, pointsBuffer, pointLog.toMap())
                out("[$playerName] takes $selectedDice")
                if (selectedDice.isEmpty()) {
                    out("Bad luck")
                    break
                }
                val points = testPattern(selectedDice)
                if (points == 0) {
                    out("Pattern was invalid, try again")
                } else {
                    patternsBuff.add(selectedDice)
                    pointsBuffer += points
                    // if all dice were selected, one can start with 5 or 6 dice again (N)
                    val diff = dice.size - selectedDice.size
                    val num = if (diff == 0) NUM_DICE else diff
                    dice = shuffleDice(num)
                    out("[$playerName] has $pointsBuffer points / $patternsBuff")
                    if (pointsBuffer >= 300) {
                        val answer = playerHandler.writePoints(dice.size, pointsBuffer, pointLog)
                        out("Write your points?")
                        if (answer) {
                            out("[$playerName] adds $pointsBuffer points")
                            pointLog[playerHandler] = pointLog[playerHandler]!! + pointsBuffer
                            break
                        } else {
                            out("Challenging your luck...")
                        }
                    }
                }
            }
            if (pointLog.any { it.value > 10_000 }) {
                out("lucky you $playerName. you won with")
                out(pointLog)
                return pointLog
                break
            }
            currentIdx = (currentIdx + 1) % numPlayers
            out(pointLog)
        }
    }

    fun testPattern(dice: List<Int>): Int {
        val mutableDice = dice.sorted().toMutableList()

        val (straightPoints, straightDice) = testStraights(mutableDice.toList())
        mutableDice.removeAllOnlyFirstInstance(straightDice)

        val (paschPoints, paschDice) = testPasch(mutableDice.toList())
        mutableDice.removeAllOnlyFirstInstance(paschDice)

        val (singlePoints, singleDice) = singleDice(mutableDice.toList())
        mutableDice.removeAllOnlyFirstInstance(singleDice)

        if (mutableDice.isEmpty()) {
            return straightPoints + paschPoints + singlePoints
        }

        out("you have leftovers: $mutableDice")
        return 0

    }

}

private fun shuffleDice(numDice: Int) = (1..numDice).map { Random.nextInt(1, 6) }.sorted()
private fun singleDice(mutableDice: List<Int>): Pair<Int, List<Int>> {
    val mDice = mutableDice
        .filter { it == 1 || it == 5 }
        .map {
            when (it) {
                5 -> 50 to listOf(5)
                1 -> 100 to listOf(1)
                else -> 0 to emptyList()
            }
        }

    return if (mDice.isEmpty()) {
        0 to emptyList()
    } else {
        mDice.reduce { (a1, b1), (a2, b2) -> a1 + a2 to b1 + b2 }
    }
}

fun testPasch(sDice: List<Int>): Pair<Int, List<Int>> {

    val grouped = sDice.groupBy { it }

    val mapped = grouped
        .filter { it.value.size >= 3 }
        .map { valuePasch(it.key, it.value.size) to it.value }

    return if (mapped.isEmpty()) {
        0 to emptyList()
    } else {
        mapped.reduce { (a, b), (aa, bb) -> a + aa to b + bb }
    }
}

fun valuePasch(key: Int, size: Int): Int {
    val base = if (key == 1) {
        1000
    } else {
        key * 100
    }
    return base * (2).toDouble().pow(size - 3).toInt()
}

fun testStraights(dice: List<Int>): Pair<Int, List<Int>> {
    val grStr = (1..6).toList()
    if (dice.containsAll(grStr)) {
        return 1000 to grStr
    }

    val klStrl = (1..5).toList()
    if (dice.containsAll(klStrl)) {
        return 500 to klStrl;
    }
    val grStrl = (2..6).toList()
    if (dice.containsAll(klStrl)) {
        return 500 to grStrl;
    }
    return 0 to emptyList()
}
