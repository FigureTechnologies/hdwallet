package io.provenance.hdwallet.ec

import io.provenance.hdwallet.bech32.Address
import io.provenance.hdwallet.bech32.toBech32
import io.provenance.hdwallet.common.hashing.sha256hash160
import io.provenance.hdwallet.ec.extensions.toBigInteger
import io.provenance.hdwallet.ec.extensions.toBytesPadded
import java.math.BigInteger

/**
 * Elliptic curve (EC) private key.
 *
 * @property key The private key `d` value.
 * @property curve The underlying elliptic curve.
 */
class PrivateKey(val key: BigInteger, val curve: Curve) {

    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PrivateKey =
            PrivateKey(bytes.toBigInteger(), curve)
    }

    fun toPublicKey(): PublicKey = PublicKey(curve.publicFromPrivate(key), curve)

    fun toECKeyPair() = ECKeyPair(this, toPublicKey())
}

fun BigInteger.toPrivateKey(curve: Curve) = PrivateKey(this, curve)

/**
 * Elliptic curve (EC) public key.
 *
 * @property key The public key `q` value.
 * @property curve The underlying elliptic curve.
 */
class PublicKey(val key: BigInteger, val curve: Curve) {

    override fun toString() = key.toString()

    fun point(): CurvePoint {
        val dest = key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
        dest[0] = 4
        return curve.decodePoint(dest)
    }

    fun address(hrp: String): Address = compressed().sha256hash160().toBech32(hrp).address

    fun compressed() = point().encoded(true)

    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PublicKey = PublicKey(bytes.toBigInteger(), curve)
    }
}

/**
 * An elliptic curve <public, private> key pair.
 *
 * @property privateKey The private key.
 * @property publicKey The public key.
 */
data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey)
