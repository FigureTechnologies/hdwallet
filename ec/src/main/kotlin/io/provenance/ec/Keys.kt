package io.provenance.ec

import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.util.Arrays

fun ByteArray.toBigInteger() = BigInteger(1, this)

/**
 * Returns public uncompressed key point from the given private key.
 */
private fun publicFromPrivate(privateKey: BigInteger, curveParams: X9ECParameters): BigInteger {
    val point = publicPointFromPrivate(privateKey, curveParams)
    val encoded = point.getEncoded(false)
    return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
}

/**
 * Returns public key point from the given private key.
 */
private fun publicPointFromPrivate(privateKey: BigInteger, curveParams: X9ECParameters): ECPoint {
    val postProcessedPrivateKey = if (privateKey.bitLength() > curveParams.n.bitLength()) {
        privateKey.mod(curveParams.n)
    } else {
        privateKey
    }
    return FixedPointCombMultiplier().multiply(curveParams.g, postProcessedPrivateKey)
}

/**
 *
 */
class PrivateKey(val key: BigInteger, val curveParams: X9ECParameters) {
    companion object {
        fun fromBytes(bytes: ByteArray, curveParams: X9ECParameters): PrivateKey =
            PrivateKey(bytes.toBigInteger(), curveParams)
    }

    fun toPublicKey(): PublicKey = PublicKey(publicFromPrivate(key, curveParams), curveParams)
}

fun BigInteger.toPrivateKey(curveParams: X9ECParameters) = PrivateKey(this, curveParams)

/**
 *
 */
class PublicKey(val key: BigInteger, val curveParams: X9ECParameters) {
    override fun toString() = key.toString()

    fun point() = curveParams.curve.decodePoint(key.toByteArray())

    fun compressed(): ByteArray {
        val dest = key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
        dest[0] = 4
        val p = curveParams.curve.decodePoint(dest)
        return p.getEncoded(true)
    }

    companion object {
        fun fromBytes(bytes: ByteArray, curveParams: X9ECParameters): PublicKey = PublicKey(bytes.toBigInteger(), curveParams)
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
