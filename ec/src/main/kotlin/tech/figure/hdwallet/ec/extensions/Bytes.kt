package tech.figure.hdwallet.ec.extensions

import java.math.BigInteger
import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.decompressPublicKey

/**
 * Pack a byte array into an unsigned [BigInteger].
 *
 * @return [BigInteger]
 */
@Deprecated("Potentially wrong usage", replaceWith = ReplaceWith("ByteArray.packIntoBigInteger"))
fun ByteArray.toBigInteger(): BigInteger = BigInteger(1, this)

/**
 * Convert a byte array to [BigInteger], using the supplied curve.
 *
 * See [decompressPublicKey] for details.
 *
 * - If [legacy] is false (the default), the returned [BigInteger] will be 65 bytes
 * - If [legacy] is true (the default), the returned [BigInteger] will be 64 bytes
 *
 * @param curve The EC curve to use when interpreting the contents of this byte array as a coordinate.
 * @param legacy Toggle legacy encoding behavior. See the note in the description for details.
 * @return [BigInteger]
 */
fun ByteArray.toBigInteger(curve: Curve, legacy: Boolean = false): BigInteger = decompressPublicKey(this, curve, legacy)

/**
 * Pack a byte array into an unsigned [BigInteger].
 *
 * @return [BigInteger]
 */
fun ByteArray.packIntoBigInteger(): BigInteger = BigInteger(1, this)
