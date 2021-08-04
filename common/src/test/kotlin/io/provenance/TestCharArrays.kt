package io.provenance

import io.provenance.chararray.split
import org.junit.Assert
import org.junit.Test

class TestCharArrays {
    @Test
    fun testSplit() {
        val s = "thing1 thing2".toCharArray().split(' ')
        Assert.assertArrayEquals(s[0], "thing1".toCharArray())
        Assert.assertArrayEquals(s[1], "thing2".toCharArray())

        val b = "thing1,thing2".toCharArray().split(',')
        Assert.assertArrayEquals(b[0], "thing1".toCharArray())
        Assert.assertArrayEquals(b[1], "thing2".toCharArray())

        val c = "thing1thing2".toCharArray().split(' ')
        Assert.assertArrayEquals(c[0], "thing1thing2".toCharArray())

        val d = "".toCharArray().split(' ')
        Assert.assertEquals(1, c.size)
    }

    @Test
    fun testIndexOf() {
        val i1 = "aoeu1aoeu2aoeu3".toCharArray()
        Assert.assertEquals(4, i1.indexOf('1'))
        Assert.assertEquals(9, i1.indexOf('2'))
        Assert.assertEquals(14, i1.indexOf('3'))
        Assert.assertEquals(-1, i1.indexOf('x'))
        Assert.assertEquals(-1, i1.indexOf('\b'))

        val i2 = "".toCharArray()
        Assert.assertEquals(-1, i2.indexOf(' '))
    }
}
