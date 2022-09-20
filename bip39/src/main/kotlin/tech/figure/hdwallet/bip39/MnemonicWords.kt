package tech.figure.hdwallet.bip39

import tech.figure.hdwallet.common.chararray.CharArrayBuffer
import tech.figure.hdwallet.common.chararray.split
import java.security.SecureRandom
import java.text.Normalizer
import java.text.Normalizer.Form.NFKD
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private fun randomByteArray(size: Int) =
    ByteArray(size).also { SecureRandom().nextBytes(it) }

class MnemonicWords(val words: List<CharArray>) {
    companion object {
        const val MNEMONIC_ITERATIONS = 2048
        const val KEY_LENGTH = 512

        fun of(words: String) = MnemonicWords(words.toCharArray().split(' '))

        fun generate(strength: Int = 128, wordList: List<CharArray> = WORDLIST_ENGLISH): MnemonicWords {
            require(strength % 32 == 0) { "strength must be multiple of 32" }
            return fromEntropy(randomByteArray(strength / 8), wordList)
        }

        fun fromEntropy(entropy: ByteArray, wordList: List<CharArray> = WORDLIST_ENGLISH): MnemonicWords =
            WordList(wordList).createMnemonic(entropy)
    }

    // Assuming UTF8
    private fun CharArray.toByteArray(): ByteArray = map { (it.code and 0xFF).toByte() }.toByteArray()

    private fun CharArray.normalizeNKFD(): CharArray {
        val dest = CharArrayBuffer()
        for (index in indices) {
            dest.append(Normalizer.normalize("" + this[index], NFKD).first())
        }
        return dest.toCharArray()
    }

    fun toSeed(passphrase: CharArray): DeterministicSeed {
        val mnemonic = words
            .map { it.normalizeNKFD() }
            .joinTo(CharArrayBuffer(), " ", transform = { it.concatToString() })
            .toCharArray()

        val passnorm = passphrase.normalizeNKFD()
        val salt = "mnemonic".toByteArray() + passnorm.toByteArray()
        val spec = PBEKeySpec(mnemonic, salt, MNEMONIC_ITERATIONS, KEY_LENGTH)
        val kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return DeterministicSeed.fromKey(kf.generateSecret(spec))
    }
}
