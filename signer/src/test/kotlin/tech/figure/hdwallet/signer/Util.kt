package tech.figure.hdwallet.signer

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import org.bouncycastle.jce.provider.BouncyCastleProvider

object paths {
    val testnet = "m/44'/1'/0'/0/0'"
    val mainnet = "m/44'/505'/0'/0/0"
}

/**
 * Testing deterministic "random" seed.
 */
val randomSeed: ByteArray = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())

/**
 * [SecureRandom] initialized with [randomSeed].
 */
val random = SecureRandom(randomSeed)

/**
 * Generate an elliptic curve keypair
 *
 * @param keySize The key size. Default is 256.
 */
fun createECKeyPair(keySize: Int = 256): Pair<PublicKey, PrivateKey> {
    val keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
    keyGen.initialize(keySize, random)
    val pair: KeyPair = keyGen.generateKeyPair()
    return Pair(pair.public, pair.private)
}
