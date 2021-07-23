package io.provenance.ec

import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.util.Arrays

fun ByteArray.toBigInteger() = BigInteger(1, this)

/**
 * Returns public uncompressed key point from the given private key.
 */
private fun publicFromPrivate(privateKey: BigInteger): BigInteger {
    val point = publicPointFromPrivate(privateKey)
    val encoded = point.getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
}

/**
 * Returns public key point from the given private key.
 */
private fun publicPointFromPrivate(privateKey: BigInteger): ECPoint {
    val postProcessedPrivateKey = if (privateKey.bitLength() > curveParams.n.bitLength()) {
        privateKey.mod(domainParams.n)
    } else {
        privateKey
    }
    return FixedPointCombMultiplier().multiply(domainParams.g, postProcessedPrivateKey)
}

/**
 *
 */
class PrivateKey(val key: BigInteger) {
    companion object {
        fun fromBytes(bytes: ByteArray): PrivateKey = PrivateKey(bytes.toBigInteger())
    }

    fun toPublicKey(): PublicKey = PublicKey(publicFromPrivate(key))
}

/**
 *
 */
class PublicKey(val key: BigInteger) {
    override fun toString() = key.toString()

    fun compressed(): ByteArray {
        val dest = key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
        dest[0] = 4
        val p = curveParams.curve.decodePoint(dest)
        return p.getEncoded(true)
    }

    companion object {
        fun fromBytes(bytes: ByteArray): PublicKey = PublicKey(bytes.toBigInteger())
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
