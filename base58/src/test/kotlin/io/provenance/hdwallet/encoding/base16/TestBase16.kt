package io.provenance.hdwallet.encoding.base16

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalUnsignedTypes
class TestBase16 {

    data class TC(val orig: ByteArray, val enc: String, val pass: Boolean)

    fun testVector() = { v: TC ->
        assertEquals(v.orig.base16Encode().contentEquals(v.enc), v.pass)
        assertEquals(v.enc.base16Decode().contentEquals(v.orig), v.pass)
    }

    @Test
    fun testBase16Codec() {
        val vectors = listOf(
            TC(
                "test".toByteArray(),
                "74657374",
                true,
            ),
            TC(
                "the quick brown fox".toByteArray(),
                "74686520717569636b2062726f776e20666f78",
                true,
            ),
            TC(
                ubyteArrayOf(
                    0x00u,
                    0x10u,
                    0x20u,
                    0x30u,
                    0x40u,
                    0x50u,
                    0x60u,
                    0x70u,
                    0x80u,
                    0x90u,
                    0xA0u,
                    0xB0u,
                    0xC0u,
                    0xD0u,
                    0xE0u,
                    0xF0u
                ).toByteArray(),
                "00102030405060708090a0b0c0d0e0f0",
                true,
            ),
        )

        vectors.map(testVector())
    }
}
