package ch.taburett.battle

import ch.taburett.common.Game
import ch.taburett.common.Player
import kotlinx.coroutines.*

enum class RuleType {
    A, B
}

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val out: (sdf: Any) -> Unit = {} // swallow io
    runBlocking {
        val results = (1..100_000)
            .map { it to Game(out) }
            .map {
                it.first to async(Dispatchers.Default) {
                    println("${it.first} started")
                    it.second.start()
                }
            }
            .onEach { launch { it.second.await(); println("${it.first} ended") } }
            .map { it.second }
            .awaitAll()
        val grouped = results.flatMap { it.map { it.toPair() } }.groupBy({ it.first }, { it.second })


        val winner = results.map { it.maxBy { it.value }.key }
        val eachCount = winner.groupingBy { it }.eachCount()
        val s = "Each Count"
        printMapFormatted(s, eachCount)


        printMapFormatted("avg", grouped.mapValues { it.value.average() })
        printMapFormatted("min", grouped.mapValues { it.value.min() })
        printMapFormatted("max", grouped.mapValues { it.value.max() })
    }

//    results.forEachIndexed{i,v-> println("$i $v")}
}

private fun printMapFormatted(title: String, eachCount: Map<Player, Number>) {
    println(
        "=== $title ===\n" +
                eachCount.toList().sortedBy { it.second.toDouble() }
                    .joinToString("\n") { "${it.first.toString().padStart(20)} ${it.second}" }
    )
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
    for (s in possibleSub) {
        if (!avaibleMut.remove(s)) {
            return false
        }
    }
    return true
}
