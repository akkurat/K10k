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
    while (true) {
        val player = players[currentIdx]
        var dice = (1..5).map { Random.nextInt(1, 6) }.sorted()
        var finished = false
        while (!finished) {
            val response = move(dice, player)
            if (response.valid) {
                val newDice = response.numbers!!
                // not yet working, needing seperated dice
                // just takes as much dice as it can
                if (dice.containsAll(newDice)) {
                    val probNewDice = dice - newDice
                    if (probNewDice.size == dice.size-newDice.size) {
                        val pattern = testPattern(newDice)
                        if( pattern > 0 ) {
                            // todo: wraong. needing a buffer here
                            // points only count only if you make it to the end
                            pointLog[currentIdx] = pointLog[currentIdx]!! + pattern
                            println(pointLog.mapKeys { players[it.key] })
                            dice = probNewDice
                        }
                    } else {
                        println("you didn't have all dice as many times as you thought")
                    }

                }else {
                    println("naughty, you didn't have those dice")
                }

            } else {
                println(response.message)
            }

        }
    }
}

fun testPattern(newDice: List<Int>): Int {
    val sDice = newDice.sorted()
    // todo: straight, pairs
    // so far: 1 & 5

    if(sDice.all { it == 5 || it == 1 } )
    {
        return sDice.map {
            when(it) {
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