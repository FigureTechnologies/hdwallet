package io.provenance.hdwallet.ec.bc

import io.provenance.hdwallet.ec.Curve
import io.provenance.hdwallet.ec.CurvePoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.math.ec.ECPoint

fun X9ECParameters.toCurve(): Curve = Curve(n, g.toCurvePoint(), curve)
fun ECPoint.toCurvePoint(): CurvePoint = CurvePoint(this)

