package org.example

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
        val dice = (1..5).map ({ Random.nextInt(1, 6) })

        println(dice)
        println("[$player]: Tell me your move. What do you keep?")
        val keepingDice = readln()
        try {
            keepingDice.split(";", " ", ",", "-").map { it.toInt() }
        } catch (e : NumberFormatException) {
            println("try again. $keepingDice is not a valid ")
        }

    }
}