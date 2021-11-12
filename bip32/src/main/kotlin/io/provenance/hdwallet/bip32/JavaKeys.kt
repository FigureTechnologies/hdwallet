package io.provenance.hdwallet.bip32

import io.provenance.hdwallet.ec.CURVE
import io.provenance.hdwallet.ec.Curve
import java.security.KeyFactory
import java.security.KeyPair
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec
import java.security.spec.EllipticCurve

fun Curve.toJavaEllipticCurve(): EllipticCurve = with(ecDomainParameters) {
    EllipticCurve({ curve.fieldSize }, curve.a.toBigInteger(), curve.b.toBigInteger(), seed)
}

fun ExtKey.toJavaKeyPair(curve: Curve = CURVE): KeyPair {
    val jcurve = curve.toJavaEllipticCurve()
    val ecParameterSpec = ECParameterSpec(jcurve, curve.g.toJavaECPoint(), curve.n, curve.ecDomainParameters.h.toInt())
    val spec = ECPrivateKeySpec(keyPair.privateKey.key, ecParameterSpec)
    val kf = KeyFactory.getInstance("EC")
    return KeyPair(kf.generatePublic(spec), kf.generatePrivate(spec))
}
