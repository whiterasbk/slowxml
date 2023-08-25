package io.whiterasbk.kotlin.slowxml

internal fun calculateLineAndColumn(input: String, position: Int): Pair<Int, Int> {
    var line = 1
    var column = 1

    for (i in 0..<position) {
        if (i >= input.length) {
            break
        }

        if (input[i] == '\n') {
            line++
            column = 1
        } else if (input[i] == '\r') continue else {
            column ++
        }
    }

    return Pair(line, column)
}