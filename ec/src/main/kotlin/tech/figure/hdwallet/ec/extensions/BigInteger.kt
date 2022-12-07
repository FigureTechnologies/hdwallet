package tech.figure.hdwallet.ec.extensions

import java.math.BigInteger
import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.PrivateKey

/**
 * Unpack a [BigInteger] into a padded [ByteArray].
 *
 * @param length The final length of the returned byte array.
 * @return The padded [ByteArray].
 */
fun BigInteger.toBytesPadded(length: Int): ByteArray {
    val result = ByteArray(length)
    val bytes = toByteArray()
    val offset = if (bytes[0].toInt() == 0) 1 else 0
    if (bytes.size - offset > length) {
         error("Input is too large to put in byte array of size $length")
    }

    val destOffset = length - bytes.size + offset
    return bytes.copyInto(result, destinationOffset = destOffset, startIndex = offset)
}

/**
 * Convert a [BigInteger] to an EC [PrivateKey] using the supplied curve
 *
 * @param curve The curve to use when creating the key. See [tech.figure.hdwallet.ec.secp256k1Curve]
 */
fun BigInteger.toPrivateKey(curve: Curve): PrivateKey = PrivateKey(this, curve)
