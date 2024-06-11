package org.example.org.example

class Move constructor(val message: String?, val numbers: List<Int>?) {
    val valid = message == null && numbers?.isNotEmpty() == true

}
