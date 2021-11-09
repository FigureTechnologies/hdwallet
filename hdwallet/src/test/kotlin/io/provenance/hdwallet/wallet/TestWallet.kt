package io.provenance.hdwallet.wallet

import io.provenance.hdwallet.bip32.toRootKey
import io.provenance.hdwallet.bip39.DeterministicSeed
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.encoding.base58.base58EncodeChecked
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class TestWallet {
    private data class Tv(val path: String, val address: String, val pubKey: String, val prvKey: String, val signature: String)

    private fun runSynchronousTestVectors(seed: DeterministicSeed, vectors: List<Tv>) {
        vectors.map { runBip32Test(seed, it) }
    }

    private fun runSynchronousTestVectorsSeed(seed: DeterministicSeed, vectors: List<Tv>) {
        vectors.map { runFromSeedTest(seed, it) }
    }

    private fun runBip32Test(seed: DeterministicSeed, vector: Tv) {
        val encodedKey = seed.toRootKey().serialize().base58EncodeChecked()
        val wallet: Wallet = Wallet.fromBip32("test", encodedKey)

        val childKey = wallet[vector.path]

        val sig = childKey.sign( "test".toByteArray().sha256())
        Assert.assertEquals(childKey.address, vector.address)
    }

    private fun runFromSeedTest(seed: DeterministicSeed, vector: Tv) {
        val wallet = Wallet.fromSeed("test", seed)
        val childKey = wallet[vector.path]
        val sig = childKey.sign("test".toByteArray().sha256())

        Assert.assertEquals(childKey.address, vector.address)
    }

    private fun runParallelTestVectors(seed: DeterministicSeed, vectors: List<Tv>) {
        runBlocking {
            val tasks = vectors.parallelStream().map {
                async { runBip32Test(seed, it) }
            }.toList()
            // await for all async tests to finish
            tasks.awaitAll();
        }
    }

    private fun runParallelTestVectorsSeed(seed: DeterministicSeed, vectors: List<Tv>) {
        runBlocking {
            val tasks = vectors.parallelStream().map {
                async { runFromSeedTest(seed, it) }
            }.toList()
            // await for all async tests to finish
            tasks.awaitAll();
        }
    }

    @Test
    fun testWalletSyncFromSeed() = runSynchronousTestVectorsSeed(
        MnemonicWords.of("this is a test phrase and is completely made up").toSeed("trezor".toCharArray()),
        getVectors()
    )

    @Test
    fun testWalletAsyncFromSeed() = runParallelTestVectorsSeed(
        MnemonicWords.of("this is a test phrase and is completely made up").toSeed("trezor".toCharArray()),
        getVectors()
    )

    @Test
    fun testWalletSyncFromBip32() = runSynchronousTestVectors(
        MnemonicWords.of("this is a test phrase and is completely made up").toSeed("trezor".toCharArray()),
        getVectors()
    )

    @Test
    fun testWalletAsyncFromBip32() = runParallelTestVectors(
        MnemonicWords.of("this is a test phrase and is completely made up").toSeed("trezor".toCharArray()),
        getVectors()
    )

    private fun getVectors(): List<Tv> {
        return listOf(
            Tv("m", "test1lrk2fun30k5zuu8cv8zfwym5utnsflswzw5w23", "", "", ""),
            Tv("m/0'", "test1nfn9t66keycrkffyfgwsrh707ywp6xk2j8fz2u", "", "", ""),
            Tv("m/0'", "test1nfn9t66keycrkffyfgwsrh707ywp6xk2j8fz2u", "", "", ""),
            Tv("m/555'/1'/0'/0/0", "test12pmlnpz3g5x7lkyg48ecedpcd0draaek5c7ynd", "", "", ""),
            Tv("m/0'/0'", "test1ux0rnahfzqt0p363g98c9scxpxsjfspy74qet7", "", "", ""),
            Tv("m/44'/1'/0'/0/0", "test1428y2937447fxnmvkp0jn2grs3yuw7v8f0ctgs", "", "", ""),
            Tv("m/120'", "test1f4j2mwk2nkrq0y003l32cagglvaf6g7dch26z6", "", "", ""),
            Tv("m/45'", "test1nvj735cyh6c66zc9q3rydx8ep93uf8wmzjklku", "", "", ""),
            Tv("m/46'/1'/0'/0/0", "test1juyykjj6hvvltthtxdaq0yra65xc03ewkfcec2", "", "", ""),
            Tv("m/200'/1'/0'/0/0", "test1pv9z6vgylcdmk229yas6ykgc8yyhvuqelt82lc", "", "", ""),
            Tv("m/301'/1'/0'/0/0", "test1798z9sh6ch6n5qs68d5lhet4csmx4nxw4ckmdd", "", "", ""),
            Tv("m/20'/1'/0'/0/0", "test1m87qtlkdcclecpc3es83p6tvgjj7t6wtj8mtye", "", "", ""),
            Tv("m/10'/1'/0'/0/0", "test16czkuz60urvtd3efcy4nnqfckje9z8zkmzme7x", "", "", ""),
            Tv("m/15'/1'/0'/0/0", "test1p93yyy6p26dq80lcw77uhlvsed5kwk5gsvpuv3", "", "", ""),
            Tv("m/30'/1'/0'/0/0", "test1tuj0pw8jge4cpnwv8csytwfjftyu8z3vkhj994", "", "", ""),
            Tv("m/21'/1'/0'/0/0", "test1px9jwpwdw0rejlqt9qmelk7hdpnaurgrep8em8", "", "", "")
        )
    }
}