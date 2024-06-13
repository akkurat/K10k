package ch.taburett.common

interface Player {
    val name: String
    fun move(dice: List<Int>, pointsBuffer: Int, pointLog: Map<Player, Int>): List<Int>
    fun writePoints(dice: Int, points: Int, pointLog: Map<Player, Int>): Boolean
}