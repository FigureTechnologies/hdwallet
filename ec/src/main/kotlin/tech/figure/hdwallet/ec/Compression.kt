package tech.figure.hdwallet.ec

import tech.figure.hdwallet.ec.extensions.packIntoBigInteger
import java.math.BigInteger
import java.nio.ByteBuffer

internal const val PUBLIC_KEY_SIZE = 64

/**
 * In Bitcoin, public keys are either compressed or uncompressed. Compressed public keys are 33 bytes,
 * consisting of a prefix either 0x02 or 0x03, and a 256-bit integer called x.
 * The older uncompressed keys are 65 bytes, consisting of constant prefix (0x04),
 * followed by two 256-bit integers called x and y (2 * 32 bytes).
 *
 * @param compressedBytes The compressed public key bytes to decompress.
 * @param curve The curve to use for decoding the public key point.
 * @return If [legacy] is true, The public key will be a packed [BigInteger] 65 bytes long according to the legacy
 * format, where the first byte is 0x04, followed by a 32-byte x point coordinate, and a 32-byte y point coordinate.
 * If [legacy] is false, the returned [BigInteger] will be 64 bytes, consisting of a 32-byte x point coordinate,
 * followed by a 32-byte y point coordinate.
 */
fun decompressPublicKey(compressedBytes: ByteArray, curve: Curve, legacy: Boolean = false): BigInteger {
    val point = curve.decodePoint(compressedBytes)
    val x = point.ecPoint.xCoord.encoded
    val y = point.ecPoint.yCoord.encoded
    val bb = ByteBuffer.allocate(PUBLIC_KEY_SIZE + if (legacy) 1 else 0)
    if (legacy) {
        bb.put(0x04)
    }
    bb.put(x)
    bb.put(y)
    return bb.array().packIntoBigInteger()
}
