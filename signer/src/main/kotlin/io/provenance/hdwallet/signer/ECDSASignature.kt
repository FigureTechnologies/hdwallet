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
         * Returns an [ECDSASignature] where the 64 byte array is divided into `r || s` with each being a 32 byte
         * big endian integer.
         *
         * @param bytes The byte array to interpret as a BTC-encoded signature.
         * @param curveParams The EC curve to use when performing the decoding.
         * @return The decoded ECDSA signature, [ECDSASignature].
         */
        private fun decodeAsBTC(bytes: ByteArray, curveParams: Curve = DEFAULT_CURVE): ECDSASignature {
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

        /**
         * Decode the given BTC-encoded signature as a ECDSA signature.
         *
         * @param signature The BTC-encoded signature.
         * @param curveParams The EC curve to use when performing the decoding.
         * @return The decoded ECDSA signature, [ECDSASignature].
         */
        fun decode(signature: BTCSignature, curveParams: Curve = DEFAULT_CURVE): ECDSASignature =
            decodeAsBTC(bytes = signature.toByteArray(), curveParams = curveParams)

        /**
         * Decode the given ASN.1-encoded signature as a ECDSA signature.
         *
         * @param signature The ASN.1-encoded signature.
         * @param curveParams The EC curve to use when performing the decoding.
         * @return The decoded ECDSA signature, [ECDSASignature].
         */
        fun decode(signature: ASN1Signature, curveParams: Curve = DEFAULT_CURVE): ECDSASignature =
            ASN1.decode(signature).run {
                ECDSASignature(r, s, curveParams)
            }
    }

    /**
     * [encodeAsBTC] returns the ECDSA signature as a ByteArray of `r || s`,
     * where both `r` and `s` are encoded into 32 byte big endian integers.
     */
    fun encodeAsBTC(): BTCSignature = BTC.encode(r, s, curve)

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
    fun encodeAsASN1DER(): ASN1Signature = ASN1.encode(r, s)
}
