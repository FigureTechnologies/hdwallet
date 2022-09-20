package tech.figure.hdwallet.ec.extensions

import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.CurvePoint
import java.math.BigInteger
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey
import java.security.interfaces.ECPublicKey as JavaECPublicKey

fun X9ECParameters.toCurve(): Curve = Curve(n, g.toCurvePoint(), curve)

fun ECPoint.toCurvePoint(): CurvePoint = CurvePoint(this)

fun ECParameterSpec.toCurve() = Curve(n, g.toCurvePoint(), curve)

/**
 * Convert a BouncyCastle [BCECPublicKey] to a [Pair] of ([BigInteger], [Curve]).
 *
 * See kethereum's EllipticCurveKeyPairGenerator for conversion of [JavaECPublicKey] > [BCECPublicKey] > [org.kethereum.model.PublicKey]]
 *
 * @param compressed True if the encoding of the key should be compressed before being packed into a [BigInteger]
 *
 * @return Pair<BigInteger, Curve>
 */
fun BCECPublicKey.toBigIntegerPair(compressed: Boolean = false): Pair<BigInteger, Curve> =
    Pair(BigInteger(1, q.getEncoded(compressed)), parameters.toCurve())

/**
 * Convert a BouncyCastle [BCECPublicKey] to a [Pair] of ([BigInteger], [Curve]).
 *
 * See kethereum's EllipticCurveKeyPairGenerator for conversion of [JavaECPublicKey] > [BCECPublicKey] > [org.kethereum.model.PublicKey]]
 *
 * @return Pair<BigInteger, Curve>
 */
fun BCECPrivateKey.toBigIntegerPair(): Pair<BigInteger, Curve> = Pair(d, parameters.toCurve())

/**
 * Interprets a byte array as an X.509 encoded elliptic curve (EC) public key.
 *
 * @return A reconstructed [JavaPublicKey].
 */
fun ByteArray.asJavaX509ECPublicKey(): JavaPublicKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePublic(X509EncodedKeySpec(this))

/**
 * Interprets a byte array as an X.509 encoded elliptic curve (EC) public key.
 *
 * @return A reconstructed [JavaPrivateKey].
 */
fun ByteArray.asJavaPKCS8ECPrivateKey(): JavaPrivateKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePrivate(PKCS8EncodedKeySpec(this))