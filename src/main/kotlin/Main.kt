package org.example

import org.example.org.example.Move
import kotlin.random.Random

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val numPlayers = 2
    val players = mutableListOf<String>()

    for (i in 1..numPlayers) {
        println("Player $i, enter your name")
        val playername = readln()
        players.add(playername)
    }

    var currentIdx = Random.nextInt(numPlayers)

    val pointLog = (0..<numPlayers).associateWith { 0 }.toMutableMap()

    // todo: until game finished
    while (true) { // player changed
        val player = players[currentIdx]
        val numDice = 5
        var dice = shuffleDice(numDice)
        var finished = false
        var pointsBuffer = 0
        while (true) {// player loop
            if( dice.none{it==5 || it == 1}) {
                println("shiiiit, no points to make. sry m8")
                break
            }
            val response = move(dice, player)
            if (response.valid) {
                val tokenDice = response.numbers!!
                // not yet working, needing seperated dice
                // just takes as much dice as it can
                if (dice.containsAll(tokenDice)) {
                    val toReshuffle = dice - tokenDice
                    if (toReshuffle.size == dice.size - tokenDice.size) {
                        val patternPoints = testPattern(tokenDice)
                        if (patternPoints > 0) {
                            // todo: wraong. needing a buffer here
                            // points only count only if you make it to the end
                            pointsBuffer += patternPoints
                            println(pointsBuffer)
                            if(pointsBuffer >= 300) {
                                println("Write your points?")
                                val answer = readln()
                                if( answer.startsWith("Y", ignoreCase = true)) {
                                    pointLog[currentIdx] = pointLog[currentIdx]!! + pointsBuffer
                                    break
                                }
                            }
                            dice = shuffleDice(toReshuffle.size)
                        } else {
                            break
                        }
                    } else {
                        println("you didn't have all dice as many times as you thought")
                    }

                } else {
                    println("naughty, you didn't have those dice")
                }

            } else {
                println(response.message)
            }
        }
        if(pointLog.any{it.value> 10_000}) {
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

private fun move(dice: List<Int>, player: String): Move {
    println(dice)
    println("[$player]: Tell me your move. What do you keep?")
    val keepingDice = readln()
    return try {
        val kept = keepingDice.split(";", " ", ",", "-").map { it.toInt() }
        Move(null, kept)
    } catch (e: NumberFormatException) {
        Move("try again. $keepingDice is not a valid ", null)
    }
}