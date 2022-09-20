package tech.figure.hdwallet.ec

import tech.figure.hdwallet.common.bc.registerBouncyCastle
import tech.figure.hdwallet.ec.extensions.toBCECPrivateKey
import tech.figure.hdwallet.ec.extensions.toBCECPublicKey
import tech.figure.hdwallet.ec.extensions.toBigIntegerPair
import tech.figure.hdwallet.ec.extensions.toECKeyPair
import tech.figure.hdwallet.ec.extensions.toECPrivateKey
import tech.figure.hdwallet.ec.extensions.toECPublicKey
import tech.figure.hdwallet.ec.extensions.toJavaECKeyPair
import tech.figure.hdwallet.ec.extensions.toJavaECPrivateKey
import tech.figure.hdwallet.ec.extensions.toJavaECPublicKey
import tech.figure.hdwallet.ec.extensions.toKeyPair
import tech.figure.hdwallet.ec.util.createECKeyPair
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.KeyPair
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

class TestKeyConversion {

    init {
        registerBouncyCastle()
    }

    private val fixtureJavaKeys = createECKeyPair()

    private val fixturePublicKey: PublicKey =
        PublicKey(
            BigInteger("11017269121487925856270788060891642208257298925476974195446871359898252955554852665247892301558653002614431352541933167109765806198072387563714207974183173"),
            DEFAULT_CURVE
        )

    @BeforeEach
    fun setup() {
        registerBouncyCastle()
    }

    @Test
    @DisplayName("PublicKey: hdwallet -> Java -> hdwallet")
    fun testECPublicKeyConversion() {
        val javaPublicKey = fixturePublicKey.toJavaECPublicKey()
        val recreatedPublicKey = javaPublicKey.toECPublicKey()
        assert(fixturePublicKey.key == recreatedPublicKey.key)
    }

    @Test
    @DisplayName("PublicKey: hdwallet compressed -> hdwallet decompressed")
    fun testECPublicKeyConversionCompressed() {
        // Check the encoding
        val encodedDecompressedPublicKey: PublicKey = PublicKey(
            decompressPublicKey(fixturePublicKey.compressed(), fixturePublicKey.curve, encode = true),
            fixturePublicKey.curve
        )
        val encodedIntBytes: ByteArray = encodedDecompressedPublicKey.key.toByteArray()
        assert(encodedIntBytes.size == 65)
        assert(encodedIntBytes[0] == 0x04.toByte())

        val unencodedDecompressedPublicKey = PublicKey(
            decompressPublicKey(fixturePublicKey.compressed(), fixturePublicKey.curve),
            fixturePublicKey.curve
        )

        // After decompression, the compressed key should equal original BigInteger key:
        assert(unencodedDecompressedPublicKey.key == fixturePublicKey.key)
    }

    @Test
    @DisplayName("PublicKey: Java -> hdwallet -> Java")
    fun testJavaPublicKeyConversion() {
        val (originalJavaPubKey: JavaPublicKey, _) = fixtureJavaKeys
        val pubKey: PublicKey = originalJavaPubKey.toECPublicKey()
        val newJavaPubKey: JavaPublicKey = pubKey.toJavaECPublicKey()
        assert(originalJavaPubKey == newJavaPubKey)
    }

    @Test
    @DisplayName("PrivateKey : Java -> hdwallet -> Java")
    fun testJavaPrivateKeyConversion() {
        val (_, originalJavaPrivKey: JavaPrivateKey) = fixtureJavaKeys
        val privKey: PrivateKey = originalJavaPrivKey.toECPrivateKey()
        val newJavaPrivKey: JavaPrivateKey = privKey.toJavaECPrivateKey()
        assert(originalJavaPrivKey == newJavaPrivKey)
    }

    @Test
    @DisplayName("PublicKey :  Java -> BC -> BigInteger -> ByteArray -> hdwallet -> Java")
    fun testPublicKeyToBytesAndBack() {
        val (originalJavaPubKey: JavaPublicKey, _) = fixtureJavaKeys
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
        val (_, originalJavaPrivateKey: JavaPrivateKey) = fixtureJavaKeys
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
        val originalJavaKeyPair: KeyPair = fixtureJavaKeys.toKeyPair()
        val ecKeyPair = originalJavaKeyPair.toECKeyPair()
        val convertedJavaKeyPair = ecKeyPair.toJavaECKeyPair()
        assert(originalJavaKeyPair.public == convertedJavaKeyPair.public)
        assert(originalJavaKeyPair.private == convertedJavaKeyPair.private)
    }
}
