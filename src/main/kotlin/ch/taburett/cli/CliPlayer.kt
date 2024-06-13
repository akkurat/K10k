package ch.taburett.cli

import ch.taburett.common.Player
import ch.taburett.battle.testEachInstance

class CliPlayer(override val name: String) : Player {
    override fun toString(): String = name
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