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


    // todo: until game finished
    while (true) {
        val player = players[currentIdx]
        var dice = (1..5).map { Random.nextInt(1, 6) }
        var finished = false
        while (!finished) {
            val response = move(dice, player)
            if (response.valid) {
                val newDice = response.numbers!!
                if (dice.containsAll(newDice)) {
                    dice = newDice
                }
            } else {
                println(response.message)
            }

        }


    }
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