package io.provenance.bip32

// BIP32 defines several 4 byte encoding values to designate the purpose of the encoded extended key
// 4 byte: version bytes
//  * mainnet: 0x0488B21E public, 0x0488ADE4 private
//  * testnet: 0x043587CF public, 0x04358394 private
internal val xprv = byteArrayOf(0x04, 0x88.toByte(), 0xAD.toByte(), 0xE4.toByte())
internal val xpub = byteArrayOf(0x04, 0x88.toByte(), 0xB2.toByte(), 0x1E.toByte())
internal val tprv = byteArrayOf(0x04, 0x35.toByte(), 0x83.toByte(), 0x94.toByte())
internal val tpub = byteArrayOf(0x04, 0x35.toByte(), 0x87.toByte(), 0xCF.toByte())
