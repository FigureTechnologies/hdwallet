//package com.figure.wallet.hdwallet
//
//import com.fasterxml.jackson.core.JsonGenerator.Feature
//import com.fasterxml.jackson.databind.JsonNode
//import com.fasterxml.jackson.databind.MapperFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.SerializationFeature
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bouncycastle.jce.spec.ECNamedCurveSpec
//import org.kethereum.crypto.CURVE
//import org.kethereum.crypto.api.ec.ECDSASignature
//import org.kethereum.crypto.impl.ec.EllipticCurveSigner
import java.math.BigInteger
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECFieldFp
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
//
//// A Zero value byte
//const val ZERO = 0x0.toByte()
//// The (Order / 2) (used for signature malleability checks -- to discard the negative signature point)
//private val HALF_CURVE_ORDER = CURVE.n.shiftRight(1)
//
///**
// * The secp256k1 Weierstraß (Koblitz) curve specification parameters.
// *
// * The curve is defined by:  𝑌^2 = 𝑋^3 + 𝑎𝑋 + 𝑏.  As 𝑎 is zero the simple form of 𝑌^2=𝑋^3 + 𝑏 is used.
// *
// * The following specifications are from "Standards for Efficient Cryptography" 2.4.1. http://www.secg.org/sec2-v2.pdf
// */
private val CURVESPEC = ECNamedCurveSpec(
    "secp256k1",
    // The elliptic curve domain parameters over Fp
    EllipticCurve(
        ECFieldFp(BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16)),
        BigInteger.ZERO, // a
        7.toBigInteger() // b
    ),
    // The base point G in uncompressed form
    ECPoint(
        BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16), // G.x
        BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16) // G.y
    ),
    // Finally the order n of G and the cofactor
    BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16), // n
    BigInteger.ONE // h
)
//
//// Test for values under 128 which indicate a 'narrow' char / high bit unset.
//fun Byte.isNarrow() = (this.toInt() and 0x80) != 0x80
//
//// Test for values over 128 which indicate a 'wide' char / have high bit set.
//fun Byte.isWide() = (this.toInt() and 0x80) == 0x80
//
//data class Fee(val amount: Array<Coin>, val gas: Int)
//data class Coin(val denom: String, val amount: Int)
//
///**
// * Message used for sending coins between accounts.
// */
//data class SendMessage(
//    val from_address: String,
//    val to_address: String,
//    val amount: Array<Coin>
//)
//
///**
// * Returns the signature in a strict DER format that does not include the appended hash type used in Bitcoin signatures
// */
//fun ECDSASignature.encodeAsDER(): ByteArray {
//    val rBytes = this.r.toByteArray()
//    require(rBytes[0].isNarrow()) { "R is not a canonical value, could be negative" }
//    // we support at most one 0 byte for padding which is required to prevent the interpretation of the sequence of
//    // bytes from being deemed "negative"
//    require(rBytes[0] != ZERO || (rBytes.size <= 1 || rBytes[1].isWide())) { "excessive padding" }
//
//    val sBytes = this.s.toByteArray()
//    require(sBytes[0].isNarrow()) { "S is not a canonical value, could be negative" }
//    require(sBytes[0] != ZERO || (sBytes.size <= 1 || sBytes[1].isWide())) { "excessive padding" }
//
//    val signature = ByteArray(6 + rBytes.size + sBytes.size)
//
//    signature.set(0, 0x30.toByte())
//    signature.set(1, (signature.size - 2).toByte())
//    signature.set(2, 0x02.toByte())
//    signature.set(3, rBytes.size.toByte())
//
//    System.arraycopy(rBytes, 0, signature, 4, rBytes.size)
//
//    signature.set(rBytes.size + 4, 0x02.toByte())
//    signature.set(rBytes.size + 5, sBytes.size.toByte())
//
//    System.arraycopy(sBytes, 0, signature, rBytes.size + 6, sBytes.size)
//
//    return signature
//}
//
///**
// * encodeAsBTC returns the ECDSA signature as a ByteArray of r || s,
// * where both r and s are encoded into 32 byte big endian integers.
// */
//fun ECDSASignature.encodeAsBTC(): ByteArray {
//    // Canonicalize - In order to remove malleability,
//    // we set s = curve_order - s, if s is greater than curve.Order() / 2.
//    var sigS = this.s
//    if (sigS > HALF_CURVE_ORDER) {
//        sigS = CURVE.n.subtract(sigS)
//    }
//
//    val sBytes = sigS.getUnsignedBytes()
//    val rBytes = this.r.getUnsignedBytes()
//
//    require(rBytes.size <= 32) { "cannot encode r into BTC Format, size overflow (${rBytes.size} > 32)" }
//    require(sBytes.size <= 32) { "cannot encode s into BTC Format, size overflow (${sBytes.size} > 32)" }
//
//    val signature = ByteArray(64)
//    // 0 pad the byte arrays from the left if they aren't big enough.
//    System.arraycopy(rBytes, 0, signature, 32 - rBytes.size, rBytes.size)
//    System.arraycopy(sBytes, 0, signature, 64 - sBytes.size, sBytes.size)
//
//    return signature
//}
//
///**
// * decodeAsBTC returns an ECDSASignature where the 64 byte array is divided
// * into r || s with each being a 32 byte big endian integer.
// */
//fun ByteArray.decodeAsBTC(): ECDSASignature {
//    require(this.size == 64) { "malformed BTC encoded signature, expected 64 bytes" }
//
//    val ecdsa = ECDSASignature(
//        BigInteger(1, this.dropLast(32).toByteArray()),
//        BigInteger(1, this.takeLast(32).toByteArray())
//    )
//
//    require(ecdsa.r < CURVE.n) { "signature R must be less than curve.N" }
//    require(ecdsa.s <= HALF_CURVE_ORDER) { "signature S must be less than (curve.N / 2)" }
//
//    return ecdsa
//}
//
///**
// * Parses the strict DER format (without the appended Bitcoin hash type) into an ECDSASignature
// */
//fun ByteArray.decodeAsDER(): ECDSASignature {
//    // 0x30 + <1-byte> + 0x02 + 0x01 + <byte> + 0x2 + 0x01 + <byte> = 8
//    val MIN_SIG_LENGTH = 8
//    require(this.size >= MIN_SIG_LENGTH) { "malformed signature: too short" }
//    require(this[0] == 0x30.toByte()) { "malformed signature: does not start with expected header byte" }
//
//    val sigLen = this[1].toInt()
//    require(!(sigLen + 2 > this.size || sigLen + 2 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//    require(this[2] == 0x02.toByte()) { "malformed signature: missing first byte marker for R" }
//
//    val rByteLen = this[3].toInt()
//    require(!(rByteLen + 7 > this.size || rByteLen + 7 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//
//    val rBytes = ByteArray(rByteLen)
//    System.arraycopy(this, 4, rBytes, 0, rByteLen)
//
//    // offset of S bytes is length of R bytes plus header, length, marker bytes
//    val sOffset = (rByteLen + 4)
//
//    val sByteLen = this[sOffset + 1].toInt()
//    require(!(sOffset + sByteLen + 2 > this.size || sByteLen + 2 < MIN_SIG_LENGTH)) { "malformed signature: bad length" }
//    require(this[sOffset] == 0x02.toByte()) { "malformed signature: missing first byte marker for S" }
//
//    val sBytes = ByteArray(sByteLen)
//    System.arraycopy(this, sOffset + 2, sBytes, 0, sByteLen)
//
//    require(rBytes[0].isNarrow()) { "R is not a canonical value, could be negative" }
//    require((rBytes[0] != ZERO) || (rBytes.size <= 1 || rBytes[1].isWide())) { "excessive padding" }
//
//    require(sBytes[0].isNarrow()) { "S is not a canonical value, could be negative" }
//    require((sBytes[0] != ZERO) || (sBytes.size <= 1 || sBytes[1].isWide())) { "excessive padding" }
//
//    val ecdsa = ECDSASignature(BigInteger(rBytes), BigInteger(sBytes))
//
//    require(ecdsa.r.signum() == 1) { "Signature R value must be positive" }
//    require(ecdsa.s.signum() == 1) { "Signature S value must be positive" }
//
//    require(ecdsa.r < CURVE.n) { "Signature R value must be less than curve N" }
//    require(ecdsa.s < CURVE.n) { "Signature S value must be less than curve N" }
//
//    return ecdsa
//}
//
///**
// * Computes a Cosmos SDK compliant signature using the digest bytes of the message to sign.
// */
//fun Account.signDigest(sha256Digest: ByteArray): ECDSASignature {
//    val s = EllipticCurveSigner()
//    // Signatures support two possible solutions (above/below halfway point in curve)... Canonical requires lower.
//    return s.sign(sha256Digest, this.getECKeyPair().privateKey.key, true)
//}
//
///**
// * Transforms a BigInteger into a JCE Compatible ECPrivateKey wrapper for secp256k1.
// */
//fun BigInteger.asECPrivateKey(): ECPrivateKey = JCECompatibleECPrivateKey(this)
//
///**
// * Transforms a BigInteger into a JCE Compatible ECPublicKey wrapper for secp256k1.
// */
//fun BigInteger.asECPublicKey(): ECPublicKey = JCECompatiblePublicKey(this)
//
///**
// * A light weight wrapper class to implement the ECPrivateKey interface
// */
//class JCECompatibleECPrivateKey(private val s: BigInteger) : ECPrivateKey {
//    override fun getAlgorithm() = "EC"
//    override fun getS() = s
//    override fun getEncoded() = s.toByteArray()
//    override fun getFormat() = "raw"
//    override fun getParams() = CURVESPEC
//}
//
///**
// * A light weight wrapper class to implement the ECPublicKey interface
// */
//class JCECompatiblePublicKey(private val p: BigInteger) : ECPublicKey {
//    override fun getAlgorithm() = "EC"
//    override fun getFormat() = "PKCS#8"
//    override fun getEncoded() = Account.compressedPublicKey(p)
//    override fun getParams() = CURVESPEC
//    override fun getW() = CURVE.decodePoint(Account.compressedPublicKey(p)).let { ECPoint(it.x, it.y) }
//}
//
///**
// * ObjectMapper.configureCanonical sets up Jackson JSON serialization in a RFC7159 compliant way.
// *
// * Cryptographic hashing used as the input for digital signatures relies on serialized json with sorted properties
// * and no insignificant whitespace among other idosyncracies.  In order to compute a message signature correctly
// * the serializer must use the canonical form for every JSON value.
// *
// * The most important two aspects of this configuration are orderered properties and maps by lexicographical order
// * of code points, and the removal of all insignificant whitespace.  The resulting JSON must be in UTF8 format with
// * exponential formatting of non-integers.
// */
//fun ObjectMapper.configureCanonical(): ObjectMapper = ObjectMapper()
//    .registerKotlinModule()
//    .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
//    .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
//    .enable(SerializationFeature.WRITE_ENUMS_USING_INDEX)
//    .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//    // This is NOT compliant with RFC7159 but it is required for Cosmos
//    .enable(Feature.WRITE_NUMBERS_AS_STRINGS)
//    // .disable(Feature.WRITE_NUMBERS_AS_STRINGS)
//    .disable(SerializationFeature.INDENT_OUTPUT)
//
//private object CanonicalJson {
//    val mapper = ObjectMapper().configureCanonical()
//}
//
//private val omWriter get() = CanonicalJson.mapper.writer()
//private val omReader get() = CanonicalJson.mapper.reader()
//
//private fun <T> T.asCanonicalJsonNode(): JsonNode = asCanonicalJsonBytes().let {
//    omReader.readTree(it.toString(Charsets.UTF_8)) ?: CanonicalJson.mapper.createObjectNode()
//}
//
//fun <T> T.asCanonicalJsonBytes() = omWriter.writeValueAsBytes(this)
//
///**
// * Returns the bytes from a BigInteger as an unsigned version by truncating a byte if needed.
// */
//fun BigInteger.getUnsignedBytes(): ByteArray {
//    val bytes = this.toByteArray()
//
//    if (bytes[0] == ZERO) {
//        return bytes.drop(1).toByteArray()
//    }
//
//    return bytes
//}
