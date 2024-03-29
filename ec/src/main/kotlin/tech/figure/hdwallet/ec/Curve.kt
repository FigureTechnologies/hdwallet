package tech.figure.hdwallet.ec

import tech.figure.hdwallet.ec.extensions.toCurve
import tech.figure.hdwallet.ec.extensions.toCurvePoint
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger
import java.security.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECParameterSpec as BCECParameterSpec
import java.security.spec.EllipticCurve

/**
 * Defines an elliptic curve point.
 *
 * @property ecPoint The curve point.
 */
class CurvePoint internal constructor(val ecPoint: ECPoint) {
    val x: BigInteger = ecPoint.xCoord.toBigInteger()
    val y: BigInteger = ecPoint.yCoord.toBigInteger()
    val isInfinity: Boolean = ecPoint.isInfinity

    fun encoded(compressed: Boolean = false): ByteArray = ecPoint.getEncoded(compressed)
    fun mul(n: BigInteger): CurvePoint = ecPoint.multiply(n).toCurvePoint()
    fun add(n: CurvePoint): CurvePoint = ecPoint.add(n.ecPoint).toCurvePoint()
    fun normalize(): CurvePoint = ecPoint.normalize().toCurvePoint()
    fun toJavaECPoint(): java.security.spec.ECPoint = java.security.spec.ECPoint(x, y)
    fun toBCECPoint(curve: ECCurve): ECPoint = EC5Util.convertPoint(curve, toJavaECPoint())
}

/**
 * Defines an elliptic curve.
 *
 * @property n
 * @property g
 * @property ecCurve
 */
data class Curve internal constructor(val n: BigInteger, val g: CurvePoint, private val ecCurve: ECCurve) {
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

    fun toBCEllipticCurve(): ECCurve =
        EC5Util.convertCurve(toJavaEllipticCurve())

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

/**
 * SECP256K1 curve.
 *
 * See https://en.bitcoin.it/wiki/Secp256k1
 */
val secp256k1Curve: Curve = Curve.lookup("secp256k1")

/**
 * SECP256R1 curve.
 *
 * See:
 * - https://www.ietf.org/rfc/rfc5480.txt
 * - https://neuromancer.sk/std/secg/secp256r1
 * - https://www.johndcook.com/blog/2018/08/21/a-tale-of-two-elliptic-curves/
 */
val secp256r1Curve: Curve = Curve.lookup("secp256r1")

/**
 * Provenance defaults to the secp256k1 EC curve for keys and signatures.
 */
val DEFAULT_CURVE: Curve = secp256k1Curve

/**
 * Get the Java EC parameter spec from the [Curve].
 */
val Curve.ecParameterSpec: ECParameterSpec
    get() = ECParameterSpec(toJavaEllipticCurve(), g.toJavaECPoint(), n, ecDomainParameters.h.toInt())

/**
 * Get the BouncyCastle EC parameter spec from the [Curve].
 */
val Curve.bcecParameterSpec: BCECParameterSpec
    get() = toBCEllipticCurve().let { bcCurve -> BCECParameterSpec(bcCurve, g.toBCECPoint(bcCurve), n, ecDomainParameters.h) }
