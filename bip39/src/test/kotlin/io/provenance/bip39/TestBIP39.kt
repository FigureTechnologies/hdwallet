package io.provenance.bip39

import com.fasterxml.jackson.module.kotlin.convertValue
import io.provenance.bip39.json.Json.asTree
import io.provenance.bip39.json.Json.om
import io.provenance.chararray.split
import org.junit.Assert
import org.junit.Test

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

        Assert.assertEquals(tv.mnemonic, mnemonic.words.joinToString(" ") { it.concatToString() })
        Assert.assertEquals(tv.seed, seed.value.hex())

        val createdMnemonic = wordList.createMnemonic(tv.entropy.unhex())
        Assert.assertTrue(MnemonicWords(tv.mnemonic.toCharArray().split(' ')).words.deepEquals(createdMnemonic.words))
    }

    private fun getTestVectors(lang: String): List<TV> {
        val json = javaClass
            .getResourceAsStream("/bip39_vectors_${lang.toLowerCase()}.json")!!
            .readAllBytes()
            .toString(Charsets.UTF_8)
            .asTree()

        return om.convertValue(json)
    }

    private fun getWordList(lang: String): WordList {
        return javaClass
            .getResourceAsStream("/wordlist_${lang.toLowerCase()}.txt")!!
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
