package io.provenance.hdwallet.signer

import io.provenance.hdwallet.ec.DEFAULT_CURVE
import io.provenance.hdwallet.ec.Curve
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.Signature as JavaSignature
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequenceGenerator

data class ECDSASignature(val r: BigInteger, val s: BigInteger, val curve: Curve = DEFAULT_CURVE) {

    private val halfCurveOrder = curve.n.shiftRight(1)

    companion object {
        /**
         * decodeAsBTC returns an ECDSASignature where the 64 byte array is divided
         * into r || s with each being a 32 byte big endian integer.
         */
        fun decodeAsBTC(bytes: ByteArray, curveParams: Curve = DEFAULT_CURVE): ECDSASignature {
            val halfCurveOrder = curveParams.n.shiftRight(1)

            require(bytes.size == 64) { "malformed BTC encoded signature, expected 64 bytes" }

            val ecdsa = ECDSASignature(
                BigInteger(1, bytes.dropLast(32).toByteArray()),
                BigInteger(1, bytes.takeLast(32).toByteArray())
            )

            require(ecdsa.r < curveParams.n) { "signature R must be less than curve.N" }
            require(ecdsa.s <= halfCurveOrder) { "signature S must be less than (curve.N / 2)" }

            return ecdsa
        }
    }

    /**
     * [encodeAsBTC] returns the ECDSA signature as a ByteArray of `r || s`,
     * where both `r` and `s` are encoded into 32 byte big endian integers.
     */
    fun encodeAsBTC(): ByteArray {
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
        var sigS = this.s
        if (sigS > halfCurveOrder) {
            sigS = curve.n.subtract(sigS)
        }

        val sBytes = sigS.getUnsignedBytes()
        val rBytes = this.r.getUnsignedBytes()

        require(rBytes.size <= 32) { "cannot encode r into BTC Format, size overflow (${rBytes.size} > 32)" }
        require(sBytes.size <= 32) { "cannot encode s into BTC Format, size overflow (${sBytes.size} > 32)" }

        val signature = ByteArray(64)
        // 0 pad the byte arrays from the left if they aren't big enough.
        System.arraycopy(rBytes, 0, signature, 32 - rBytes.size, rBytes.size)
        System.arraycopy(sBytes, 0, signature, 64 - sBytes.size, sBytes.size)
        return signature
    }

    /**
     * Encodes the `(r,s)` pair as an ASN.1 DER byte array, suitable for use with the Java [JavaSignature] API
     * for signature verification.
     *
     * Example:
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
     * @return `(r,s)` pair encoded as a ASN.1 DER array of bytes.
     */
    fun encodeAsASN1DER(): ByteArray =
        ByteArrayOutputStream().use {
            DERSequenceGenerator(it).apply {
                addObject(ASN1Integer(r.toByteArray()))
                addObject(ASN1Integer(s.toByteArray()))
                close()
            }
            it.toByteArray()
        }
}
