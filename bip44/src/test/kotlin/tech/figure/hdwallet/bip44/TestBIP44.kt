package tech.figure.hdwallet.bip44

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestBIP44 {
    private data class Tv(val path: String, val parsed: List<PathElement> = emptyList(), val valid: Boolean = true)

    private val testVector = { v: Tv ->
        val parsed = runCatching { v.path.parseBIP44Path() }
        assertEquals(v.valid, parsed.isSuccess, "${v.path} valid")
        if (v.valid) {
            val res = parsed.getOrThrow()
            assertEquals(v.parsed.size, res.size, "${v.path} size")
            assertEquals(v.parsed, res, "${v.path} contents")
        }
    }

    private fun pathOf(vararg p: Pair<Int, Boolean>): List<PathElement> =
        p.mapIndexed { position, (n, hard) -> buildPathElement(position, n, hard) }

    @Test
    fun `check basic path parsing`() {
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

    @Test
    fun `parsing a path that's too long should raise an exception`() {
        assertThrows<IllegalArgumentException> {
            "m/1/1/1/1/1/1".parseBIP44Path()
        }
    }

    @Test
    fun `parse a path string into a DerivationPath instance`() {
        val path = DerivationPath.from("m/44'/505'/0'/0/0'")
        assertEquals("m/44'/505'/0'/0/0'", path.toString())
        assertEquals(path.purpose, PathElement.Purpose(44, hardened = true))
        assertEquals(path.coinType, PathElement.CoinType(505, hardened = true))
        assertEquals(path.account, PathElement.Account(0, hardened = true))
        assertEquals(path.change, PathElement.Change(0, hardened = false))
        assertEquals(path.index, PathElement.Index(0, hardened = true))
    }

    @Test
    fun `a path builder without modification should return the origin path`() {
        val path = DerivationPath.from("m/44'/505'/0'/0/0'")
        val newPath = path.toBuilder().build()
        assertEquals(path, newPath)
    }

    @Test
    fun `create a new path with an updated account from an existing one`() {
        val path = DerivationPath.from("m/44'/505'/0'/0/0'")
        val newAccount = 789
        val newPath = path.toBuilder().account(newAccount, harden = false).build()
        assertEquals(newPath.account, PathElement.Account(newAccount, hardened = false))
        assertEquals("m/44'/505'/$newAccount/0/0'", newPath.toString())
    }

    @Test
    fun `create a new path with an updated index from an existing one`() {
        val path = DerivationPath.from("m/44'/505'/0'/0/0'")
        val newIndex = 789
        val newPath = path.toBuilder().index(newIndex, harden = false).build()
        assertEquals(newPath.index, PathElement.Index(newIndex, hardened = false))
        assertEquals("m/44'/505'/0'/0/$newIndex", newPath.toString())
    }

    @Test
    fun `create a new path with from an existing one`() {
        val path = DerivationPath.from("m/44'/505'/0'/0/0'")
        val newAccount = 987
        val newIndex = 789
        val newPath = path.toBuilder()
            .account(newAccount, harden = false)
            .index(newIndex, harden = true)
            .build()
        assertEquals(newPath.account, PathElement.Account(newAccount, hardened = false))
        assertEquals(newPath.index, PathElement.Index(newIndex, hardened = true))
        assertEquals("m/44'/505'/$newAccount/0/${newIndex}'", newPath.toString())
    }
}
