package org.example

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
                dice = shuffleDice((dice.size - selectedDice.size + NUM_DICE) % NUM_DICE)
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

fun testPattern(newDice: List<Int>): Int {
    val sDice = newDice.sorted()
    // todo: straight, pairs
    // so far: 1 & 5

    if (sDice.all { it == 5 || it == 1 }) {
        return sDice.map {
            when (it) {
                5 -> 50
                1 -> 100
                else -> 0
            }
        }.sum()
    }
    return 0
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


//            val toReshuffle = dice - tokenDice
//            if (toReshuffle.size == dice.size - tokenDice.size) {
//                val patternPoints = testPattern(tokenDice)
//                if (patternPoints > 0) {
//                    // todo: wraong. needing a buffer here
//                    // points only count only if you make it to the end
//                    dice = shuffleDice(toReshuffle.size)
//                } else {
//                    break
//                }
//            } else {
//                println("you didn't have all dice as many times as you thought")
//            }
//
//        } else {
//            println("naughty, you didn't have those dice")
//        }
//
//    } else {
//        println(response.message)
//    }
//}