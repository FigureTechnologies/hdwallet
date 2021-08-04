package io.provenance.base58

import io.provenance.shadow.org.bitcoinj.core.Base58

fun ByteArray.base58Encode() = Base58.encode(this)
fun String.base58Decode() = Base58.decode(this)
fun ByteArray.base58EncodeChecked() = Base58.encodeChecked(this)
fun String.base58DecodeChecked() = Base58.decodeChecked(this)
