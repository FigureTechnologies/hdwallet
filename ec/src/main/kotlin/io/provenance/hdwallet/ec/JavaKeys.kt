package io.provenance.hdwallet.ec

import io.provenance.hdwallet.common.PrivateKey
import io.provenance.hdwallet.common.PublicKey
import io.provenance.hdwallet.common.toBigInteger
import io.provenance.hdwallet.ec.bc.toCurvePoint
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.spec.ECParameterSpec
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

private fun ECParameterSpec.toCurve() = Curve(n, g.toCurvePoint(), curve)

fun JavaPublicKey.toECPublicKey(): PublicKey {
    val bcec = requireNotNull(this as? BCECPublicKey) { "key type invalid" }
    return PublicKey(bcec.q.getEncoded(true).toBigInteger(), bcec.parameters.toCurve())
}

fun JavaPrivateKey.toECPrivateKey(): PrivateKey {
    val bcec = requireNotNull(this as? BCECPrivateKey) { "key type invalid" }
    return PrivateKey(bcec.d, bcec.parameters.toCurve())
}
