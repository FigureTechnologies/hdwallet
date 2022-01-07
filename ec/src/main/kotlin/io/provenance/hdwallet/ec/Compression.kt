package io.provenance.hdwallet.ec

import java.math.BigInteger

internal const val PUBLIC_KEY_SIZE = 64

fun decompressPublicKey(compressedBytes: ByteArray, curve: Curve): BigInteger {
    val point = curve.decodePoint(compressedBytes)
    val encoded = point.encoded(true)
    return BigInteger(encoded.copyOfRange(1, encoded.size))
}

