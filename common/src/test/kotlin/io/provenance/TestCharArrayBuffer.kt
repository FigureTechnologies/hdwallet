package io.provenance

import io.provenance.chararray.CharArrayBuffer
import org.junit.Assert
import org.junit.Test

class TestCharArrayBuffer {
    @Test
    fun testAppendSeq() {
        fun bufferOf(vararg a: CharSequence) = CharArrayBuffer().apply {
            a.forEach { append(it) }
        }

        Assert.assertArrayEquals("a".toCharArray(), bufferOf("a").toCharArray())
        Assert.assertArrayEquals("ab".toCharArray(), bufferOf("a", "b").toCharArray())
        Assert.assertArrayEquals("abc".toCharArray(), bufferOf("a", "b", "c").toCharArray())

        Assert.assertArrayEquals("あ".toCharArray(), bufferOf("あ").toCharArray())
        Assert.assertArrayEquals("あい".toCharArray(), bufferOf("あ", "い").toCharArray())
        Assert.assertArrayEquals("あいこ".toCharArray(), bufferOf("あ", "い", "こ").toCharArray())
    }

    @Test
    fun testAppendSubseq() {
        CharArrayBuffer().apply { append("test", 1, 2) }.also {
            Assert.assertArrayEquals("e".toCharArray(), it.toCharArray())
        }

        CharArrayBuffer().also {
            try {
                it.append("test", 1, 200)
                Assert.fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }

            try {
                it.append("test", 1000, 2000)
                Assert.fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }

            try {
                it.append("test", 2, 1)
                Assert.fail()
            } catch (e: IndexOutOfBoundsException) {
                // Expected
            }
        }
    }

    @Test
    fun testAppendChar() {
        CharArrayBuffer().also {
            it.append('f')

            Assert.assertArrayEquals("f".toCharArray(), it.toCharArray())
        }

        CharArrayBuffer().also {
            it.append('あ')
            Assert.assertArrayEquals("あ".toCharArray(), it.toCharArray())

            it.append('い')
            Assert.assertArrayEquals("あい".toCharArray(), it.toCharArray())
        }
    }
}
