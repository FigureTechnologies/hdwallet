package io.provenance.hdwallet.ec

import io.provenance.hdwallet.common.bc.registerBouncyCastle
import io.provenance.hdwallet.ec.extensions.toBCECPrivateKey
import io.provenance.hdwallet.ec.extensions.toBCECPublicKey
import io.provenance.hdwallet.ec.extensions.toBigIntegerPair
import io.provenance.hdwallet.ec.extensions.toECKeyPair
import io.provenance.hdwallet.ec.extensions.toECPrivateKey
import io.provenance.hdwallet.ec.extensions.toECPublicKey
import io.provenance.hdwallet.ec.extensions.toJavaECKeyPair
import io.provenance.hdwallet.ec.extensions.toJavaECPrivateKey
import io.provenance.hdwallet.ec.extensions.toJavaECPublicKey
import io.provenance.hdwallet.ec.extensions.toKeyPair
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

class TestKeyConversion {

    private val seed: ByteArray = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
    private val random = SecureRandom(seed)

    private fun createECKeyPair(keySize: Int = 256): Pair<JavaPublicKey, JavaPrivateKey> {
        val keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
        keyGen.initialize(keySize, random)
        val pair: KeyPair = keyGen.generateKeyPair()
        return Pair(pair.public, pair.private)
    }

    @BeforeEach
    fun setup() {
        registerBouncyCastle()
    }

    @Test
    @DisplayName("PublicKey: Java -> hdwallet -> Java")
    fun testJavaPublicKeyConversion() {
        val (originalJavaPubKey: JavaPublicKey, _) = createECKeyPair()
        val pubKey: PublicKey = originalJavaPubKey.toECPublicKey()
        val newJavaPubKey: JavaPublicKey = pubKey.toJavaECPublicKey()
        assert(originalJavaPubKey == newJavaPubKey)
    }

    @Test
    @DisplayName("PrivateKey : Java -> hdwallet -> Java")
    fun testJavaPrivateKeyConversion() {
        val (_, originalJavaPrivKey: JavaPrivateKey) = createECKeyPair()
        val privKey: PrivateKey = originalJavaPrivKey.toECPrivateKey()
        val newJavaPrivKey: JavaPrivateKey = privKey.toJavaECPrivateKey()
        assert(originalJavaPrivKey == newJavaPrivKey)
    }

    @Test
    @DisplayName("PublicKey :  Java -> BC -> BigInteger -> ByteArray -> hdwallet -> Java")
    fun testPublicKeyToBytesAndBack() {
        val (originalJavaPubKey: JavaPublicKey, _) = createECKeyPair()
        val bcecPubKey: BCECPublicKey = originalJavaPubKey.toBCECPublicKey()!!
        val (bigInt: BigInteger, curve: Curve) = bcecPubKey.toBigIntegerPair(compressed = false)
        val bytesPubKey: ByteArray = bigInt.toByteArray()
        val pubKey: PublicKey = PublicKey.fromBytes(bytesPubKey, curve)
        val newJavaPublicKey: JavaPublicKey = pubKey.toJavaECPublicKey()
        assert(originalJavaPubKey == newJavaPublicKey)
    }

    @Test
    @DisplayName("PrivateKey :  Java -> BC -> BigInteger -> ByteArray -> hdwallet -> Java")
    fun testPrivateKeyToBytesAndBack() {
        val (_, originalJavaPrivateKey: JavaPrivateKey) = createECKeyPair()
        val bcecPrivKey: BCECPrivateKey = originalJavaPrivateKey.toBCECPrivateKey()!!
        val (bigInt: BigInteger, curve: Curve) = bcecPrivKey.toBigIntegerPair()
        val bytesPrivKey: ByteArray = bigInt.toByteArray()
        val privKey: PrivateKey = PrivateKey.fromBytes(bytesPrivKey, curve)
        val newJavaPrivateKey: JavaPrivateKey = privKey.toJavaECPrivateKey()
        assert(originalJavaPrivateKey == newJavaPrivateKey)
    }

    @Test
    @DisplayName("Java keypair -> EC keypair -> Java keypair")
    fun testKeypairConversion() {
        val originalJavaKeyPair: KeyPair = createECKeyPair().toKeyPair()
        val ecKeyPair = originalJavaKeyPair.toECKeyPair()
        val convertedJavaKeyPair = ecKeyPair.toJavaECKeyPair()
        assert(originalJavaKeyPair.public == convertedJavaKeyPair.public)
        assert(originalJavaKeyPair.private == convertedJavaKeyPair.private)
    }
}
