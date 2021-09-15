// Copyright (c) 2020 Figure Technologies Inc.
// The contents of this file were derived from an implementation
// by the btcsuite developers https://github.com/btcsuite/btcutil.

// Copyright (c) 2017 The btcsuite developers
// Use of this source code is governed by an ISC
// license that can be found in the LICENSE file.

package io.provenance.hdwallet.common.bech32

import org.apache.commons.codec.binary.Base16

/**
 * Given an array of bytes, associate an HRP and return a Bech32Data instance.
 * Input: byteArrayOf(8, 9, 10, 11)
 * Output: Bech32Data{address:x1pqys5zc7vr64r hrp:x checksum:1E0C031A1503 bytes:08090A0B}
 */
fun ByteArray.toBech32(hrp: String): Bech32Data = Bech32Data(hrp, Bech32.convertBits(this, 8, 5, true))

/**
 * Using a string in bech32 encoded address format, parses out and returns a Bech32Data instance
 * Input: x1pqys5zc7vr64r
 * Output: Bech32Data{address:x1pqys5zc7vr64r hrp:x checksum:1E0C031A1503 bytes:08090A0B}
 */
fun String.toBech32(): Bech32Data = Bech32.decode(this)

/**
 * Convert a byte array into its equivalent string representation.
 * Input: byteArrayOf(8, 9, 10, 11)
 * Output: 08090A0B
 */
internal fun ByteArray.hex(): String = Base16().encodeAsString(this)
internal fun String.unhex(): ByteArray = Base16().decode(this)

/**
 * Bech32 Data encoding instance containing data for encoding as well as a human readable prefix
 */
class Bech32Data(val hrp: String, fiveBitData: ByteArray) {
    /**
     * The encapsulated data as typical 8bit bytes.
     */
    val data = Bech32.convertBits(fiveBitData, 5, 8, false)

    /**
     * Checksum for encapsulated data + hrp
     */
    val checksum = Bech32.checksum(hrp, fiveBitData)

    /**
     * Address is the Bech32 encoded value of the data prefixed with the human readable portion and
     * protected by an appended checksum.
     */
    val address = Bech32.encode(hrp, fiveBitData)

    /**
     * The Bech32 Address toString prints state information for debugging purposes.
     * @see address() for the bech32 encoded address string output.
     * Output: address:x1pqys5zc7vr64r hrp:x checksum:1E0C031A1503 bytes:08090A0B
     */
    override fun toString(): String {
        return "address:$address hrp:$hrp checksum:${checksum.hex()} bytes:${data.hex()}"
    }
}
