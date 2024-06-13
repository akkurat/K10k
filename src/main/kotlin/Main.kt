package org.example

import kotlin.math.pow
import kotlin.random.Random

enum class RuleType {
    A, B
}

private const val NUM_DICE = 6

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val numPlayers = 2
    val players = mutableListOf("Hans", "Pferdi")
    val playerhandler = mutableListOf(CliPlayer(), AutoPlayer())
//    val players = mutableListOf<String>()
//
//    for (i in 1..numPlayers) {
//        println("Player $i, enter your name")
//        val playername = readln()
//        players.add(playername)
//    }

//    val rule: RuleType
//    println("What rules should apply? A: normal, B: extended")
//    val rules = readln()
//    rule = if (rules.startsWith("B", ignoreCase = true)) RuleType.B else RuleType.A

    var currentIdx = Random.nextInt(numPlayers)
    val pointLog = (0..<numPlayers).associateWith { 0 }.toMutableMap()

    // todo: until game finished
    while (true) { // player changed
        val player = players[currentIdx]
        var dice = shuffleDice(NUM_DICE)
        var finished = false
        var pointsBuffer = 0
        var patternsBuff = mutableListOf<List<Int>>()
        while (true) {// player loop
            val selectedDice = playerhandler[currentIdx].move(dice, player, pointsBuffer, pointLog.toMap())
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
                println("you have $pointsBuffer points / $patternsBuff")
                if (pointsBuffer >= 300) {
                    val answer = playerhandler[currentIdx].writePoints(dice.size, points, pointLog)
                    println("Write your points?")
                    if (answer) {
                        pointLog[currentIdx] = pointLog[currentIdx]!! + pointsBuffer
                        break
                    }
                }
            }
        }
        if (pointLog.any { it.value > 10_000 }) {
            println("lucky you $player. you won with")
            println( pointLog.mapKeys { players[it.key] })
            break
        }
        currentIdx = (currentIdx + 1) % numPlayers
        println(pointLog.mapKeys { players[it.key] })

    }
}

class AutoPlayer : Player {
    override fun move(dice: List<Int>, player: String, pointsBuffer: Int, toMap: Map<Int, Int>): List<Int> {
        println("$dice")
        val fiveten: (Int) -> Boolean = { it == 1 || it == 5 }
        val out = mutableListOf<Int>()
        val copy = dice.toMutableList()
        val (a, b) = testStraights(dice)
        if (a > 0) {
            copy.removeAll(b)
            out.addAll(b)
        }
        val (pp, pl) = testPasch(copy)
        if (pp >= 500) {
            copy.removeAll(pl)
            out.addAll(pl)
        }
        if (copy.isNotEmpty()) {
            if (copy.all(fiveten)) {
                copy.clear()
                out.addAll(copy)
            }
            if (out.isEmpty()) {
                val shit = copy.filter(fiveten).minOfOrNull { it }
                copy.remove(shit)
                shit?.let { out.add(it) }
            } else {
                copy.size
                val shit = copy.filter(fiveten)

                if (copy.size - shit.size <= 2) {
                    out.addAll(shit)
                }
            }
        }
        println("taking $out")
        return out

    }

    override fun writePoints(numDice: Int, points: Int, pointLog: MutableMap<Int, Int>): Boolean {
        if (points > 1500) {
            return true
        }

        if (numDice <= 2) {
            return true
        }
        return false
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

class CliPlayer : Player {
    override fun move(dice: List<Int>, player: String, pointsBuffer: Int, toMap: Map<Int, Int>): List<Int> {
        do {
            val result = _move(dice, player)
            if (result != null) {
                return result
            }
        } while (true)
    }

    override fun writePoints(dice: Int, points: Int, pointLog: MutableMap<Int, Int>): Boolean {
        while (true) {
            println("write points?")
            val answer = readln()
            if (answer.startsWith("Y", ignoreCase = true)) {
                return true
            }
            if (answer.startsWith("N", ignoreCase = true)) {
                return false
            }
        }
    }

    private fun _move(dice: List<Int>, player: String): List<Int>? {
        println(dice)
        println("[$player]: Tell me your move. What do you keep? N(one) / A(ll) / \\d+")
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
        val failures = dice.testEachInstance( possibleSub = keptDiceUnverified)
        if (failures.isEmpty()) {
            return keptDiceUnverified
        } else {
            println("You can only select available dice. $failures was not available")
        }
        return null
    }
}

interface Player {
    fun move(dice: List<Int>, player: String, pointsBuffer: Int, toMap: Map<Int, Int>): List<Int>
    fun writePoints(dice: Int, points: Int, pointLog: MutableMap<Int, Int>): Boolean
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
    for( s in possibleSub) {
        if(!avaibleMut.remove(s)) {
            return false
        }
    }
    return true
}
