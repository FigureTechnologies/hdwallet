package io.provenance.ec

import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import java.math.BigInteger

fun X9ECParameters.toDomainParams() = ECDomainParameters(curve, g, n, h)

internal const val PUBLIC_KEY_SIZE = 64

fun decompressPublicKey(compressedBytes: ByteArray, curveParams: X9ECParameters = secp256k1CurveParams): BigInteger {
    val point = curveParams.curve.decodePoint(compressedBytes)
    val encoded = point.getEncoded(true)
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
