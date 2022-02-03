package io.provenance.hdwallet.ec.util

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import org.bouncycastle.jce.provider.BouncyCastleProvider

private val seed: ByteArray = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

private val seededRandom = SecureRandom(seed)

fun createECKeyPair(keySize: Int = 256, random: SecureRandom = seededRandom): Pair<PublicKey, PrivateKey> {
    val keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
    keyGen.initialize(keySize, random)
    val pair: KeyPair = keyGen.generateKeyPair()
    return Pair(pair.public, pair.private)
}