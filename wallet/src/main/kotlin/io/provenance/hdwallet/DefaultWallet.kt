package io.provenance.hdwallet

import io.provenance.bech32.toBech32
import io.provenance.bip32.AccountType.ROOT
import io.provenance.bip32.ExtKey
import io.provenance.bip44.PathElement
import io.provenance.hashing.sha256
import io.provenance.hashing.sha256hash160
import io.provenance.signer.BCECSigner

class DefaultWallet(private val hrp: String, private val key: ExtKey) : Wallet {
    init {
        require(key.depth == ROOT) { "cannot init wallet with non-root key" }
    }

    override fun get(path: List<PathElement>): Account = path
        .fold(key) { t, p -> t.childKey(p.hardenedNumber, p.hardened) }
        .let { DefaultAccount(hrp, it) }
}

class DefaultAccount(hrp: String, key: ExtKey) : Account {
    override val address: String =
        key.keyPair.publicKey.compressed().sha256hash160().toBech32(hrp).address

    private val keyMaker = hrp to key::childKey
    private val signateur = BCECSigner()
    private val signer = { bytes: ByteArray -> signateur.sign(key.keyPair.privateKey, bytes.sha256()) }

    override fun sign(payload: ByteArray): ByteArray =
        signer.invoke(payload).encodeAsBTC()

    override fun get(index: Int, hardened: Boolean): Account =
        DefaultAccount(keyMaker.first, keyMaker.second(index, hardened))
}
