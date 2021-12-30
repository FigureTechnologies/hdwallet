package io.provenance.hdwallet.ec

import io.provenance.hdwallet.ec.bc.toCurvePoint
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import java.security.KeyFactory
import java.security.spec.ECPublicKeySpec
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

private fun ECParameterSpec.toCurve() = Curve(n, g.toCurvePoint(), curve)

/**
 * Convert a Java cryptographic [JavaPublicKey] to a hdwallet library [PublicKey].
 *
 * @return The converted public key, [PublicKey].
 */
fun JavaPublicKey.toECPublicKey(): PublicKey {
    val bcec = requireNotNull(this as? BCECPublicKey) { "key type invalid" }
    return PublicKey(bcec.q.getEncoded(true).toBigInteger(), bcec.parameters.toCurve())
}

/**
 * Convert a Java cryptographic [JavaPrivateKey] to a hdwallet library [PrivateKey].
 *
 * @return The converted private key, [PrivateKey].
 */
fun JavaPrivateKey.toECPrivateKey(): PrivateKey {
    val bcec = requireNotNull(this as? BCECPrivateKey) { "key type invalid" }
    return PrivateKey(bcec.d, bcec.parameters.toCurve())
}

/**
 * Convert an instance of a hdwallet library [PublicKey] to a Java cryptographic [PublicKey].
 *
 * @return The converted Java public key, [JavaPublicKey].
 */
fun PublicKey.toJavaECPublicKey(): JavaPublicKey =
    KeyFactory
        .getInstance("EC")
        .generatePublic(ECPublicKeySpec(point().toJavaECPoint(), curve.ecParameterSpec))

/**
 * Convert an instance of a hdwallet library [PrivateKey] to a Java cryptographic [JavaPrivateKey].
 *
 * @return The converted Java private key, [JavaPrivateKey].
 */
fun PrivateKey.toJavaPrivateKey(): JavaPrivateKey =
    KeyFactory
        .getInstance("EC")
        .generatePrivate(ECPrivateKeySpec(key, curve.bcecParameterSpec))

