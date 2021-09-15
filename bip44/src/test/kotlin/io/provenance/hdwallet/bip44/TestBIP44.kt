package io.provenance.hdwallet.bip44

import org.junit.Assert
import org.junit.Test

class TestBIP44 {
    private data class Tv(val path: String, val parsed: List<PathElement> = emptyList(), val valid: Boolean = true)

    private val testVector = { v: Tv ->
        val parsed = runCatching { v.path.parseBIP44Path() }
        Assert.assertEquals("${v.path} valid", v.valid, parsed.isSuccess)
        if (v.valid) {
            val res = parsed.getOrThrow()
            Assert.assertEquals("${v.path} size", v.parsed.size, res.size)
            Assert.assertEquals("${v.path} contents", v.parsed, res)
        }
    }

    private fun pathOf(vararg p: Pair<Int, Boolean>) = p.map { PathElement(it.first, it.second) }

    @Test
    fun testPathParse() {
        val vectors = listOf(
            Tv("", valid = false),
            Tv("u", valid = false),
            Tv("m"),
            Tv("m/", valid = false),
            Tv("m/0", pathOf(0 to false)),
            Tv("m/1H/1H/1H", pathOf(1 to true, 1 to true, 1 to true)),
            Tv("m/1h/1h/1h", pathOf(1 to true, 1 to true, 1 to true)),
            Tv("m/1'/1'/1'", pathOf(1 to true, 1 to true, 1 to true)),
            Tv("m/1/1/1/1/1", pathOf(1 to false, 1 to false, 1 to false, 1 to false, 1 to false)),
            Tv("m/1/1/1/1/1/1", valid = false),
            Tv("m/1m/1m/1m", valid = false),
        )
        vectors.forEach(testVector)
    }
}
