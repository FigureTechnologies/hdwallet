package io.provenance.hdwallet.common

import io.provenance.hdwallet.common.chararray.CharArrayBuffer
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class TestCharArrayBuffer {
    @Test
    fun testAppendSeq() {
        fun bufferOf(vararg a: CharSequence) = CharArrayBuffer().apply {
            a.forEach { append(it) }
        }

        assertArrayEquals("a".toCharArray(), bufferOf("a").toCharArray())
        assertArrayEquals("ab".toCharArray(), bufferOf("a", "b").toCharArray())
        assertArrayEquals("abc".toCharArray(), bufferOf("a", "b", "c").toCharArray())

        assertArrayEquals("あ".toCharArray(), bufferOf("あ").toCharArray())
        assertArrayEquals("あい".toCharArray(), bufferOf("あ", "い").toCharArray())
        assertArrayEquals("あいこ".toCharArray(), bufferOf("あ", "い", "こ").toCharArray())
    }

    @Test
    fun testAppendSubseq() {
        CharArrayBuffer().apply { append("test", 1, 2) }.also {
            assertArrayEquals("e".toCharArray(), it.toCharArray())
        }

        CharArrayBuffer().also {
            try {
                it.append("test", 1, 200)
                fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }

            try {
                it.append("test", 1000, 2000)
                fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }

            try {
                it.append("test", 2, 1)
                fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }
        }
    }

    @Test
    fun testAppendChar() {
        CharArrayBuffer().also {
            it.append('f')
            assertArrayEquals("f".toCharArray(), it.toCharArray())
        }

        CharArrayBuffer().also {
            it.append('あ')
            assertArrayEquals("あ".toCharArray(), it.toCharArray())

            it.append('い')
            assertArrayEquals("あい".toCharArray(), it.toCharArray())
        }
    }
}
