package tech.figure.hdwallet.ec

import tech.figure.hdwallet.bech32.Address
import tech.figure.hdwallet.bech32.toBech32
import tech.figure.hdwallet.common.hashing.sha256hash160
import tech.figure.hdwallet.ec.extensions.packIntoBigInteger
import tech.figure.hdwallet.ec.extensions.toBytesPadded
import java.math.BigInteger
import java.util.Base64

/**
 * Elliptic curve (EC) private key.
 *
 * @property key The private key `d` value.
 * @property curve The underlying elliptic curve.
 */
class PrivateKey(val key: BigInteger, val curve: Curve) {

    companion object {
        fun fromBytes(bytes: ByteArray, curve: Curve): PrivateKey = PrivateKey(bytes.packIntoBigInteger(), curve)
    }

    fun toPublicKey(): PublicKey = PublicKey(curve.publicFromPrivate(key), curve)

    fun toECKeyPair(): ECKeyPair = ECKeyPair(this, toPublicKey())
}

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

    fun compressed(): ByteArray = point().encoded(true)

    companion object {
        /**
         * Create a [PublicKey] from an array of bytes using the default curve, [secp256k1Curve].
         *
         * @param bytes The byte array to decode.
         */
        fun fromBytes(bytes: ByteArray): PublicKey = fromBytes(bytes, DEFAULT_CURVE)

        /**
         * Create a [PublicKey] from a base64 encoded string using the default curve, [secp256k1Curve].
         *
         * @param bytes The base-64 encoded bytes to decode.
         */
        fun fromString(encoded: String): PublicKey = fromBytes(Base64.getDecoder().decode(encoded), DEFAULT_CURVE)

        /**
         * Create a [PublicKey] from an array of bytes using the provided [curve] with byte decompression.
         *
         * See [decompressPublicKey].
         *
         * @param bytes The raw byte array to decode.
         * @param curve The EC curve to use when decoding [bytes] into a coordinate.
         */
        fun fromBytes(bytes: ByteArray, curve: Curve): PublicKey = PublicKey(decompressPublicKey(bytes, curve), curve)
    }
}

/**
 * An elliptic curve <public, private> key pair.
 *
 * @property privateKey The private key.
 * @property publicKey The public key.
 */
data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey) {
    fun toPair(): Pair<PrivateKey, PublicKey> = Pair(privateKey, publicKey)
}
