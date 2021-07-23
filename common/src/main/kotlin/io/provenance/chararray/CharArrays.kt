package io.provenance.chararray

fun CharArray.split(delimiter: Char): List<CharArray> {
    var currentOffset = 0
    var nextIndex = indexOf(delimiter)
    if (nextIndex == -1) {
        return listOf(this)
    }

    val result = ArrayList<CharArray>(10)
    do {
        result.add(sliceArray(currentOffset until nextIndex))
        currentOffset = nextIndex + 1
        nextIndex = indexOf(delimiter, currentOffset)
    } while (nextIndex != -1)

    result.add(sliceArray(currentOffset until size))
    return result
}

fun CharArray.indexOf(c: Char, startIndex: Int): Int {
    if (startIndex >= size) {
        return -1
    }

    val indicies = startIndex until size
    for (index in indicies) {
        if (this[index] == c) {
            return index
        }
    }
    return -1
}
