package ch.taburett
import kotlinx.coroutines.*

import kotlin.math.pow
import kotlin.random.Random

enum class RuleType {
    A, B
}

private const val NUM_DICE = 6

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val out : (sdf:String) -> Unit = {}
    runBlocking {
        val results = (1..100_000).map { i ->
            async(Dispatchers.Default) {  game(out)}
        }.awaitAll()
    val grouped = results.flatMap { it.map { it.toPair() } }.groupBy({ it.first }, { it.second })


    val winner = results.map { it.maxBy { it.value }.key }
    val eachCount = winner.groupingBy { it }.eachCount()
    val s = "Each Count"
    printMapFormatted(s, eachCount)


    printMapFormatted("avg", grouped.mapValues { it.value.average() })
    printMapFormatted("min", grouped.mapValues { it.value.min() })
    printMapFormatted("max", grouped.mapValues { it.value.max() })
    }

//    results.forEachIndexed{i,v-> println("$i $v")}
}

private fun printMapFormatted(title: String, eachCount: Map<Player, Number>) {
    println(
        "=== $title ===\n" +
                eachCount.toList().sortedBy { it.second.toDouble() }
                    .joinToString("\n") { "${it.first.toString().padStart(20)} ${it.second}" }
    )
}

fun game(out: (sdf: String) -> Unit): Map<Player, Int> {

    val playerhandlers: List<Player> = listOf(
        AutoPlayer(2, 500, "Avg", 0),
        AutoPlayer(2, 1000, "Avg", 0),
        AutoPlayer(3, 300, "Coward", 0),
        AutoPlayer(1, 300, "Hc", 0),
        AutoPlayer(1, 1000, "Testo", 0),
        AutoPlayer(1, 1500, "Tc", 0),
    )

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
            println("[$playerName] takes $selectedDice")
            if (selectedDice.isEmpty()) {
                println("Bad luck")
                break
            }
            val points = testPattern(selectedDice)
            if (points == 0) {
                println("Pattern was invalid, try again")
            } else {
                patternsBuff.add(selectedDice)
                pointsBuffer += points
                // if all dice were selected, one can start with 5 or 6 dice again (N)
                val diff = dice.size - selectedDice.size
                val num = if (diff == 0) NUM_DICE else diff
                dice = shuffleDice(num)
                println("[$playerName] has $pointsBuffer points / $patternsBuff")
                if (pointsBuffer >= 300) {
                    val answer = playerHandler.writePoints(dice.size, pointsBuffer, pointLog)
                    println("Write your points?")
                    if (answer) {
                        println("[$playerName] adds $pointsBuffer points")
                        pointLog[playerHandler] = pointLog[playerHandler]!! + pointsBuffer
                        break
                    } else {
                        println("Challenging your luck...")
                    }
                }
            }
        }
        if (pointLog.any { it.value > 10_000 }) {
            println("lucky you $playerName. you won with")
            println(pointLog)
            return pointLog
            break
        }
        currentIdx = (currentIdx + 1) % numPlayers
        println(pointLog)
    }
}


class AutoPlayer(val minDice: Int = 2, val minPoints: Int = 500, name: String, val delay: Long = 300) : Player {
    override val name = "$name/${minPoints}/${minDice}"
    override fun toString(): String = name
    override fun move(dice: List<Int>, pointsBuffer: Int, pointLog: Map<Player, Int>): List<Int> {
        println("What shall I do with $dice?")
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
        println("To write or not to write...")
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

private fun shuffleDice(numDice: Int) = (1..numDice).map { Random.nextInt(1, 6) }.sorted()

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

    println("you have leftovers: $mutableDice")
    return 0

}

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

class CliPlayer(override val name: String) : Player {
    override fun move(dice: List<Int>, pointsBuffer: Int, pointLog: Map<Player, Int>): List<Int> {

        do {
            val result = _move(dice)
            if (result != null) {
                return result
            }
        } while (true)
    }

    override fun writePoints(dice: Int, points: Int, pointLog: Map<Player, Int>): Boolean {
        while (true) {
            println("write $points points? $dice dice left")
            val answer = readln()
            if (answer.startsWith("Y", ignoreCase = true)) {
                return true
            }
            if (answer.startsWith("N", ignoreCase = true)) {
                return false
            }
        }
    }

    private fun _move(dice: List<Int>): List<Int>? {
        println(dice)
        println("[$name]: Tell me your move. What do you keep? N(one) / A(ll) / \\d+")
        val keepingDice = readln()
        val keptDiceUnverified = try {
            if (keepingDice.startsWith("n", ignoreCase = true)) {
                emptyList()
            } else if (keepingDice.startsWith("a", ignoreCase = true)) {
                dice
            } else {
                val list = "\\d".toRegex().findAll(keepingDice).map { it.value.toInt() }.toList()
                list.ifEmpty {
                    return null
                }
            }
        } catch (e: NumberFormatException) {
            println("try again. $keepingDice is not a valid ")
            return null
        }
        val failures = dice.testEachInstance(possibleSub = keptDiceUnverified)
        if (failures.isEmpty()) {
            return keptDiceUnverified
        } else {
            println("You can only select available dice. $failures was not available")
        }
        return null
    }
}

interface Player {
    val name: String
    fun move(dice: List<Int>, pointsBuffer: Int, pointLog: Map<Player, Int>): List<Int>
    fun writePoints(dice: Int, points: Int, pointLog: Map<Player, Int>): Boolean
}


/**
 * remove all
 */
fun <K> MutableList<K>.removeAllOnlyFirstInstance(possibleSub: List<K>): List<K> {
    val failures = mutableListOf<K>()
    for (sub in possibleSub) {
        val wasThere = this.remove(sub)
        if (!wasThere) {
            failures.add(sub)
        }
    }
    return failures
}

/**
 *
 */
fun <K> List<K>.testEachInstance(possibleSub: List<K>): List<K> {
    val avaibleMut = this.toMutableList()
    val failures = mutableListOf<K>()
    for (sub in possibleSub) {
        val wasThere = avaibleMut.remove(sub)
        if (!wasThere) {
            failures.add(sub)
        }
    }
    return failures
}

fun <K> List<K>.containsEachInstance(possibleSub: List<K>): Boolean {
    val avaibleMut = this.toMutableList()
    for (s in possibleSub) {
        if (!avaibleMut.remove(s)) {
            return false
        }
    }
    return true
}
