package tech.figure.hdwallet.signer

import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.DEFAULT_CURVE
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import org.bouncycastle.asn1.ASN1InputStream
import java.security.Signature as JavaSignature
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Primitive
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequenceGenerator

@JvmInline
value class BTCSignature private constructor(private val signatureBytes: ByteArray) {
    companion object {
        fun fromByteArray(bytes: ByteArray): BTCSignature = BTCSignature(bytes)
    }

    fun toByteArray(): ByteArray = signatureBytes
    fun toIntegerPair(curve: Curve = DEFAULT_CURVE): BigIntegerPair =
        BTC.decode(bytes = signatureBytes, curveParams = curve)
}

@JvmInline
value class ASN1Signature private constructor(private val signatureBytes: ByteArray) {
    companion object {
        fun fromByteArray(bytes: ByteArray): ASN1Signature = ASN1Signature(bytes)
    }

    fun toByteArray(): ByteArray = signatureBytes
    fun toIntegerPair(): BigIntegerPair = ASN1.decode(this)
}

data class BigIntegerPair(val r: BigInteger, val s: BigInteger) {
    fun asPair(): Pair<BigInteger, BigInteger> = Pair(r, s)
}

/**
 * Bitcoin-specific signature format operations.
 */
object BTC {
    /**
     * Returns the ECDSA signature as a ByteArray of `r || s`, * where both `r` and `s` are encoded into
     * 32 byte big endian integers.
     */
    fun encode(r: BigInteger, s: BigInteger, curve: Curve): BTCSignature {

        val halfCurveOrder = curve.n.shiftRight(1)

        /**
         * Returns the bytes from a BigInteger as an unsigned version by truncating a byte if needed.
         *
         * @return `r || s` encoded as an array of bytes.
         */
        fun BigInteger.getUnsignedBytes(): ByteArray {
            val bytes = this.toByteArray()
            if (bytes[0] == 0x0.toByte()) {
                return bytes.drop(1).toByteArray()
            }
            return bytes
        }

        // Canonicalize - In order to remove malleability,
        // we set s = curve_order - s, if s is greater than curve.Order() / 2.
        var sigS = s
        if (sigS > halfCurveOrder) {
            sigS = curve.n.subtract(sigS)
        }

        val sBytes = sigS.getUnsignedBytes()
        require(sBytes.size <= 32) { "cannot encode s into BTC Format, size overflow (${sBytes.size} > 32)" }

        val rBytes = r.getUnsignedBytes()
        require(rBytes.size <= 32) { "cannot encode r into BTC Format, size overflow (${rBytes.size} > 32)" }

        val signature = ByteArray(64)
        // 0 pad the byte arrays from the left if they aren't big enough.
        System.arraycopy(rBytes, 0, signature, 32 - rBytes.size, rBytes.size)
        System.arraycopy(sBytes, 0, signature, 64 - sBytes.size, sBytes.size)

        return BTCSignature.fromByteArray(signature)
    }

    /**
     * Returns an [ECDSASignature] where the 64 byte array is divided into `r || s` with each being a 32 byte
     * big endian integer.
     *
     * @param bytes The byte array to interpret as a BTC-encoded signature.
     * @param curveParams The EC curve to use when performing the decoding.
     * @return The decoded ECDSA signature, [ECDSASignature].
     */
    fun decode(bytes: ByteArray, curveParams: Curve = DEFAULT_CURVE): BigIntegerPair {
        require(bytes.size == 64) { "malformed BTC encoded signature, expected 64 bytes" }
        val halfCurveOrder = curveParams.n.shiftRight(1)

        val r = BigInteger(1, bytes.dropLast(32).toByteArray())
        require(r < curveParams.n) { "signature R must be less than curve.N" }

        val s = BigInteger(1, bytes.takeLast(32).toByteArray())
        require(s <= halfCurveOrder) { "signature S must be less than (curve.N / 2)" }

        return BigIntegerPair(r, s)
    }
}

/**
 * ASN.1-specific signature format operations.
 */
object ASN1 {
    /**
     * Encodes the `(r,s)` pair as an ASN.1 DER byte array, suitable for use with the Java [JavaSignature] API
     * for signature verification.
     *
     * As an ASN.1 structure, the ECDSA signature is defined as
     *
     * ```
     * ECDSASignature ::= SEQUENCE {
     *     r   INTEGER,
     *     s   INTEGER
     * }
     * ```
     *
     * A usage example:
     *
     * ```
     * val privateKey: PrivateKey = ...
     * val payload: ByteArray = ... // raw array of bytes
     * val payloadHash: ByteArray = payloadHash.sha256()
     * val signature: EDCSASignature = BCECSigner().sign(privateKey, payloadHash)
     * val ok: Boolean = Signature.getInstance("SHA256withECDSA").apply {
     *     initVerify(publicKey)
     *     update(payload)
     *     s.verify(signature.encodeAsASN1DER())
     * }
     * assert(ok)
     * ```
     *
     * @param r The `r` [BigInteger] to encode.
     * @param s The `s` [BigInteger] to encode.
     * @return `(r,s)` pair encoded as a ASN.1 DER array of bytes.
     */
    fun encode(r: BigInteger, s: BigInteger): ASN1Signature =
        ByteArrayOutputStream().use {
            DERSequenceGenerator(it).apply {
                addObject(ASN1Integer(r.toByteArray()))
                addObject(ASN1Integer(s.toByteArray()))
                close()
            }
            ASN1Signature.fromByteArray(it.toByteArray())
        }

    /**
     * Decode an ASN.1 signature into an `(r,s)` integer pair.
     *
     * @param bytes The signature bytes to decode.
     * @return The decoded `(r,s)` integer pair as a [BigIntegerPair].
     */
    fun decode(bytes: ByteArray): BigIntegerPair =
        ASN1InputStream(bytes).use {
            var obj: ASN1Primitive? = null
            val parts: MutableList<BigInteger> = mutableListOf()
            do {
                obj = it.readObject()
                obj?.run {
                    when (obj) {
                        is ASN1Sequence -> {
                            obj.objects.asSequence().forEachIndexed { i, e ->
                                if (i >= 2) {
                                    error("r and s already read from sequence")
                                }
                                when (e) {
                                    is ASN1Integer -> parts.add(e.value)
                                    else -> error("unexpected object type")
                                }
                            }
                        }
                        else -> error("unexpected object type")
                    }
                }
            } while (obj != null)

            BigIntegerPair(parts[0], parts[1])
        }

    /**
     * Decode an ASN.1 signature into an `(r,s)` integer pair.
     *
     * @param signature The signature to decode.
     * @return The decoded `(r,s)` integer pair as a [BigIntegerPair].
     */
    fun decode(signature: ASN1Signature): BigIntegerPair = decode(signature.toByteArray())
}

//fun ByteArray.decodeAsDER(): ECDSASignature {
//    // 0x30 + <1-byte> + 0x02 + 0x01 + <byte> + 0x2 + 0x01 + <byte> = 8
//    val MIN_SIG_LENGTH = 8
//    require(this.size >= MIN_SIG_LENGTH) { "malformed signature: too short" }
//    require(this[0] == 0x30.toByte()) { "malformed signature: does not start with expected header byte" }
//
//    val sigLen = this[1].toInt()
//    require(!(sigLen + 2 > this.size || sigLen + 2 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//    require(this[2] == 0x02.toByte()) { "malformed signature: missing first byte marker for R" }
//
//    val rByteLen = this[3].toInt()
//    require(!(rByteLen + 7 > this.size || rByteLen + 7 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//
//    val rBytes = ByteArray(rByteLen)
//    System.arraycopy(this, 4, rBytes, 0, rByteLen)
//
//    // offset of S bytes is length of R bytes plus header, length, marker bytes
//    val sOffset = (rByteLen + 4)
//
//    val sByteLen = this[sOffset + 1].toInt()
//    require(!(sOffset + sByteLen + 2 > this.size || sByteLen + 2 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//    require(this[sOffset] == 0x02.toByte()) { "malformed signature: missing first byte marker for S" }
//
//    val sBytes = ByteArray(sByteLen)
//    System.arraycopy(this, sOffset + 2, sBytes, 0, sByteLen)
//
//    require(rBytes[0].isNarrow()) { "R is not a canonical value, could be negative" }
//    require((rBytes[0] != ZERO) || (rBytes.size <= 1 || rBytes[1].isWide())) { "excessive padding" }
//
//    require(sBytes[0].isNarrow()) { "S is not a canonical value, could be negative" }
//    require((sBytes[0] != ZERO) || (sBytes.size <= 1 || sBytes[1].isWide())) { "excessive padding" }
//
//    val ecdsa = ECDSASignature(BigInteger(rBytes), BigInteger(sBytes))
//
//    require(ecdsa.r.signum() == 1) { "Signature R value must be positive" }
//    require(ecdsa.s.signum() == 1) { "Signature S value must be positive" }
//
//    require(ecdsa.r < CURVE.n) { "Signature R value must be less than curve N" }
//    require(ecdsa.s < CURVE.n) { "Signature S value must be less than curve N" }
//
//    return ecdsa
//}
