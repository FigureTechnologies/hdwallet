package tech.figure.hdwallet.signer

import tech.figure.hdwallet.ec.PrivateKey
import tech.figure.hdwallet.ec.PublicKey

/**
 * An interface for providing cryptographic digital signing capabilities.
 */
interface Signer {
    /**
     * Sign a data payload with the given private key, returning a signature.
     *
     * @param privateKey The private key of the identity whose signature is going to be generated.
     * @param payload The data payload to sign.
     * @return The cryptographic signature, [ECDSASignature].
     */
    fun sign(privateKey: PrivateKey, payload: ByteArray): ECDSASignature
}

/**
 * An interface for digital signature verification.
 */
interface SignatureVerifier {
    /**
     * Verify a signature using the supplied public key and data.
     *
     * @param publicKey The public key of the identity whose signature is going to be verified.
     * @param data The data payload to verify.
     * @param signature The [ECDSASignature] to be verified.
     */
    fun verify(publicKey: PublicKey, data: ByteArray, signature: ECDSASignature): Boolean
}

/**
 * Convenience interface for signing and verification.
 */
interface SignAndVerify : Signer, SignatureVerifier

/**
 * An interface for providing cryptographic signing capabilities for a stream of data.
 */
interface StreamSigner {
    /**
     * Initialize the implementing object for signing.
     *
     * @param privateKey The private key of the identity whose signature is going to be generated.
     */
    // fun init(privateKey: PrivateKey)

    /**
     * Updates the computed data digest read from the underlying stream, using the specified
     * array of bytes, starting at the specified offset.
     *
     * @param bytes The data to use to update the digest with.
     * @param offset The offset to start from in the array of bytes.
     * @param len The number of bytes to use, starting at offset.
     */
    // fun update(bytes: ByteArray, offset: Int, len: Int)

    // fun update(bytes: ByteArray) = update(bytes, 0, bytes.size)

    // fun sign(): ECDSASignature
}

/**
 * An interface for providing cryptographic signature verification for a stream of data.
 */
/*
interface StreamSignatureVerifier {
    fun init(publicKey: PublicKey)
    fun update(bytes: ByteArray, offset: Int, len: Int)
    fun update(bytes: ByteArray) = update(bytes, 0, bytes.size)
    fun verify(signature: ECDSASignature): Boolean
}
*/
