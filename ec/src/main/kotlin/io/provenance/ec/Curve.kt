package io.provenance.ec

import org.bouncycastle.crypto.ec.CustomNamedCurves

val secp256k1CurveParams = CustomNamedCurves.getByName("secp256k1")
val secp256r1CurveParams = CustomNamedCurves.getByName("secp256r1")

// Provenance defaults to the secp256k1 EC curve for keys and signatures.
val CURVE = secp256k1CurveParams
