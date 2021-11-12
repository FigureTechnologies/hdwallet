package io.provenance.hdwallet.ec

import io.provenance.hdwallet.ec.bc.toCurve
import io.provenance.hdwallet.ec.bc.toCurvePoint
import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.spec.ECParameterSpec as BCECParameterSpec
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.spec.ECParameterSpec
import java.security.spec.EllipticCurve
import java.util.Enumeration

class CurvePoint(val ecPoint: ECPoint) {
    val x: BigInteger = ecPoint.xCoord.toBigInteger()
    val y: BigInteger = ecPoint.yCoord.toBigInteger()
    val isInfinity: Boolean = ecPoint.isInfinity

    fun encoded(compressed: Boolean = false): ByteArray = ecPoint.getEncoded(compressed)
    fun mul(n: BigInteger): CurvePoint = ecPoint.multiply(n).toCurvePoint()
    fun add(n: CurvePoint): CurvePoint = ecPoint.add(n.ecPoint).toCurvePoint()
    fun normalize(): CurvePoint = ecPoint.normalize().toCurvePoint()

    fun toJavaECPoint(): java.security.spec.ECPoint =
        java.security.spec.ECPoint(x, y)
}

data class NamedCurve(val name: String, val curve: Curve)

data class Curve(val n: BigInteger, val g: CurvePoint, private val ecCurve: ECCurve) {
    val ecDomainParameters: ECDomainParameters = ECDomainParameters(ecCurve, g.ecPoint, n)

    fun decodePoint(data: ByteArray): CurvePoint = ecCurve.decodePoint(data).toCurvePoint()
    fun createPoint(x: BigInteger, y: BigInteger): CurvePoint = ecCurve.createPoint(x, y).toCurvePoint()

    fun publicFromPrivate(privateKey: BigInteger): BigInteger {
        val point = fpcMul(privateKey)
        val encoded = point.encoded(false)
        return BigInteger(1, encoded.copyOfRange(1, encoded.size))
    }

    fun toJavaEllipticCurve(): EllipticCurve =
        EC5Util.convertCurve(ecCurve, ecDomainParameters.seed)

    private fun fpcMul(pk: BigInteger): CurvePoint {
        val postProcessedPrivateKey =
            if (pk.bitLength() > n.bitLength()) pk.mod(n)
            else pk
        return FixedPointCombMultiplier().multiply(g.ecPoint, postProcessedPrivateKey).toCurvePoint()
    }

    companion object {
        fun lookup(name: String): Curve = CustomNamedCurves.getByName(name).toCurve()
    }
}


val secp256k1Curve = Curve.lookup("secp256k1")
val secp256r1Curve = Curve.lookup("secp256r1")

// Provenance defaults to the secp256k1 EC curve for keys and signatures.
val CURVE = secp256k1Curve

fun PublicKey.deriveCurve(): NamedCurve = curve.ecParameterSpec.deriveCurve()
fun PrivateKey.deriveCurve(): NamedCurve = curve.ecParameterSpec.deriveCurve()

private fun ECParameterSpec.deriveCurve(): NamedCurve {
    val matchesSpec: (Pair<String, X9ECParameters>) -> Boolean = { (_, p) ->
        p.n == order &&
                p.h == cofactor.toBigInteger() &&
                p.g == generator &&
        p.curve == EC5Util.convertCurve(curve)
    }

    data class ECLookup(private val enumeration: Enumeration<Any>, val block: (String) -> X9ECParameters?) {
        val names: Sequence<String> = enumeration.asSequence().map { it as String }
        val resolved: Sequence<Pair<String, X9ECParameters>> = names.map { it to ECNamedCurveTable.getByName(it) }
    }

    val enc = ECLookup(ECNamedCurveTable.getNames(), ECNamedCurveTable::getByName)
    val cnc = ECLookup(CustomNamedCurves.getNames(), CustomNamedCurves::getByName)

    val p = (enc.resolved + cnc.resolved).firstOrNull(matchesSpec)
        ?: throw GeneralSecurityException("Could not find curve for params:$this")

    return NamedCurve(p.first, p.second.toCurve())
}

private val Curve.ecParameterSpec: ECParameterSpec
    get() = ECParameterSpec(toJavaEllipticCurve(), g.toJavaECPoint(), n, ecDomainParameters.h.toInt())

private val java.security.PrivateKey.ecParameters: ECParameterSpec
    get() = when (this) {
        is java.security.interfaces.ECPrivateKey -> params
        is org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey -> params
        else -> throw IllegalArgumentException("requires ec or bcec private key")
    }
