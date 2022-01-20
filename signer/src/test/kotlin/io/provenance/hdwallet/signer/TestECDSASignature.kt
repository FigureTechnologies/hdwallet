package io.provenance.hdwallet.signer

import io.provenance.hdwallet.bip32.ExtKey
import io.provenance.hdwallet.bip32.toRootKey
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.common.bc.registerBouncyCastle
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.ec.extensions.toECPrivateKey
import io.provenance.hdwallet.ec.extensions.toECPublicKey
import io.provenance.hdwallet.ec.secp256k1Curve
import io.provenance.hdwallet.ec.secp256r1Curve
import io.provenance.hdwallet.encoding.base16.base16Decode
import io.provenance.hdwallet.encoding.base16.base16Encode
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.SignatureException
import java.util.Arrays
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

object paths {
    val testnet = "m/44'/1'/0'/0/0'"
    val mainnet = "m/44'/505'/0'/0/0"
}

interface TestECDSASignature {
    fun signer(): SignAndVerify
}

val mnemonic = MnemonicWords.of(
    "defense legal stem absorb hurdle physical prosper review process primary exist camera"
)
val seed = mnemonic.toSeed("".toCharArray())
val payload = "test".toByteArray()
val payloadHash = payload.sha256()

val expected = mapOf(
    secp256k1Curve to ExpectedSigs(
        ExpectedSig(
            "103983774820643500215082854519765665317236078679902989525656367220645751138099".toBigInteger(),
            "82659495478440068823917482899087829579900564952477345264247370968127761886159".toBigInteger(),
            "e5e4bb1b5d409837a48d89476585271072bd949c36c41d51b6079d9dfb8fa7334940622d962584da6a6465111492aa412da5552636eea1caf221f6fc827d1572".base16Decode(),
        ),

        ExpectedSig(
            "95801138147837954606004188261328606299891675155763289432154373623867774496927".toBigInteger(),
            "14031748757374300434682348731734333112132253375132969433918854158158678614077".toBigInteger(),
            "d3cd85cc9bb04f2b1fff99bacc7ddab9c871e24a3c192ae1268f8914f776709f1f05b037f520e83589ec530a70e2768e44c62eda941d56c4f6f3225d7442603d".base16Decode(),
        ),

        ExpectedSig(
            "17658101104089426292805451403999513160531797433477978227889304996774154117402".toBigInteger(),
            "78418590613312569024886558300198676180512285136998342380024508185675425727015".toBigInteger(),
            "270a218c3f5b676eb1b68673ed1d8f68186c8e952340abda6ff00c1d893ca11a52a0a68db0497c705e990f0972df96f6e49ff89b85b279494ddaa8573ba4ff1a".base16Decode()
        ),
    ),

    secp256r1Curve to ExpectedSigs(
        ExpectedSig(
            "98107675753390773036640878690826125641577492299606982091785407557416771397742".toBigInteger(),
            "45713286138739669004318608142584199971787212195386045070452505313279266326904".toBigInteger(),
            "d8e6fa07575baba76cfbd6ab6b71501951e48393640d97726b991e9f9ec0286e6510cd9523b473e683be4a95d78d3d26ef738967ccda57bc509ba61058450978".base16Decode(),
        ),

        ExpectedSig(
            "107341802698941283953223552902597426104572978003292539605476300632902874056470".toBigInteger(),
            "111272204444066855090766866392560775989219971564888385023157275326163560168616".toBigInteger(),
            "ed514eabf7c76a7fc4f84bdba5c11062c6cc0696abbcf4df1564da3000aa471609fe2a04c15421444e16ef9777b4fe929eb65e73631047c5616a971166224499".base16Decode(),
        ),

        ExpectedSig(
            "20727226463115513394457175384497173873773562814282725735552832555772132551858".toBigInteger(),
            "66274330211786722681738727591084814804047669008330573656207483100894184867443".toBigInteger(),
            "2dd331c6d637f79ae2aec915c027f39c38572da26f8b1cfd427aa8376fa96cb26d7a0ef3751fb2f157365d82f72edcf6d2409b4e055b2d67274859c16aca3ace".base16Decode(),
        ),
    ),
)

class TestItAll {
    private val randomSeed: ByteArray = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
    private val random = SecureRandom(randomSeed)

    private fun createECKeyPair(keySize: Int = 256): Pair<PublicKey, PrivateKey> {
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
    fun testRun() {
        data class Keys(val root: ExtKey) {
            val mainnet: ExtKey = root.childKey(paths.mainnet)
            val testnet: ExtKey = root.childKey(paths.testnet)
        }

        listOf(
            TestBCECDSASignature(),
            // TestJSSEECDSASignature(),
        ).map { test ->
            val signer = test.signer()

            listOf(
                "secp256k1" to secp256k1Curve,
                "secp256r1" to secp256r1Curve,
            ).map { (name, curve) ->
                val keys = Keys(seed.toRootKey(curve = curve))
                val ekp = expected[curve]!!

                val rootSig = signer.sign(keys.root.keyPair.privateKey, payloadHash)
                val testnetSig = signer.sign(keys.testnet.keyPair.privateKey, payloadHash)
                val mainnetSig = signer.sign(keys.mainnet.keyPair.privateKey, payloadHash)

                assertEquals(ekp.expectedRoot.r, rootSig.r, "${test.javaClass.simpleName} $name root r")
                assertEquals(ekp.expectedRoot.s, rootSig.s, "${test.javaClass.simpleName} $name root s")
                assertEquals(
                    ekp.expectedRoot.btc.base16Encode(),
                    rootSig.encodeAsBTC().base16Encode(),
                    "${test.javaClass.simpleName} $name root btc"
                )
                assertEquals(
                    true,
                    signer.verify(keys.root.keyPair.publicKey, payloadHash, rootSig),
                    "${test.javaClass.simpleName} $name root verify"
                )

                assertEquals(ekp.expectedTestnet.r, testnetSig.r, "${test.javaClass.simpleName} $name test r")
                assertEquals(ekp.expectedTestnet.s, testnetSig.s, "${test.javaClass.simpleName} $name test s")
                assertEquals(
                    ekp.expectedTestnet.btc.base16Encode(),
                    testnetSig.encodeAsBTC().base16Encode(),
                    "${test.javaClass.simpleName} $name test btc"
                )
                assertEquals(
                    true,
                    signer.verify(keys.testnet.keyPair.publicKey, payloadHash, testnetSig),
                    "${test.javaClass.simpleName} $name test verify"
                )

                assertEquals(ekp.expectedMainnet.r, mainnetSig.r, "${test.javaClass.simpleName} $name main r")
                assertEquals(ekp.expectedMainnet.s, mainnetSig.s, "${test.javaClass.simpleName} $name main s")
                assertEquals(
                    ekp.expectedMainnet.btc.base16Encode(),
                    mainnetSig.encodeAsBTC().base16Encode(),
                    "${test.javaClass.simpleName} $name main btc",
                )
                assertEquals(
                    true,
                    signer.verify(keys.mainnet.keyPair.publicKey, payloadHash, mainnetSig),
                    "${test.javaClass.simpleName} $name main verify"
                )
            }
        }
    }

    @Test
    fun testSignatureEncoding() {
        val keyPair = createECKeyPair()
        val publicKey = keyPair.first
        val privateKey = keyPair.second

        // We need to use ECDDSA (deterministic ECDSA) (only provided by Bouncy Castle)
        fun createSignature(): Signature = Signature.getInstance("SHA256withECDDSA")

        val signatureBytesGoodFormat: ByteArray = BCECSigner().run {
            sign(privateKey.toECPrivateKey(), payloadHash).encodeAsASN1DER()
        }

        val signatureBytesBadFormat: ByteArray = BCECSigner().run {
            sign(privateKey.toECPrivateKey(), payloadHash).encodeAsBTC()
        }

        // BTC encoding should fail:
        assertThrows<SignatureException> {
            createSignature().run {
                initVerify(publicKey)
                update(payload)
                verify(signatureBytesBadFormat)
            }
        }

        // DER encoding should succeed:
        assert(createSignature().run {
            initVerify(publicKey)
            update(payload)
            verify(signatureBytesGoodFormat)
        })

        // Generate signature bytes from Java security's Signature API:
        val javaSignBytes = createSignature().run {
            initSign(privateKey)
            update(payload)
            sign()
        }

        // If deterministic ECDSA is used, `javaSignBytes` and `signatureBytesGoodFormat` should be equal:
        assertTrue(Arrays.equals(javaSignBytes, signatureBytesGoodFormat))
    }
}

data class ExpectedSigs(
    val expectedRoot: ExpectedSig,
    val expectedTestnet: ExpectedSig,
    val expectedMainnet: ExpectedSig
)

data class ExpectedSig(val r: BigInteger, val s: BigInteger, val btc: ByteArray)

class TestBCECDSASignature : TestECDSASignature {
    override fun signer() = BCECSigner()
}
//
//class TestJSSEECDSASignature : TestECDSASignature {
//    override fun signer(): SignAndVerify = JsseECSigner()
//}
