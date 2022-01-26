package io.provenance.hdwallet.wallet

import io.provenance.hdwallet.bech32.Address
import io.provenance.hdwallet.bip32.AccountType.ROOT
import io.provenance.hdwallet.bip32.ExtKey
import io.provenance.hdwallet.bip44.PathElement
import io.provenance.hdwallet.ec.ECKeyPair
import io.provenance.hdwallet.encoding.base58.base58EncodeChecked
import io.provenance.hdwallet.signer.BCECSigner
import io.provenance.hdwallet.signer.Signer

class DefaultWallet(private val hrp: String, private val key: ExtKey) : Wallet {
    init {
        require(key.depth == ROOT) { "cannot init wallet with non-root key" }
    }

    override fun get(path: List<PathElement>): Account = path
        .fold(key) { t, p -> t.childKey(p.hardenedNumber, p.hardened) }
        .let { DefaultAccount(hrp, it) }
}

class DefaultAccount(
    private val hrp: String,
    private val key: ExtKey,
    private val signer: Signer = BCECSigner()
) : Account {

    override val address: Address = key.keyPair.publicKey.address(hrp)

    override fun serializeExtKey(publicOnly: Boolean): String =
        key.serialize(publicOnly).base58EncodeChecked()

    override val keyPair: ECKeyPair = key.keyPair

    override fun sign(payload: ByteArray, hash: (ByteArray) -> ByteArray): ByteArray =
        signer.sign(key.keyPair.privateKey, hash(payload)).encodeAsBTC().toByteArray()

    override fun get(index: Int, hardened: Boolean): Account =
        DefaultAccount(hrp, key.childKey(index, hardened), signer)
}
