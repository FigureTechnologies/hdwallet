package io.provenance.ec

import java.math.BigInteger

fun ByteArray.toBigInteger() = BigInteger(1, this)

/**
 *
 */
class PrivateKey(val key: BigInteger, val curve: Curve) {
    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PrivateKey =
            PrivateKey(bytes.toBigInteger(), curve)
    }

    fun toPublicKey(): PublicKey = PublicKey(curve.publicFromPrivate(key), curve)
}

fun BigInteger.toPrivateKey(curve: Curve) = PrivateKey(this, curve)

/**
 *
 */
class PublicKey(val key: BigInteger, val curve: Curve) {
    override fun toString() = key.toString()

    fun point(): CurvePoint {
        val dest = key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
        dest[0] = 4
        return curve.decodePoint(dest)
    }

    fun compressed() = point().encoded(true)

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
