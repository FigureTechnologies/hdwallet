package io.provenance.hdwallet.signer

import io.provenance.hdwallet.ec.PrivateKey
import io.provenance.hdwallet.ec.PublicKey
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.X509EncodedKeySpec

class JsseECSigner(private val secureRandom: SecureRandom) : SignAndVerify {
    override fun sign(privateKey: PrivateKey, payload: ByteArray): ECDSASignature {
        val kf = KeyFactory.getInstance("EC")
        val spec = ECParameterSpec(
            EC5Util.convertCurve(
                privateKey.curve.ecDomainParameters.curve,
                privateKey.curve.ecDomainParameters.seed
            ),
            EC5Util.convertPoint(privateKey.curve.g.ecPoint),
            privateKey.curve.n,
            privateKey.curve.ecDomainParameters.h.toInt(),
        )

        val prvkey = kf.generatePrivate(ECPrivateKeySpec(privateKey.key, spec))
        val sig = Signature.getInstance("SHA256withECDSA", "BC").let {
            // Force deterministic signing with null secureRandom.
            val dsakCalculator = HMacDSAKCalculator(SHA256Digest())
            it.initSign(prvkey, )
            it.update(payload)
            return@let it.sign()
        }
        return ECDSASignature.decodeAsDER(sig, privateKey.curve)
    }

    override fun verify(publicKey: PublicKey, data: ByteArray, signature: ECDSASignature): Boolean {
        val kf = KeyFactory.getInstance("EC")
        val p = kf.generatePublic(X509EncodedKeySpec(publicKey.compressed()))
        return Signature.getInstance("SHA256withECDSA", "BC").let {
            it.initVerify(p)
            it.verify(signature.encodeAsBTC())
        }
    }
}
