package io.provenance.hdwallet.ec.extensions

import io.provenance.hdwallet.ec.Curve
import io.provenance.hdwallet.ec.CurvePoint
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.math.ec.ECPoint
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

fun X9ECParameters.toCurve(): Curve = Curve(n, g.toCurvePoint(), curve)

fun ECPoint.toCurvePoint(): CurvePoint = CurvePoint(this)

fun ECParameterSpec.toCurve() = Curve(n, g.toCurvePoint(), curve)

/**
 * Interprets a byte array as an X.509 encoded elliptical curve (EC) public key.
 *
 * @return A reconstructed [JavaPublicKey].
 */
fun ByteArray.asJavaX509ECPublicKey(): JavaPublicKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePublic(X509EncodedKeySpec(this))

/**
 * Interprets a byte array as an X.509 encoded elliptical curve (EC) public key.
 *
 * @return A reconstructed [JavaPrivateKey].
 */
fun ByteArray.asJavaPKCS8ECPrivateKey(): JavaPrivateKey =
    KeyFactory
        .getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        .generatePrivate(PKCS8EncodedKeySpec(this))