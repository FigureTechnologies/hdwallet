package io.provenance.hdwallet.bip39

import java.security.MessageDigest

class WordList(private val dictionary: List<CharArray>) {
    private fun ByteArray.sha256() = MessageDigest.getInstance("SHA-256").digest(this)

    // createMnemonic based off of docs: https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki
    fun createMnemonic(entropy: ByteArray): MnemonicWords {
        // Must be between 128 and 256 bytes and be a multiple of 32
        val entBits = entropy.size * 8
        // require(entBits in 128..256) { "Entropy must be in range 128..256" }
        require(entBits % 32 == 0) { "Entropy must be multiple of 32" }

        val checksumLen = entBits / 32
        val checksumEntropy = entropy.copyOf() + entropy.sha256()[0]
        val range = (0 until entBits + checksumLen).step(11)
        val mnemonic = range
            .map { offset -> next11Bits(checksumEntropy, offset) }
            .map { wordIndex -> dictionary[wordIndex] }
        return MnemonicWords(mnemonic)
    }
}

private fun next11Bits(bytes: ByteArray, offset: Int): Int {
    val skip = offset / 8
    val lowerBitsToRemove = 3 * 8 - 11 - offset % 8
    val a = bytes[skip].toInt() and 0xff shl 16
    val b = bytes[skip + 1].toInt() and 0xff shl 8
    val c =
        if (lowerBitsToRemove < 8) bytes[skip + 2].toInt() and 0xff
        else 0
    return (a or b or c) shr lowerBitsToRemove and (1 shl 11) - 1
}
