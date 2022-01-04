package io.provenance.hdwallet.common

import io.provenance.hdwallet.common.chararray.split
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestCharArrays {
    @Test
    fun testSplit() {
        val s = "thing1 thing2".toCharArray().split(' ')
        assertArrayEquals(s[0], "thing1".toCharArray())
        assertArrayEquals(s[1], "thing2".toCharArray())

        val b = "thing1,thing2".toCharArray().split(',')
        assertArrayEquals(b[0], "thing1".toCharArray())
        assertArrayEquals(b[1], "thing2".toCharArray())

        val c = "thing1thing2".toCharArray().split(' ')
        assertArrayEquals(c[0], "thing1thing2".toCharArray())

        val d = "".toCharArray().split(' ')
        assertEquals(1, c.size)
    }

    @Test
    fun testIndexOf() {
        val i1 = "aoeu1aoeu2aoeu3".toCharArray()
        assertEquals(4, i1.indexOf('1'))
        assertEquals(9, i1.indexOf('2'))
        assertEquals(14, i1.indexOf('3'))
        assertEquals(-1, i1.indexOf('x'))
        assertEquals(-1, i1.indexOf('\b'))

        val i2 = "".toCharArray()
        assertEquals(-1, i2.indexOf(' '))
    }
}
