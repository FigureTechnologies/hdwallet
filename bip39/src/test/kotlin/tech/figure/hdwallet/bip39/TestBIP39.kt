package tech.figure.hdwallet.bip39

import com.fasterxml.jackson.module.kotlin.convertValue
import tech.figure.hdwallet.bip39.json.Json.asTree
import tech.figure.hdwallet.bip39.json.Json.om
import tech.figure.hdwallet.common.chararray.split
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

open class TestBIP39 {
    private val passphrase = "TREZOR".toCharArray()
    private val wordlist = getWordList("en")

    @Test
    fun testMnemonic() {
        getTestVectors("en").map(runTestVector(wordlist))
    }

    fun runTestVector(wordList: WordList) = { tv: TV ->
        val mnemonic = MnemonicWords(tv.mnemonic.toCharArray().split(' '))
        val seed = mnemonic.toSeed(passphrase)

        assertEquals(tv.mnemonic, mnemonic.words.joinToString(" ") { it.concatToString() })
        assertEquals(tv.seed, seed.value.hex())

        val createdMnemonic = wordList.createMnemonic(tv.entropy.unhex())
        assertTrue(MnemonicWords(tv.mnemonic.toCharArray().split(' ')).words.deepEquals(createdMnemonic.words))
    }

    private fun getTestVectors(lang: String): List<TV> {
        val json = javaClass
            .getResourceAsStream("/bip39_vectors_${lang.lowercase()}.json")!!
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .asTree()

        return om.convertValue(json)
    }

    private fun getWordList(lang: String): WordList {
        return javaClass
            .getResourceAsStream("/wordlist_${lang.lowercase()}.txt")!!
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .split("\n")
            .map { it.toCharArray() }
            .let { WordList(it) }
    }
}

fun List<CharArray>.deepEquals(other: List<CharArray>): Boolean {
    if (this === other) {
        return true
    }

    if (size != other.size) {
        return false
    }

    for (index in indices) {
        if (!this[index].contentEquals(other[index])) {
            return false
        }
    }
    return true
}
