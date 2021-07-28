package io.provenance.ec

import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

fun X9ECParameters.toCurve(name: String): Curve = Curve(name, curve, n, h, g, seed)
fun Curve.toDomainParams(): ECDomainParameters = ECDomainParameters(c, g, n, h, seed)

data class Curve(val name: String, val c: ECCurve, val n: BigInteger, val h: BigInteger, val g: ECPoint, val seed: ByteArray?) {
    companion object {
        fun lookup(name: String): Curve = CustomNamedCurves.getByName(name).toCurve(name)
    }
}

val secp256k1Curve = Curve.lookup("secp256k1")
val secp256r1Curve = Curve.lookup("secp256r1")

// Provenance defaults to the secp256k1 EC curve for keys and signatures.
val CURVE = secp256k1Curve
