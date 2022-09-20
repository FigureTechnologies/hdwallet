package tech.figure.hdwallet.ec.extensions

import java.math.BigInteger

/**
 * Pack a byte array into an unsigned [BigInteger].
 *
 * @return [BigInteger]
 */
fun ByteArray.toBigInteger() = BigInteger(1, this)

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
        throw RuntimeException("Input is too large to put in byte array of size $length")
    }

    val destOffset = length - bytes.size + offset
    return bytes.copyInto(result, destinationOffset = destOffset, startIndex = offset)
}
