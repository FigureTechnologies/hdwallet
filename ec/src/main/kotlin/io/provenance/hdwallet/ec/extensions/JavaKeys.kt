package io.provenance.hdwallet.ec.extensions

import io.provenance.hdwallet.ec.PrivateKey
import io.provenance.hdwallet.ec.PublicKey
import io.provenance.hdwallet.ec.bcecParameterSpec
import io.provenance.hdwallet.ec.ecParameterSpec
import java.security.KeyFactory
import java.security.spec.ECPublicKeySpec
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey
import java.security.interfaces.ECPrivateKey as JavaECPrivateKey
import java.security.interfaces.ECPublicKey as JavaECPublicKey

/**
 * Convert a Sun Security Provider [JavaECPublicKey] to a Bouncy Castle elliptic curve (EC) public key, [BCECPublicKey].
 *
 * @return [BCECPublicKey] The wrapped EC public key.
 */
fun JavaECPublicKey.toBCECPublicKey(): BCECPublicKey = BCECPublicKey(this, BouncyCastlePQCProvider.CONFIGURATION)

/**
 * Convert a Sun Security Provider [JavaPublicKey] to a Bouncy Castle elliptic curve (EC) public key, [BCECPublicKey].
 *
 * @return [BCECPublicKey] if the underlying key is a subclass of [ECPublicKey], or null if otherwise.
 */
fun JavaPublicKey.toBCECPublicKey(): BCECPublicKey? =
    when (this) {
        is JavaECPublicKey -> toBCECPublicKey()
        else -> null
    }

/**
 * Convert a Java cryptographic [JavaPublicKey] to a hdwallet library EC [PublicKey].
 *
 * @return The converted public key, [PublicKey].
 */
fun JavaPublicKey.toECPublicKey(compressed: Boolean = false): PublicKey {
    val bcec = requireNotNull(toBCECPublicKey()) { "key type invalid: not EC" }
    return PublicKey(bcec.q.getEncoded(compressed).toBigInteger(), bcec.parameters.toCurve())
}

/**
 * Convert an instance of a hdwallet library [PublicKey] to a Java cryptographic [PublicKey], using the
 * [BouncyCastleProvider] provider.
 *
 * @return The converted Java public key, [JavaPublicKey].
 */
fun PublicKey.toJavaECPublicKey(): JavaPublicKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePublic(ECPublicKeySpec(point().toJavaECPoint() /* w */, curve.ecParameterSpec))

/**
 * Convert an instance of a hdwallet library [PrivateKey] to a Java cryptographic [JavaPrivateKey], using the
 * [BouncyCastleProvider] provider.
 *
 * @return The converted Java private key, [JavaPrivateKey].
 */
fun PrivateKey.toJavaECPrivateKey(): JavaPrivateKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePrivate(ECPrivateKeySpec(key /* d */, curve.bcecParameterSpec))

/**
 * Convert a Sun Security Provider [JavaECPrivateKey] to a Bouncy Castle elliptic curve (EC) public key, [BCECPrivateKey].
 *
 * @return [BCECPrivateKey] The wrapped EC private key.
 */
fun JavaECPrivateKey.toBCECPrivateKey(): BCECPrivateKey = BCECPrivateKey(this, BouncyCastlePQCProvider.CONFIGURATION)

/**
 * Convert a Sun Security Provider [JavaPublicKey] to a Bouncy Castle elliptic curve (EC) public key, [BCECPublicKey].
 *
 * @return [BCECPublicKey] if the underlying key is a subclass of [JavaECPublicKey], or null if otherwise.
 */
fun JavaPrivateKey.toBCECPrivateKey(): BCECPrivateKey? =
    when (this) {
        is JavaECPrivateKey -> toBCECPrivateKey()
        else -> null
    }

/**
 * Convert a Java cryptographic [JavaPrivateKey] to a hdwallet library [PrivateKey].
 *
 * @return The converted private key, [PrivateKey].
 */
fun JavaPrivateKey.toECPrivateKey(): PrivateKey {
    val bcec = requireNotNull(toBCECPrivateKey()) { "key type invalid: not EC" }
    return PrivateKey(bcec.d, bcec.parameters.toCurve())
}
