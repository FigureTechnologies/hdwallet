package io.provenance.hdwallet.ec

import io.provenance.hdwallet.ec.bc.toCurve
import io.provenance.hdwallet.ec.bc.toCurvePoint
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import java.math.BigInteger

class CurvePoint(val ecPoint: ECPoint) {
    val x: BigInteger = ecPoint.xCoord.toBigInteger()
    val y: BigInteger = ecPoint.yCoord.toBigInteger()
    val isInfinity: Boolean = ecPoint.isInfinity

    fun encoded(compressed: Boolean = false): ByteArray = ecPoint.getEncoded(compressed)
    fun mul(n: BigInteger): CurvePoint = ecPoint.multiply(n).toCurvePoint()
    fun add(n: CurvePoint): CurvePoint = ecPoint.add(n.ecPoint).toCurvePoint()
    fun normalize(): CurvePoint = ecPoint.normalize().toCurvePoint()
}

data class Curve(val n: BigInteger, val g: CurvePoint, private val ecCurve: ECCurve) {
    val ecDomainParameters: ECDomainParameters = ECDomainParameters(ecCurve, g.ecPoint, n)

    fun decodePoint(data: ByteArray): CurvePoint = ecCurve.decodePoint(data).toCurvePoint()
    fun createPoint(x: BigInteger, y: BigInteger): CurvePoint = ecCurve.createPoint(x, y).toCurvePoint()

    fun publicFromPrivate(privateKey: BigInteger): BigInteger {
        val point = fpcMul(privateKey)
        val encoded = point.encoded(false)
        return BigInteger(1, encoded.copyOfRange(1, encoded.size))
    }

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
