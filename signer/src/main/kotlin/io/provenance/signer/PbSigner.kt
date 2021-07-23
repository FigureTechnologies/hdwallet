package com.figure.wallet.signer
//
//import com.figure.wallet.hdwallet.encodeAsBTC
//import com.figure.wallet.util.Hash
//import com.google.protobuf.ByteString
//import io.provenance.core.extensions.toByteString
//import org.kethereum.crypto.impl.ec.EllipticCurveSigner
//import org.kethereum.model.ECKeyPair
//
//object PbSigner {
//    fun signerFor(keyPair: ECKeyPair): (ByteArray) -> List<ByteString> = { bytes ->
//        bytes.let {
//            Hash.sha256(it)
//        }.let {
//            EllipticCurveSigner().sign(it, keyPair.privateKey.key, true).encodeAsBTC().toByteString()
//        }.let {
//            listOf(it)
//        }
//    }
//}
