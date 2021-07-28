package io.provenance.base16

object Base16 {
    private val b16Alphabet = "0123456789abcdef".toByteArray()

    fun decode(input: String): ByteArray {
        val s =
            if (input.length % 2 != 0) input.padStart(input.length + 1, '0')
            else input

        return ByteArray(s.length / 2).also {
            for (i in s.indices.step(2)) {
                it[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            }
        }
    }

    fun encode(input: ByteArray): String = ByteArray(input.size * 2).let {
        for (i in input.indices) {
            val v = input[i].toInt() and 0xff
            it[i * 2] = b16Alphabet[v ushr 4]
            it[i * 2 + 1] = b16Alphabet[v and 0x0f]
        }
        it.toString(Charsets.UTF_8)
    }
}

fun String.base16Decode() = Base16.decode(this)
fun ByteArray.base16Encode() = Base16.encode(this)
