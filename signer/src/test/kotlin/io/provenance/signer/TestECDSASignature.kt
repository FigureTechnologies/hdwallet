package io.provenance.signer

import io.provenance.base16.base16Decode
import io.provenance.base16.base16Encode
import io.provenance.bip32.ExtKey
import io.provenance.bip32.toRootKey
import io.provenance.bip39.MnemonicWords
import io.provenance.ec.secp256k1Curve
import io.provenance.ec.secp256r1Curve
import io.provenance.hashing.sha256
import org.junit.Assert
import org.junit.Test
import java.math.BigInteger

object paths {
    val testnet = "m/44'/1'/0'/0/0'"
    val mainnet = "m/44'/505'/0'/0/0"
}

interface TestECDSASignature {
    fun signer(): SignAndVerify
}

val mnemonic = MnemonicWords.of(
    "transfer hip shaft equip make believe desert logic ginger wine album citizen emotion laptop path what april lab napkin bulk silent artefact injury tag"
)
val seed = mnemonic.toSeed("".toCharArray())
val payloadHash = "test".toByteArray().sha256()

val expected = mapOf(
    secp256k1Curve to ExpectedSigs(
        ExpectedSig(
            "57469986241562286946042025192141317461649010201239636784713669727108899997705".toBigInteger(),
            "369442288951583454686650909004329655345823929397184098658781431902709281662".toBigInteger(),
            "7f0edc0878a3649b7dcb0f09d12315d47d9f4a291bcf1eae7e920c31a9f3080900d118d01ae3696e787396abcdd5f0750f079e154db2572d45aabb38196cf37e".base16Decode(),
        ),

        ExpectedSig(
            "88006751095464836250625038057176603769929873888335827755536246189656928323643".toBigInteger(),
            "459111695588851989901226468387334385508409586134382378746116036623334786691".toBigInteger(),
            "c2920e50a734ec873b5ad74b62c10c543984d0152ecf4358929d537906d6883b0103d9174055a7f7a34eedfc5a909deada9da5d0b8bdab1a9c81464a62764683".base16Decode(),
        ),

        ExpectedSig(
            "25997571277705891019657786514392770885303289053033240120375063576688883654194".toBigInteger(),
            "43974628893527353777472159866324079997614690156858778939681894624087682319456".toBigInteger(),
            "397a1a98dcace839577a7aea3286d56347d992cb3ff1f69f8920b76e0b63de326138c207a9229f98db7dd07cc421771964e92e29e204339d67b4ef6de1b40460".base16Decode()
        ),
    ),

    secp256r1Curve to ExpectedSigs(
        ExpectedSig(
            "21753168909585361901698837299917205198687298947807233571855694015673834073431".toBigInteger(),
            "38086756602760523597282750086592933175057347672132956935554687218119963437449".toBigInteger(),
            "3017db7729ec71fc6d5fc0ec7fb8dcca6216c7ee1a93ddddb6ddbc554a4aed575434571e81c6e154f6af7465d34746539fb36ccc2648a74bfb8d60086869e989".base16Decode(),
        ),

        ExpectedSig(
            "112933647512762912047871241551058218772497767139512968382836247446722995039949".toBigInteger(),
            "24363831613179829191973208979191080568078032315851154663666738142116851715719".toBigInteger(),
            "f9ae2de7bd32a4d10d11c2ce7edb54769ed09debce9037c42ed568fc26442ecd35dd70a4bd3bba50d73f2783439ed21f2f7a3c0f7ca25a89dada77f4093b8e87".base16Decode(),
        ),

        ExpectedSig(
            "28683642256261352430572769683114565276283399679975521364881223354944273899039".toBigInteger(),
            "14621719776976667543605328675580890218993942448804809431108083757445229100621".toBigInteger(),
            "3f6a5dbecf77d692f05db2936d71f682ada1ea0ca429cba15f254ae90be9de1f205399a14dcc3cc5cc9cb548c726fd3625171904daeebc76cd6fb5e6b8bcbe4d".base16Decode(),
        ),
    ),
)

class TestItAll {
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

            listOf(secp256k1Curve, secp256r1Curve).map { curve ->
                val keys = Keys(seed.toRootKey(curve = curve))
                val ekp = expected[curve]!!

                val rootSig = signer.sign(keys.root.keyPair.privateKey, payloadHash)
                val testnetSig = signer.sign(keys.testnet.keyPair.privateKey, payloadHash)
                val mainnetSig = signer.sign(keys.mainnet.keyPair.privateKey, payloadHash)

                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} root r", ekp.expectedRoot.r, rootSig.r)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} root s", ekp.expectedRoot.s, rootSig.s)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} root btc", ekp.expectedRoot.btc.base16Encode(), rootSig.encodeAsBTC().base16Encode())
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} root verify", true, signer.verify(keys.root.keyPair.publicKey, payloadHash, rootSig))

                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} test r", ekp.expectedTestnet.r, testnetSig.r)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} test s", ekp.expectedTestnet.s, testnetSig.s)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} test btc", ekp.expectedTestnet.btc.base16Encode(), testnetSig.encodeAsBTC().base16Encode())
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} test verify", true, signer.verify(keys.testnet.keyPair.publicKey, payloadHash, testnetSig))

                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} main r", ekp.expectedMainnet.r, mainnetSig.r)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} main s", ekp.expectedMainnet.s, mainnetSig.s)
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} main btc", ekp.expectedMainnet.btc.base16Encode(), mainnetSig.encodeAsBTC().base16Encode())
                Assert.assertEquals("${test.javaClass.simpleName} ${curve.name} main verify", true, signer.verify(keys.mainnet.keyPair.publicKey, payloadHash, mainnetSig))
            }
        }

    }
}

data class ExpectedSigs(val expectedRoot: ExpectedSig, val expectedTestnet: ExpectedSig, val expectedMainnet: ExpectedSig)
data class ExpectedSig(val r: BigInteger, val s: BigInteger, val btc: ByteArray)

class TestBCECDSASignature : TestECDSASignature {
    override fun signer() = BCECSigner()
}
//
//class TestJSSEECDSASignature : TestECDSASignature {
//    override fun signer(): SignAndVerify = JsseECSigner()
//}
