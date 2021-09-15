package io.provenance.hdwallet.bech32

/**
 * BIP173 compliant processing functions for handling Bech32 encoding for addresses
 */
object Bech32 {
    private infix fun UByte.shl(bitCount: Int) = ((this.toInt() shl bitCount) and 0xff).toUByte()
    private infix fun UByte.shr(bitCount: Int) = (this.toInt() shr bitCount).toUByte()

    private const val CHECKSUM_SIZE = 6
    private const val MIN_VALID_LENGTH = 8
    private const val MAX_VALID_LENGTH = 90
    private const val MIN_VALID_CODEPOINT = 33
    private const val MAX_VALID_CODEPOINT = 126

    private const val charset = "qpzry9x8gf2tvdw0s3jn54khce6mua7l"
    private val gen = intArrayOf(0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3)

    /**
     * Decodes a Bech32 String
     */
    fun decode(bech32: String): Bech32Data {
        require(bech32.length in MIN_VALID_LENGTH..MAX_VALID_LENGTH) { "invalid bech32 string length" }
        require(bech32.toCharArray().all { c -> c.toInt() in MIN_VALID_CODEPOINT..MAX_VALID_CODEPOINT }) {
            val invalidChars = bech32.toCharArray()
                .filter { it.toInt() !in MIN_VALID_CODEPOINT..MAX_VALID_CODEPOINT }
            "invalid characters in bech32: $invalidChars"
        }
        require(bech32 == bech32.toLowerCase() || bech32 == bech32.toUpperCase()) {
            "bech32 must be either all upper or lower case"
        }
        require(bech32.substring(1).dropLast(CHECKSUM_SIZE).contains('1')) {
            "invalid index of '1'"
        }

        val hrp = bech32.substringBeforeLast('1').toLowerCase()
        val dataString = bech32.substringAfterLast('1').toLowerCase()

        require(charset.toList().containsAll(dataString.toList())) {
            "invalid data encoding character in bech32"
        }

        val dataBytes = dataString.map { c -> charset.indexOf(c).toByte() }.toByteArray()
        val checkBytes = dataString.takeLast(CHECKSUM_SIZE).map { c -> charset.indexOf(c).toByte() }.toByteArray()
        val actualSum = checksum(hrp, dataBytes.dropLast(CHECKSUM_SIZE).toByteArray())
        require(1 == polymod(expandHrp(hrp).plus(dataBytes.map { d -> d.toInt() }))) {
            "checksum failed: $checkBytes != $actualSum"
        }

        return Bech32Data(hrp, dataBytes.dropLast(CHECKSUM_SIZE).toByteArray())
    }

    /**
     * ConvertBits regroups bytes with toBits set based on reading groups of bits as a continuous stream group by fromBits.
     * This process is used to convert from base64 (from 8) to base32 (to 5) or the inverse.
     */
    fun convertBits(data: ByteArray, fromBits: Int, toBits: Int, pad: Boolean): ByteArray {
        require(fromBits in 1..8 && toBits in 1..8) { "only bit groups between 1 and 8 are supported" }

        // resulting bytes with each containing the toBits bits from the input set.
        var regrouped = arrayListOf<Byte>()

        var nextByte = 0.toUByte()
        var filledBits = 0

        data.forEach { d ->
            // discard unused bits.
            var b = (d.toUByte() shl (8 - fromBits))

            // How many bits remain to extract from input data.
            var remainFromBits = fromBits

            while (remainFromBits > 0) {
                // How many bits remain to be copied in
                val remainToBits = toBits - filledBits

                // we extract the remaining bits unless that is more than we need.
                val toExtract = remainFromBits.takeUnless { remainToBits < remainFromBits } ?: remainToBits
                check(toExtract >= 0) { "extract should be positive" }

                // move existing bits to the left to make room for bits toExtract, copy in bits to extract
                nextByte = (nextByte shl toExtract) or (b shr (8 - toExtract))

                // discard extracted bits and update position counters
                b = b shl toExtract
                remainFromBits -= toExtract
                filledBits += toExtract

                // if we have a complete group then reset.
                if (filledBits == toBits) {
                    regrouped.add(nextByte.toByte())
                    filledBits = 0
                    nextByte = 0.toUByte()
                }
            }
        }

        // pad any unfinished groups as required
        if (pad && filledBits > 0) {
            nextByte = nextByte shl (toBits - filledBits)
            regrouped.add(nextByte.toByte())
            filledBits = 0
            nextByte = 0.toUByte()
        }

        // check for any incomplete groups that are more than 4 bits or not all zeros
        require(filledBits == 0 || (filledBits <= 4 && nextByte == 0.toUByte())) { "invalid incomplete group" }

        return regrouped.toByteArray()
    }

    /**
     * Encodes data 5-bit bytes (data) with a given human readable portion (hrp) into a bech32 string.
     * @see convertBits for conversion or ideally use the Bech32Data extension functions
     */
    fun encode(hrp: String, fiveBitData: ByteArray): String {
        return (fiveBitData.plus(checksum(hrp, fiveBitData)).map { b -> charset[b.toInt() and 0xff] })
            .joinToString("", hrp + "1")
    }

    /**
     * Calculates a bech32 checksum based on BIP 173 specification
     */
    fun checksum(hrp: String, data: ByteArray): ByteArray {
        var values = expandHrp(hrp)
            .plus(data.map { d -> d.toInt() })
            .plus(Array<Int>(6) { _ -> 0 }.toIntArray())

        var poly = polymod(values) xor 1

        return (0..5).map {
            ((poly shr (5 * (5 - it))) and 31).toByte()
        }.toByteArray()
    }

    /**
     * Expands the human readable prefix per BIP173 for Checksum encoding
     */
    private fun expandHrp(hrp: String) = let {
        hrp.map { c -> c.toInt() shr 5 } + 0 + hrp.map { c -> c.toInt() and 31 }
    }.toIntArray()

    /**
     * Polynomial division function for checksum calculation.  For details see BIP173
     */
    private fun polymod(values: IntArray): Int {
        var chk = 1
        return values.map {
            var b = chk shr 25
            chk = ((chk and 0x1ffffff) shl 5) xor it
            (0..4).map {
                if (((b shr it) and 1) == 1) {
                    chk = chk xor gen[it]
                }
            }
        }.let { chk }
    }
}
