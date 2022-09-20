package tech.figure.hdwallet.encoding.base58

import tech.figure.hdwallet.encoding.shadow.org.bitcoinj.core.Base58

fun ByteArray.base58Encode(): String = Base58.encode(this)

fun String.base58Decode(): ByteArray = Base58.decode(this)

fun ByteArray.base58EncodeChecked(): String = Base58.encodeChecked(this)

fun String.base58DecodeChecked(): ByteArray = Base58.decodeChecked(this)
