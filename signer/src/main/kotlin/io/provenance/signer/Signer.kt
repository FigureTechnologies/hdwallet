package io.provenance.signer

import io.provenance.ec.PrivateKey
import io.provenance.ec.PublicKey

/**
 *
 */
interface Signer {
    /**
     *
     */
    fun sign(privateKey: PrivateKey, payload: ByteArray): ECDSASignature
}

/**
 *
 */
interface SignatureVerifier {
    /**
     *
     */
    fun verify(publicKey: PublicKey, data: ByteArray, signature: ECDSASignature): Boolean
}

/**
 *
 */
interface SignAndVerify : Signer, SignatureVerifier

/**
 *
 */
interface StreamSigner {
    /**
     *
     */
    fun init(privateKey: PrivateKey)

    /**
     *
     */
    fun update(bytes: ByteArray, offset: Int, len: Int)

    /**
     *
     */
    fun update(bytes: ByteArray) = update(bytes, 0, bytes.size)

    /**
     *
     */
    fun sign(): ECDSASignature
}

/**
 *
 */
interface StreamSignatureVerifier {
    /**
     *
     */
    fun init(publicKey: PublicKey)

    /**
     *
     */
    fun update(bytes: ByteArray, offset: Int, len: Int)

    /**
     *
     */
    fun update(bytes: ByteArray) = update(bytes, 0, bytes.size)

    /**
     *
     */
    fun verify(signature: ECDSASignature): Boolean
}
