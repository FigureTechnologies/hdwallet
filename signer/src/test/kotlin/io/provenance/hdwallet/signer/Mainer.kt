package io.provenance.hdwallet.signer

import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.ec.PrivateKey
import io.provenance.hdwallet.ec.secp256k1Curve
import io.provenance.hdwallet.encoding.base16.base16Decode
import io.provenance.hdwallet.signer.TestJSSEECDSASignature.FixedSecureRandom
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.math.BigInteger
import java.security.Security

fun main() {
    Security.addProvider(BouncyCastleProvider())

    val key = PrivateKey.fromBytes("31a84594060e103f5a63eb742bd46cf5f5900d8406e2726dedfc61c7cf43ebad".base16Decode(), secp256k1Curve)
    val fsr = FixedSecureRandom(byteArrayOf(0x01))
    val bytes = ByteArray(10).also { fsr.nextBytes(it) }
    println(bytes.toList())


    val sig = "30440220132382ca59240c2e14ee7ff61d90fc63276325f4cbe8169fc53ade4a407c2fc802204d86fbe3bde6975dd5a91fdc95ad6544dcdf0dab206f02224ce7e2b151bd82ab".base16Decode()
    val payload = "9e5755ec2f328cc8635a55415d0e9a09c2b6f2c9b0343c945fbbfe08247a4cbe".base16Decode()
    val bcec = BCECSigner().sign(key, payload)
    val jsse = JsseECSigner(fsr).sign(key, payload)

    val kcalc = HMacDSAKCalculator(SHA256Digest())
    kcalc.init(key.curve.n, key.key, payload)
    val nextK = kcalc.nextK()

    println("nextK: " + nextK)

    println(ECDSASignature.decodeAsDER(sig, secp256k1Curve))
    println(bcec.r)
    println(bcec.s)
    println(jsse.r)
    println(jsse.s)

}

fun calculateE(n: BigInteger, message: ByteArray): BigInteger {
    val log2n = n.bitLength()
    val messageBitLength = message.size * 8
    var e = BigInteger(1, message)
    if (log2n < messageBitLength) {
        e = e.shiftRight(messageBitLength - log2n)
    }
    return e
}
