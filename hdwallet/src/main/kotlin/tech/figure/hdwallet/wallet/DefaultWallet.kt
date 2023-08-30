package tech.figure.hdwallet.wallet

import tech.figure.hdwallet.bech32.Address
import tech.figure.hdwallet.bip32.AccountType.ROOT
import tech.figure.hdwallet.bip32.ExtKey
import tech.figure.hdwallet.bip44.DerivationPath
import tech.figure.hdwallet.bip44.PathElement
import tech.figure.hdwallet.bip44.toDerivationPath
import tech.figure.hdwallet.ec.ECKeyPair
import tech.figure.hdwallet.encoding.base58.base58EncodeChecked
import tech.figure.hdwallet.signer.BCECSigner
import tech.figure.hdwallet.signer.Signer

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
    private val signer: Signer = BCECSigner(),
) : Account {

    override val address: Address = key.keyPair.publicKey.address(hrp)

    override fun serializeExtKey(publicOnly: Boolean): String =
        key.serialize(publicOnly).base58EncodeChecked()

    override val keyPair: ECKeyPair = key.keyPair
    override fun isRoot(): Boolean = key.depth == ROOT

    override fun sign(payload: ByteArray, hash: (ByteArray) -> ByteArray): ByteArray =
        signer.sign(key.keyPair.privateKey, hash(payload)).encodeAsBTC().toByteArray()

    override fun get(index: Int, hardened: Boolean): Account =
        DefaultAccount(hrp, key.childKey(index, hardened), signer)

    override fun get(path: List<PathElement>): Account = DefaultAccount(hrp, key.childKey(path))

    override fun get(path: DerivationPath): Account = get(path.elements())

    override fun get(path: String): Account = DefaultAccount(hrp, key.childKey(path))
}
