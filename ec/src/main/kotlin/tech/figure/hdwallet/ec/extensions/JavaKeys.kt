package tech.figure.hdwallet.ec.extensions

import tech.figure.hdwallet.ec.ECKeyPair
import tech.figure.hdwallet.ec.PrivateKey
import tech.figure.hdwallet.ec.PublicKey
import tech.figure.hdwallet.ec.bcecParameterSpec
import tech.figure.hdwallet.ec.ecParameterSpec
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.ECPublicKeySpec
import java.security.KeyPair as JavaKeyPair
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
 * @return [BCECPublicKey] if the underlying key is a subclass of [BCECPublicKey], or null if otherwise.
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
fun JavaPublicKey.toECPublicKey(): PublicKey {
    val bcec = requireNotNull(toBCECPublicKey()) { "key type invalid: not EC" }
    val q = bcec.q.getEncoded(false)
    val curve = bcec.parameters.toCurve()
    // Ethereum does not use encoded public keys like bitcoin - see
    // https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm for details
    // Additionally, as the first bit is a constant prefix (0x04) we ignore this value

    // In Bitcoin, public keys are either compressed or uncompressed. Compressed public keys are 33 bytes,
    // consisting of a prefix either 0x02 or 0x03, and a 256-bit integer called x.
    // The older uncompressed keys are 65 bytes, consisting of constant prefix (0x04),
    // followed by two 256-bit integers called x and y (2 * 32 bytes).
    val bytes = BigInteger(1, q.copyOfRange(1, q.size))

    return PublicKey(bytes, curve)
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

/**
 * Convert a hdwallet elliptic curve keypair into a [JavaKeyPair].
 *
 * @return The converted Java [JavaKeyPair].
 */
fun ECKeyPair.toJavaECKeyPair(): JavaKeyPair = JavaKeyPair(publicKey.toJavaECPublicKey(), privateKey.toJavaECPrivateKey())

/**
 * Convert a Java [JavaKeyPair] into an hdwallet [ECKeyPair].
 *
 * @return The converted hdwallet [ECKeyPair].
 */
fun JavaKeyPair.toECKeyPair(): ECKeyPair = ECKeyPair(private.toECPrivateKey(), public.toECPublicKey())

/**
 * Convert a [Pair<JavaPublicKey, JavaPrivateKey>] to a [JavaKeyPair].
 *
 * @return [JavaKeyPair]
 */
fun Pair<JavaPublicKey, JavaPrivateKey>.toKeyPair(): JavaKeyPair = JavaKeyPair(first, second)

/**
 * Convert a [JavaKeyPair] to a [Pair] of <[JavaPublicKey], [JavaPrivateKey]>.
 *
 * @return A [Pair] of public, private keys.
 */
fun JavaKeyPair.toPair(): Pair<JavaPublicKey, JavaPrivateKey> = Pair(public, private)
