package ch.taburett.cli

import ch.taburett.common.AutoPlayer
import ch.taburett.common.Game

fun main() {

    val out: (Any) -> Unit = { println(it) }
    val players = listOf(
        CliPlayer("Mensch"),
        AutoPlayer(2, 300, "Avg", 0, out),
    )

    val game = Game(out, players)
    game.start()

}
