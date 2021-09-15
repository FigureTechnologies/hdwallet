package io.provenance.hdwallet.signer

//
//fun FiniteField.toECField(): ECField = ECFieldFp(characteristic)
//
//fun Curve.toECCurve(): EllipticCurve =
//    EllipticCurve(
//        c.field.toECField(),
//        c.a.toBigInteger(),
//        c.b.toBigInteger(),
//        seed
//    )
//
//fun ECPoint.toECPoint(): java.security.spec.ECPoint =
//    java.security.spec.ECPoint(
//        xCoord.toBigInteger(),
//        yCoord.toBigInteger()
//    )
//
//class JsseECSigner : SignAndVerify {
//    override fun sign(privateKey: PrivateKey, payload: ByteArray): ECDSASignature {
//        val kf = KeyFactory.getInstance("EC")
//        val spec = ECParameterSpec(
//            privateKey.curve.toECCurve(),
//            privateKey.curve.g.toECPoint(),
//            privateKey.curve.n,
//            privateKey.curve.h.toInt(),
//        )
//
//        val p = kf.generatePrivate(ECPrivateKeySpec(privateKey.key, spec))
//        val sig = Signature.getInstance("SHA256withECDSA").let {
//            it.initSign(p)
//
//            val out = ByteArray(64)
//            it.sign(out, 0, 64)
//            return@let out
//        }
//        return ECDSASignature.decodeAsBTC(sig, privateKey.curve)
//    }
//
//    override fun verify(publicKey: PublicKey, data: ByteArray, signature: ECDSASignature): Boolean {
//        val kf = KeyFactory.getInstance("EC")
//        val p = kf.generatePublic(X509EncodedKeySpec(publicKey.compressed()))
//        return Signature.getInstance("SHA256withECDSA").let {
//            it.initVerify(p)
//            it.verify(signature.encodeAsBTC())
//        }
//    }
//}
