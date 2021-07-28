package io.provenance.signer

import io.provenance.base16.base16Encode
import io.provenance.bip32.toRootKey
import io.provenance.bip39.MnemonicWords
import io.provenance.ec.CURVE
import io.provenance.hashing.sha256

val paths = mapOf(
    "test" to "m/44'/1'/0'/0/0'",
    "prod" to "m/44'/505'/0'/0/0"
)

private val mnemonic =
    MnemonicWords.of("transfer hip shaft equip make believe desert logic ginger wine album citizen emotion laptop path what april lab napkin bulk silent artefact injury tag")
private val root = mnemonic.toSeed("".toCharArray()).toRootKey(curveParams = CURVE)

fun main() {
    val hash = "test".toByteArray().sha256()
    val sig1 = BCECSigner().sign(root.keyPair.privateKey, hash)
//        val sig2 = JsseECSigner().sign(key.keyPair.privateKey.key, hash)

    println(sig1)
    println(sig1.encodeAsBTC().base16Encode())
//        println(sig2)
}
