package io.provenance.signer

import io.provenance.ec.PrivateKey
import io.provenance.ec.PublicKey
import io.provenance.ec.toDomainParams
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator

/**
 *
 */
open class BCECSigner : SignAndVerify {
    private fun <T> signer(fn: ECDSASigner.() -> T): T {
        return ECDSASigner(HMacDSAKCalculator(SHA256Digest())).fn()
    }

    override fun sign(privateKey: PrivateKey, payload: ByteArray): ECDSASignature {
        val params = ECPrivateKeyParameters(privateKey.key, privateKey.curve.toDomainParams())
        val (r, s) = signer {
            init(true, params)
            generateSignature(payload)
        }
        return ECDSASignature(r, s)
    }

    override fun verify(publicKey: PublicKey, data: ByteArray, signature: ECDSASignature): Boolean {
        val params = ECPublicKeyParameters(publicKey.point(), publicKey.curve.toDomainParams())
        return signer {
            init(false, params)
            verifySignature(data, signature.r, signature.s)
        }
    }
}
