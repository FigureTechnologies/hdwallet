package io.provenance.hdwallet.common.bc

import io.provenance.ec.Curve
import io.provenance.ec.CurvePoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.math.ec.ECPoint

fun X9ECParameters.toCurve(): Curve = Curve(n, g.toCurvePoint(), curve)
fun ECPoint.toCurvePoint(): CurvePoint = CurvePoint(this)

