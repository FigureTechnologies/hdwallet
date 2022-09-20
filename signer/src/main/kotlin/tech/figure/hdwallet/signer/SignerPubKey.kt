package com.figure.wallet.signer
//
//import com.figure.wallet.hdwallet.Bech32
//import com.figure.wallet.hdwallet.encodeAsBTC
//import com.figure.wallet.hdwallet.toBech32Data
//import com.figure.wallet.signer.ProvenanceSigner.Companion.KeyType.EC
//import com.figure.wallet.util.Hash
//import com.google.protobuf.ByteString
//import io.p8e.crypto.proto.CryptoProtos.Address
//import io.p8e.crypto.proto.CryptoProtos.AddressType.BECH32
//import io.p8e.crypto.proto.CryptoProtos.Key
//import io.p8e.crypto.proto.CryptoProtos.Signature
//import tech.figure.core.encryption.ecies.ECUtils
//import tech.figure.core.extensions.toByteString
//import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
//import org.bouncycastle.jce.provider.BouncyCastleProvider
//import org.kethereum.crypto.impl.ec.EllipticCurveSigner
//import org.kethereum.model.ECKeyPair
//import java.security.KeyPair
//import java.security.PublicKey
//import java.security.Security
//import java.security.interfaces.ECPrivateKey
//import java.security.interfaces.ECPublicKey
//
//interface Signer {
//    fun address(): Address
//
//    /**
//     * Sign byte array.
//     */
//    fun sign(data: ByteArray): Signature
//
//    fun signLambda(): (ByteArray) -> List<ByteString>
//}
//
//class ProvenanceSigner(val keypair: KeyPair, val ecKeyPair: ECKeyPair? = null, private val mainNet: Boolean) : Signer {
//    constructor(keypair: KeyPair, mainNet: Boolean) : this(keypair, null, mainNet)
//
//    init {
//        Security.addProvider(BouncyCastleProvider())
//    }
//
//    companion object {
//        enum class KeyType(val algo: String) {
//            EC("SHA256withECDSA"),
//            UNKNOWN("Unknown")
//        }
//
//        fun keyType(key: java.security.Key) =
//            when (key) {
//                is ECPublicKey, is ECPrivateKey -> EC
//                else -> throw UnsupportedOperationException("Key type not implemented")
//            }
//
//        fun asKey(key: PublicKey, mainNet: Boolean) =
//            Key.newBuilder().also {
//                var keyBytes: ByteArray
//                keyType(key).also { keyType ->
//                    when (keyType) {
//                        EC -> {
//                            keyBytes = (key as BCECPublicKey).q.getEncoded(true)
//                            it.curve = ECUtils.LEGACY_DIME_CURVE
//                        }
//                        else -> throw UnsupportedOperationException("Key type not implemented")
//                    }
//                }
//
//                it.encodedKey = keyBytes.toByteString()
//                it.address = getAddress(keyBytes, mainNet)
//                it.encoding = "RAW"
//            }.build()
//
//        fun getAddress(key: PublicKey, mainNet: Boolean) = getAddress(asKey(key, mainNet).encodedKey.toByteArray(), mainNet)
//
//        fun getAddress(bytes: ByteArray, mainNet: Boolean) =
//            bytes.let {
//                (ECUtils.convertBytesToPublicKey(it) as BCECPublicKey).q.getEncoded(true)
//            }.let {
//                Hash.sha256hash160(it)
//            }.let {
//                mainNet.let {
//                    if (it)
//                        Bech32.PROVENANCE_MAINNET_ACCOUNT_PREFIX
//                    else
//                        Bech32.PROVENANCE_TESTNET_ACCOUNT_PREFIX
//                }.let { prefix ->
//                    it.toBech32Data(prefix).address
//                }
//            }.let {
//                Address.newBuilder().setValue(it).setType(BECH32).build()
//            }
//    }
//
//    override fun address(): Address = getAddress(keypair.public, mainNet)
//
//    /**
//     * Sign byte array.
//     */
//    override fun sign(data: ByteArray): Signature {
//        require(ecKeyPair != null) { "Signer doesn't implement kethereum BigInteger keypair." }
//        val signature = EllipticCurveSigner()
//            .sign(Hash.sha256(data), ecKeyPair.privateKey.key, true)
//            .encodeAsBTC()
//            .toByteString()
//
//        return Signature.newBuilder()
//            .setPublicKey(asKey(keypair.public, mainNet))
//            .setSignatureBytes(signature)
//            .build()
//    }
//
//    override fun signLambda(): (ByteArray) -> List<ByteString> {
//        require(ecKeyPair != null) { "Signer doesn't implement kethereum BigInteger keypair." }
//        return PbSigner.signerFor(ecKeyPair)
//    }
//
//    private fun <T : Any, X : Throwable> T?.orThrow(supplier: () -> X) = this?.let { it } ?: throw supplier()
//}
