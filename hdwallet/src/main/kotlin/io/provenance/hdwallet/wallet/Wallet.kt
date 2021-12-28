package io.provenance.hdwallet.wallet

import io.provenance.hdwallet.bip32.ExtKey
import io.provenance.hdwallet.bip32.toRootKey
import io.provenance.hdwallet.bip39.DeterministicSeed
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.bip44.PathElement
import io.provenance.hdwallet.bip44.PathElements
import io.provenance.hdwallet.ec.ECKeyPair
import io.provenance.hdwallet.encoding.base58.base58DecodeChecked
import java.security.KeyException

interface Wallet {
    operator fun get(path: List<PathElement>): Account
    operator fun get(path: String): Account = get(PathElements.from(path))

    companion object {
        /**
         * Create a new wallet from a root key.
         *
         * @param hrp The human-readable prefix to use when generating bech32 addresses from this wallet.
         * @param rootKey The root key to use when generating this wallet.
         * @return [Wallet]
         */
        fun fromRootKey(hrp: String, rootKey: ExtKey): Wallet = DefaultWallet(hrp, rootKey)

        /**
         * Create a new wallet from a deterministic seed value.
         *
         * @param hrp The human-readable prefix to use when generating bech32 addresses from this wallet.
         * @param seed The deterministic seed used to generate this wallet.
         * @param testnet A flag that specifies if this wallet corresponds to a test net. If omitted, the default
         * `false` will be used.
         * @return [Wallet]
         */
        fun fromSeed(hrp: String, seed: DeterministicSeed, testnet: Boolean = false): Wallet =
            DefaultWallet(hrp, seed.toRootKey(testnet = testnet))

        /**
         * Create a new wallet from a BIP32 mnemonic phrase.
         *
         * Example:
         *
         * ```kotlin
         * val wallet: Wallet = Wallet.fromMnemonic(
         *   hrp = "tp",
         *   passphrase = "".toCharArray(),
         *   mnemonicWords = MnemonicWords.of("home used crowd sphere kick taxi strategy just punch admit speak enable"),
         * )
         * ```
         *
         * @param hrp The human-readable prefix to use when generating bech32 addresses from this wallet.
         * @param passphrase The passphrase to use when generating the seed that will be used to create this wallet.
         * @param mnemonicWords The BIP39 mnemonic phrase to use to generate the seed for this wallet.
         * @param testnet A flag that specifies if this wallet corresponds to a test net. If omitted, the default
         * `false` will be used.
         * @return [Wallet]
         */
        fun fromMnemonic(
            hrp: String,
            passphrase: CharArray,
            mnemonicWords: MnemonicWords,
            testnet: Boolean = false
        ): Wallet = fromSeed(hrp = hrp, seed = mnemonicWords.toSeed(passphrase), testnet = testnet)

        /**
         * Create a new wallet from a BIP32 mnemonic phrase.
         *
         * Example:
         *
         * ```kotlin
         * val wallet: Wallet = Wallet.fromMnemonic(
         *   hrp = "tp",
         *   passphrase = "",
         *   mnemonicWords = MnemonicWords.of("home used crowd sphere kick taxi strategy just punch admit speak enable"),
         * )
         * ```
         *
         * @param hrp The human-readable prefix to use when generating bech32 addresses from this wallet.
         * @param passphrase The passphrase to use when generating the seed that will be used to create this wallet.
         * @param mnemonicWords The BIP39 mnemonic phrase to use to generate the seed for this wallet.
         * @param testnet A flag that specifies if this wallet corresponds to a test net. If omitted, the default
         * `false` will be used.
         * @return [Wallet]
         */
        fun fromMnemonic(
            hrp: String,
            passphrase: String,
            mnemonicWords: MnemonicWords,
            testnet: Boolean = false
        ): Wallet = fromMnemonic(
            hrp = hrp,
            passphrase = passphrase.toCharArray(),
            mnemonicWords = mnemonicWords,
            testnet = testnet
        )
    }
}

interface Account {
    val address: String
    val keyPair: ECKeyPair
    fun sign(payload: ByteArray): ByteArray
    operator fun get(index: Int, hardened: Boolean = true): Account

    companion object {
        fun fromBip32(hrp: String, bip32: String): Account {
            val data = try {
                bip32.base58DecodeChecked()
            } catch (e: Throwable) {
                // Eat the exception so no sensitive info gets logged.
                throw KeyException()
            }
            return DefaultAccount(hrp, ExtKey.deserialize(data))
        }
    }
}

interface Discoverer {
    fun discover(account: Account, query: (path: String) -> List<Account>): List<Account>
}
