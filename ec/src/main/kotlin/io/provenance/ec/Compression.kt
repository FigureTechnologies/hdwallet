package io.provenance.ec

import java.math.BigInteger

internal const val PUBLIC_KEY_SIZE = 64

fun decompressPublicKey(compressedBytes: ByteArray, curve: Curve): BigInteger {
    val point = curve.decodePoint(compressedBytes)
    val encoded = point.encoded(true)
    return BigInteger(encoded.copyOfRange(1, encoded.size))
}

fun BigInteger.toBytesPadded(length: Int): ByteArray {
    val result = ByteArray(length)
    val bytes = toByteArray()
    val offset = if (bytes[0].toInt() == 0) 1 else 0
    if (bytes.size - offset > length) {
        throw RuntimeException("Input is too large to put in byte array of size $length")
    }

    val destOffset = length - bytes.size + offset
    return bytes.copyInto(result, destinationOffset = destOffset, startIndex = offset)
}
