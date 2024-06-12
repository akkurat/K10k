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
            val selectedDice = move(dice, player)
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
                    println("Write your points?")
                    val answer = readln()
                    if (answer.startsWith("Y", ignoreCase = true)) {
                        pointLog[currentIdx] = pointLog[currentIdx]!! + pointsBuffer
                        break
                    }
                }
            }
        }
        if (pointLog.any { it.value > 10_000 }) {
            println("lucky you $player")
            break
        }
        currentIdx = (currentIdx + 1) % numPlayers
        println(pointLog.mapKeys { players[it.key] })

    }
}

private fun shuffleDice(numDice: Int) = (1..numDice).map { Random.nextInt(1, 6) }.sorted()

fun testPattern(dice: List<Int>): Int {
    val mutableDice = dice.sorted().toMutableList()


    val (straightPoints, straightDice) = testStraights(mutableDice.toList())

    mutableDice.removeAll(straightDice)

    val (paschPoints, paschDice) = testPasch(mutableDice.toList())

    mutableDice.removeAll(paschDice)

    val (singlePoints, singleDice) = singleDice(mutableDice.toList())

    mutableDice.removeAll(singleDice)

    if (mutableDice.isEmpty()) {
        return straightPoints + paschPoints + singlePoints
    }

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

private fun move(dice: List<Int>, player: String): List<Int> {
    do {
        val result = _move(dice, player)
        if (result != null) {
            return result
        }
    } while (true)
}

private fun _move(dice: List<Int>, player: String): List<Int>? {
    println(dice)
    println("[$player]: Tell me your move. What do you keep?")
    val keepingDice = readln()
    val keptDiceUnverified = try {
        if (keepingDice.isEmpty()) {
            listOf()
        } else {
            keepingDice.split(";", " ", ",", "-").map { it.toInt() }
        }
    } catch (e: NumberFormatException) {
        println("try again. $keepingDice is not a valid ")
        return null
    }
    if (verifyDice(avaible = dice, possibleSub = keptDiceUnverified)) {
        return keptDiceUnverified
    } else {
        println("You can only select available dice")
    }
    return null
}

fun <K> verifyDice(avaible: List<K>, possibleSub: List<K>): Boolean {
    val avaibleMut = avaible.toMutableList()
    for (sub in possibleSub) {
        val wasThere = avaibleMut.remove(sub)
        if (!wasThere) {
            return false
        }
    }
    return true
}

