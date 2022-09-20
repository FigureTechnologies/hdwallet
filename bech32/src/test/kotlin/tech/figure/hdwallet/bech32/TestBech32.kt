package tech.figure.hdwallet.bech32

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// https://en.bitcoin.it/wiki/BIP_0173#Test_vectors
class TestBech32 {
    @Test
    fun testBech32() {
        data class B32(val address: String, val valid: Boolean)

        val vectors = mapOf(
            // Valid
            "" to B32("A12UEL5L", true),
            "" to B32("a12uel5l", true),
            "" to B32(
                "an83characterlonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1tt5tgs",
                true
            ),
            "" to B32("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw", true),
            "" to B32(
                "11qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqc8247j",
                true
            ),
            "" to B32("split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w", true),
            "" to B32("?1ezyfcl", true),

            // Invalid
            "hrp out of range" to B32(0x20.toChar() + "1nwldj5", false),
            "hrp out of range" to B32(0x7f.toChar() + "1axkwrx", false),
            "hrp out of range" to B32(0x80.toChar() + "1eym55h", false),
            "overall max length exceeded" to B32(
                "an84characterslonghumanreadablepartthatcontainsthenumber1andtheexcludedcharactersbio1569pvx",
                false
            ),
            "No separator character" to B32("pzry9x0s0muk", false),
            "Empty HRP" to B32("1pzry9x0s0muk", false),
            "Invalid data character" to B32("x1b4n0q5v", false),

            "Too short checksum" to B32("li1dgmt3", false),
            "Invalid character in checksum" to B32("de1lg7wt" + 0xFF, false),

            "checksum calculated with uppercase form of HRP" to B32("A1G7SGD8", false),
            "empty HRP" to B32("10a06t8", false),
            "empty HRP" to B32("1qzzfhee", false),
        )

        for (vector in vectors) {
            val value = Result.runCatching { Bech32.decode(vector.value.address) }
            assertEquals(value.isSuccess, vector.value.valid)
        }
    }
}
