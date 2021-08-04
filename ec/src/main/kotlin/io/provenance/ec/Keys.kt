package io.provenance.ec

import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.util.Arrays

fun ByteArray.toBigInteger() = BigInteger(1, this)

/**
 * Returns public uncompressed key point from the given private key.
 */
private fun publicFromPrivate(privateKey: BigInteger, curve: Curve): BigInteger {
    val point = publicPointFromPrivate(privateKey, curve)
    val encoded = point.getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
}

/**
 * Returns public key point from the given private key.
 */
private fun publicPointFromPrivate(privateKey: BigInteger, curve: Curve): ECPoint {
    val postProcessedPrivateKey = if (privateKey.bitLength() > curve.n.bitLength()) {
        privateKey.mod(curve.n)
    } else {
        privateKey
    }
    return FixedPointCombMultiplier().multiply(curve.g, postProcessedPrivateKey)
}

/**
 *
 */
class PrivateKey(val key: BigInteger, val curve: Curve) {
    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PrivateKey =
            PrivateKey(bytes.toBigInteger(), curve)
    }

    fun toPublicKey(): PublicKey = PublicKey(publicFromPrivate(key, curve), curve)
}

fun BigInteger.toPrivateKey(curve: Curve) = PrivateKey(this, curve)

/**
 *
 */
class PublicKey(val key: BigInteger, val curve: Curve) {
    override fun toString() = key.toString()

    fun point(): ECPoint {
        val dest = key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
        dest[0] = 4
        return curve.c.decodePoint(dest)
    }

    fun compressed() = point().getEncoded(true)

    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PublicKey = PublicKey(bytes.toBigInteger(), curve)
    }
}

/**
 *
 */
data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey)

/**
 *
 */
fun PrivateKey.toECKeyPair() = ECKeyPair(this, toPublicKey())
