package io.provenance.hdwallet.signer

import io.provenance.hdwallet.ec.CURVE
import io.provenance.hdwallet.ec.Curve
import java.math.BigInteger
import java.nio.ByteBuffer

data class ECDSASignature(val r: BigInteger, val s: BigInteger, val curve: Curve = CURVE) {
    private val halfCurveOrder = curve.n.shiftRight(1)

    companion object {
        // Test for values under 128 which indicate a 'narrow' char / high bit unset.
        private fun Byte.isNarrow() = (this.toInt() and 0x80) != 0x80

        // Test for values over 128 which indicate a 'wide' char / have high bit set.
        private fun Byte.isWide() = (this.toInt() and 0x80) == 0x80

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

        private const val DER_ECDSA_MIN_LEN = 8

        fun decodeAsDER(bytes: ByteArray, curveParams: Curve = CURVE): ECDSASignature {
            // 0x30 type : sequence
            // 0xNN len  : sequence
            // 0x02 type : integer
            // 0x01 len  : integer
            // 0xNN value: integer
            // 0x02 type : integer
            // 0x01 len  : integer
            // 0xNN value: integer
            require(bytes.size >= DER_ECDSA_MIN_LEN) { "malformed der encoding: encoded ecdsa sig len(${bytes.size}) < min($DER_ECDSA_MIN_LEN)" }

            val bb = ByteBuffer.wrap(bytes)

            // seq
            require(bb.get() == 0x30.toByte()) { "malformed der encoding: not a seq" }

            // seq len
            val sigLen = bb.get()
            require(sigLen <= bb.remaining()) { "malformed der encoding: bad length $sigLen" }

            // type
            require(bb.get() == 0x02.toByte()) { "malformed der encoding: R type must be 02" }

            // len
            val rByteLen = bb.get().toInt()
            require(rByteLen <= bb.remaining()) { "malformed der encoding: bad R len $rByteLen" }

            // value
            val rBytes = ByteArray(rByteLen)
            bb.get(rBytes)

            // type
            require(bb.get() == 0x02.toByte()) { "malformed der encoding: S type must be 02" }

            // len
            val sByteLen = bb.get().toInt()
            require(sByteLen <= bb.remaining()) { "malformed der encoding: bad S len $sByteLen" }

            // value
            val sBytes = ByteArray(sByteLen)
            bb.get(sBytes)
            require(bb.remaining() == 0) { "malformed der encoding: ${bb.remaining()} bytes remaining" }

            require(rBytes[0].isNarrow()) { "R is not a canonical value, could be negative" }
            require((rBytes[0] != 0x00.toByte()) || (rBytes.size <= 1 || rBytes[1].isWide())) { "excessive padding" }

            require(sBytes[0].isNarrow()) { "S is not a canonical value, could be negative" }
            require((sBytes[0] != 0x00.toByte()) || (sBytes.size <= 1 || sBytes[1].isWide())) { "excessive padding" }

            val ecdsa = ECDSASignature(BigInteger(rBytes), BigInteger(sBytes))

            require(ecdsa.r.signum() == 1) { "R value must be positive" }
            require(ecdsa.s.signum() == 1) { "S value must be positive" }

            require(ecdsa.r < curveParams.n) { "R value must be less than curve N" }
            require(ecdsa.s < curveParams.n) { "S value must be less than curve N" }

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
