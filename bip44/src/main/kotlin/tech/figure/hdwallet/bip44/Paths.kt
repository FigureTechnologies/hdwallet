package tech.figure.hdwallet.bip44

const val BIP44_HARDENING_FLAG = 0x80000000.toInt()

object PathElements {
    /**
     * Given a BIP-32 style path like "m/44'/1'/0'/420'", generate a list of [PathElement] represented the parsed path.
     *
     * @param path The BIP-32 style derivation path to parse.
     * @return The parsed path as a list of [PathElement] instances.
     */
    fun from(path: String): List<PathElement> = path.parseBIP44Path()
}

internal fun List<PathElement>.toPathString() =
    (listOf("m") + map { it.toString() }).joinToString("/")

internal fun buildPathElement(position: Int, n: Int, hard: Boolean): PathElement =
    when (position) {
        0 -> PathElement.Purpose(n, hard)
        1 -> PathElement.CoinType(n, hard)
        2 -> PathElement.Account(n, hard)
        3 -> PathElement.Change(n, hard)
        4 -> PathElement.Index(n, hard)
        else -> error("Invalid path position: $position")
    }

/**
 * Represents the individual elements of a BIP44-style derivation path, providing
 * typed representations of the components of the derivation path.
 */
sealed class PathElement(open val number: Int, open val hardened: Boolean) {
    private fun <R> Boolean.into(t: R, f: R): R = if (this) t else f

    val hardenedNumber: Int get() = if (hardened) number or BIP44_HARDENING_FLAG else number

    override fun toString(): String = "$number${hardened.into("'", "")}"

    data class Purpose(override val number: Int, override val hardened: Boolean) : PathElement(number, hardened) {
        override fun toString(): String = super.toString()
    }

    data class CoinType(override val number: Int, override val hardened: Boolean) : PathElement(number, hardened) {
        override fun toString(): String = super.toString()
    }

    data class Account(override val number: Int, override val hardened: Boolean) : PathElement(number, hardened) {
        override fun toString(): String = super.toString()
    }

    data class Change(override val number: Int, override val hardened: Boolean) : PathElement(number, hardened) {
        override fun toString(): String = super.toString()
    }

    data class Index(override val number: Int, override val hardened: Boolean) : PathElement(number, hardened) {
        override fun toString(): String = super.toString()
    }
}

// test: m/44'/1'/0'/0/0'
// prod: m/44'/505'/0'/0/0

fun String.parseBIP44Path(): List<PathElement> {
    val s = split("/")
    require(s[0] == "m") { "No root account m/" }
    require(s.size <= 6) { "bip44 path too deep" }
    return s.drop(1).mapIndexed { position, part ->
        val l = part.takeWhile { c -> c.isDigit() }
        val n = l.toInt()
        val r = part.substring(l.length, part.length)
        val hard = r == "\'" || r.lowercase() == "h"
        require(r.isEmpty() || hard) { "Invalid hardening: $r" }
        buildPathElement(position, n, hard)
    }
}
