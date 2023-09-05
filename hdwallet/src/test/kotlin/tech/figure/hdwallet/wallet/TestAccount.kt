package tech.figure.hdwallet.wallet

import java.security.KeyException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tech.figure.hdwallet.bip32.AccountType
import tech.figure.hdwallet.bip32.ExtKey
import tech.figure.hdwallet.bip32.toRootKey
import tech.figure.hdwallet.bip39.MnemonicWords
import tech.figure.hdwallet.bip44.DerivationPath
import tech.figure.hdwallet.encoding.base58.base58DecodeChecked
import tech.figure.hdwallet.hrp.Hrp

class TestAccount {

    private val seed =
        MnemonicWords.of("letter advice cage absurd amount doctor acoustic avoid letter advice cage absurd amount doctor acoustic avoid letter always")
            .toSeed("TREZOR".toCharArray())

    private fun Account.toExtKey(publicOnly: Boolean = false): ExtKey =
        ExtKey.deserialize(serializeExtKey(publicOnly).base58DecodeChecked())

    @Test
    fun `indexing from a root Account is successful`() {
        val wallet = DefaultWallet(Hrp.ProvenanceBlockchain.testnet, seed.toRootKey())

        // Child accounts are not root:
        val path = DerivationPath.from("m/44'/1'/0'/0/0")
        val addressAccount = wallet[path]
        assertFalse(addressAccount.isRoot())

        val addressAccountExtKey = addressAccount.toExtKey()
        assertEquals(AccountType.ADDRESS, addressAccountExtKey.depth)
    }

    @Test
    fun `indexing from an Account derived from a partial path is successful`() {
        val wallet = DefaultWallet(Hrp.ProvenanceBlockchain.testnet, seed.toRootKey())

        val childSubAccount = wallet["m/44'/1'/123'"]
        val childSubAccountExtKey = childSubAccount.toExtKey()
        assertEquals(AccountType.GENERAL, childSubAccountExtKey.depth) // general = account
        assertFalse(childSubAccount.isRoot())

        val scopeAccount = childSubAccount[0]
        val scopeAccountExtKey = scopeAccount.toExtKey()
        assertEquals(AccountType.SCOPE, scopeAccountExtKey.depth)
        assertFalse(scopeAccount.isRoot())

        val addressAccount = scopeAccount[0]
        val addressAccountExtKey = addressAccount.toExtKey()
        assertEquals(AccountType.ADDRESS, addressAccountExtKey.depth)
        assertFalse(addressAccount.isRoot())
    }

    @Test
    fun `indexing further from a child address Account will fail`() {
        val wallet = DefaultWallet(Hrp.ProvenanceBlockchain.testnet, seed.toRootKey())
        val path = DerivationPath.from("m/44'/1'/0'/0/0")
        val addressAccount = wallet[path]
        // Indexing further will fail:
        assertThrows<KeyException> {
            addressAccount[0]
        }
    }
}
