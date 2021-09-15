package io.provenance.hdwallet.signer

import io.provenance.hdwallet.ec.CURVE
import io.provenance.hdwallet.ec.Curve
import java.math.BigInteger

data class ECDSASignature(val r: BigInteger, val s: BigInteger, val curve: Curve = CURVE) {
    private val halfCurveOrder = curve.n.shiftRight(1)

    companion object {
        /**
         * decodeAsBTC returns an ECDSASignature where the 64 byte array is divided
         * into r || s with each being a 32 byte big endian integer.
         */
        fun decodeAsBTC(bytes: ByteArray, curveParams: Curve = CURVE): ECDSASignature {
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
     * encodeAsBTC returns the ECDSA signature as a ByteArray of r || s,
     * where both r and s are encoded into 32 byte big endian integers.
     */
    fun encodeAsBTC(): ByteArray {

        /**
         * Returns the bytes from a BigInteger as an unsigned version by truncating a byte if needed.
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
}
