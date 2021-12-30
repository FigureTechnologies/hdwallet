package io.provenance.hdwallet.wallet

import io.provenance.hdwallet.bip32.ExtKey
import io.provenance.hdwallet.bip32.toRootKey
import io.provenance.hdwallet.bip39.DeterministicSeed
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.bip44.PathElement
import io.provenance.hdwallet.bip44.PathElements
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.ec.ECKeyPair
import io.provenance.hdwallet.encoding.base58.base58DecodeChecked
import java.security.KeyException

/**
 * Wallets are the root key representation used to derive [Account]s.
 */
interface Wallet {
    operator fun get(path: List<PathElement>): Account
    operator fun get(path: String): Account = get(PathElements.from(path))

    companion object {
        /**
         * Create a new wallet from a deterministic seed value.
         *
         * @param hrp The human-readable prefix to use when generating bech32 addresses from this wallet.
         * @param seed The deterministic seed used to generate this wallet.
         * @param publicKeyOnly Flag specifying to generate only public keys [default: false]
         * @param testnet Flag specifying if this wallet corresponds to a test net. [default: false]
         * @return [Wallet]
         */
        fun fromSeed(
            hrp: String,
            seed: DeterministicSeed,
            publicKeyOnly: Boolean = false,
            testnet: Boolean = false
        ): Wallet = DefaultWallet(
            hrp = hrp,
            key = seed.toRootKey(publicKeyOnly = publicKeyOnly, testnet = testnet)
        )

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
         * @param publicKeyOnly Flag specifying to generate only public keys [default: false]
         * @param testnet Flag specifying if this wallet corresponds to a test net. [default: false]
         * @return [Wallet]
         */
        fun fromMnemonic(
            hrp: String,
            passphrase: CharArray,
            mnemonicWords: MnemonicWords,
            publicKeyOnly: Boolean = false,
            testnet: Boolean = false
        ): Wallet = fromSeed(
            hrp = hrp,
            seed = mnemonicWords.toSeed(passphrase),
            publicKeyOnly = publicKeyOnly,
            testnet = testnet
        )

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
         * @param publicKeyOnly Flag specifying to generate only public keys [default: false]
         * @param testnet Flag specifying if this wallet corresponds to a test net. [default: false]
         * @return [Wallet]
         */
        fun fromMnemonic(
            hrp: String,
            passphrase: String,
            mnemonicWords: MnemonicWords,
            publicKeyOnly: Boolean = false,
            testnet: Boolean = false
        ): Wallet = fromMnemonic(
            hrp = hrp,
            passphrase = passphrase.toCharArray(),
            mnemonicWords = mnemonicWords,
            publicKeyOnly = publicKeyOnly,
            testnet = testnet
        )
    }
}

/**
 * Accounts are initialized from an [ExtKey] and used to transact on various blockchains.
 */
interface Account {
    /**
     * Bech32 encoded address for this account's extended key.
     */
    val address: String

    /**
     * Elliptic curve keypair for this account.
     */
    val keyPair: ECKeyPair

    /**
     * Serialize this account's extended key to the string xprv / xpub representation.
     * @param publicOnly If true, generate the xpub. If false, generate the xprv.
     */
    fun serializeExtKey(publicOnly: Boolean = false): String

    /**
     * Sign the supplied payload's hash.
     * @param payload The full byte payload to sign.
     * @param hash The hashing algorithm to use on the payload.
     * @return The raw encoded ecdsa (r||s) btc format signature.
     */
    fun sign(payload: ByteArray, hash: (ByteArray) -> ByteArray = ByteArray::sha256): ByteArray

    /**
     * Path down to the next extended key derived from this account's extended key.
     * @param index The index of the next key
     * @param hardened To harden the derived key or not to.
     */
    operator fun get(index: Int, hardened: Boolean = true): Account

    companion object {
        /**
         * Convert a base58 check encoded bip32 serialized extended key back into an account.
         * @param hrp The human-readable prefix for the network this key will be used with.
         * @param bip32 The base58 check encoded xprv / xpub extended key string.
         */
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

/**
 * Helper function because serialize has two names.
 */
fun Account.toBip32(publicOnly: Boolean = false): String = serializeExtKey(publicOnly)

/**
 * Account discoverer interface to determine used addresses from the account.
 */
interface Discoverer {
    fun discover(account: Account, query: (path: String) -> List<Account>): List<Account>
}