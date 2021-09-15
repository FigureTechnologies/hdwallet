package io.provenance.hdwallet.bip44

object PathElements {
    fun from(path: String): List<PathElement> {
        val s = path.split("/")
        if (s.isEmpty()) {
            return emptyList()
        }

        require(s[0] == "m") { "No root account m/" }

        return s.drop(1).map {
            val num = it.removeSuffix("'").let { n ->
                requireNotNull(n.toIntOrNull()) { "num part `$n` must be a valid int" }
            }
            val hard = it[it.length - 1] == '\''

            PathElement(num, hard)
        }
    }

}

internal fun List<PathElement>.toString() =
    (listOf("m") + map { it.toString() }).joinToString("/")

const val BIP44_HARDENING_FLAG = 0x80000000.toInt()

data class PathElement(val number: Int, val hardened: Boolean) {
    private fun <R> Boolean.into(t: R, f: R): R = if (this) t else f

    val hardenedNumber = if (hardened) number or BIP44_HARDENING_FLAG else number

    override fun toString(): String = "$number${hardened.into("'", "")}"
}

// test: m/44'/1'/0'/0/0'
// prod: m/44'/505'/0'/0/0


fun String.parseBIP44Path(): List<PathElement> {
    val s = split("/")
    require(s[0] == "m") { "No root account m/" }
    require(s.size <= 6) { "bip44 path too deep" }

    return s.drop(1).map {
        val l = it.takeWhile { c -> c.isDigit() }
        val n = l.toInt()
        val r = it.substring(l.length, it.length)
        val hard = r == "\'" || r.toLowerCase() == "h"
        require(r.isEmpty() || hard) { "Invalid hardening: $r" }
        PathElement(n, hard)
    }
}
