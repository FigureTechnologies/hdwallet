package tech.figure.hdwallet.signer

import com.google.protobuf.ByteString
import cosmos.crypto.secp256k1.Keys
import io.provenance.client.grpc.Signer
import tech.figure.hdwallet.common.hashing.sha256
import tech.figure.hdwallet.wallet.Account

/**
 * A signing wallet implementation for use with the Provenance gRPC client.
 *
 * Repurposed from <a href="https://github.com/provenance-io/pb-grpc-client-kotlin/blob/main/client/src/main/kotlin/io/provenance/client/wallet/WalletSigner.kt">Provenance Client WalletSigner</a>
 *
 * @property account The account used for transacting on the blockhain.
 */
class WalletSigner(private val account: Account) : Signer {
    override fun address(): String = account.address.value

    override fun pubKey(): Keys.PubKey =
        Keys.PubKey
            .newBuilder()
            .setKey(ByteString.copyFrom(account.keyPair.publicKey.compressed()))
            .build()

    override fun sign(data: ByteArray): ByteArray = BCECSigner()
        .sign(account.keyPair.privateKey, data.sha256())
        .encodeAsBTC()
        .toByteArray()
}
