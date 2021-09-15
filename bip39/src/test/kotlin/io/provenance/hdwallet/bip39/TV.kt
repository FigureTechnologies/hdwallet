package io.provenance.hdwallet.bip39

import org.apache.commons.codec.binary.Hex

data class TV(
    val entropy: String,
    val mnemonic: String,
    val passphrase: String,
    val seed: String,
    val bip32_xprv: String
)

fun ByteArray.hex(): String = Hex.encodeHexString(this, true)
fun String.unhex(): ByteArray = Hex.decodeHex(this)
