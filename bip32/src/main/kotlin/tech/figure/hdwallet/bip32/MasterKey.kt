package tech.figure.hdwallet.bip32

import tech.figure.hdwallet.bip39.DeterministicSeed
import tech.figure.hdwallet.bip44.BIP44_HARDENING_FLAG
import tech.figure.hdwallet.bip44.parseBIP44Path
import tech.figure.hdwallet.common.hashing.sha256hash160
import tech.figure.hdwallet.ec.DEFAULT_CURVE
import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.ECKeyPair
import tech.figure.hdwallet.ec.PrivateKey
import tech.figure.hdwallet.ec.PublicKey
import tech.figure.hdwallet.ec.decompressPublicKey
import tech.figure.hdwallet.ec.extensions.toBigInteger
import tech.figure.hdwallet.ec.extensions.toBytesPadded
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import tech.figure.hdwallet.ec.extensions.packIntoBigInteger

private val BITCOIN_SEED = "Bitcoin seed".toByteArray(Charsets.UTF_8)
private const val HMAC_SHA512 = "HmacSHA512"
private const val PRIVATE_KEY_SIZE = 32
private const val CHAINCODE_SIZE = 32
private const val EXTENDED_KEY_SIZE: Int = 78

private fun hmacSha512(key: ByteArray, input: ByteArray): ByteArray =
    Mac.getInstance(HMAC_SHA512).run {
        val spec = SecretKeySpec(key, HMAC_SHA512)
        init(spec)
        doFinal(input)
    }

@JvmInline
value class ExtKeyVersion(val bytes: ByteArray) {
    init {
        require(bytes.size == 4) { "invalid version len" }
    }
}

@JvmInline
value class ExtKeyFingerprint(val bytes: ByteArray = byteArrayOf(0, 0, 0, 0)) {
    init {
        require(bytes.size == 4) { "invalid fingerprint len" }
    }
}

@JvmInline
value class ExtKeyChainCode(val bytes: ByteArray) {
    init {
        require(bytes.size == 32) { "invalid chaincode len" }
    }
}

internal fun ByteBuffer.getByteArray(size: Int) = ByteArray(size).also { get(it) }

data class ExtKey(
    val versionBytes: ExtKeyVersion,
    val depth: AccountType,
    val parentKeyFingerprint: ExtKeyFingerprint,
    val childNumber: Int,
    val chainCode: ExtKeyChainCode,
    val keyPair: ECKeyPair
) {
    private val curve: Curve = keyPair.privateKey.curve

    fun serialize(publicKeyOnly: Boolean = false): ByteArray {
        if (!publicKeyOnly && !(versionBytes.bytes.contentEquals(xprv)) && !(versionBytes.bytes contentEquals tprv))
            throw KeyException("The extended version bytes dedicated to public keys. Suggest using publicKeyOnly mode")

        if (!publicKeyOnly && keyPair.privateKey.key == BigInteger.ZERO)
            throw KeyException("The extended key doesn't provide any private key. Suggest using publicKeyOnly mode")

        val bb = ByteBuffer.allocate(EXTENDED_KEY_SIZE)
        bb.put(
            when {
                publicKeyOnly && versionBytes.bytes.contentEquals(xprv) -> xpub
                publicKeyOnly && versionBytes.bytes.contentEquals(tprv) -> tpub
                else -> versionBytes.bytes
            }
        )
        bb.put(depth.ordinal.toByte())
        bb.put(parentKeyFingerprint.bytes)
        bb.putInt(childNumber)
        bb.put(chainCode.bytes)
        if (publicKeyOnly) {
            bb.put(keyPair.publicKey.compressed())
        } else {
            bb.put(0x00)
            bb.put(keyPair.privateKey.key.toBytesPadded(PRIVATE_KEY_SIZE))
        }

        return bb.array()
    }

    fun childKey(path: String): ExtKey =
        path.parseBIP44Path().fold(this) { acc, i -> acc.childKey(i.number, i.hardened) }

    fun childKey(index: Int, hardened: Boolean = true): ExtKey {
        if (depth == AccountType.ADDRESS) {
            throw KeyException("cannot create key beyond current scope $depth")
        }

        if (hardened && keyPair.privateKey.key == BigInteger.ZERO) {
            throw KeyException("private key required for hardened child keys")
        }

        val account =
            if (hardened) index or BIP44_HARDENING_FLAG
            else index

        val pub = keyPair.publicKey.compressed()
        val ext = if (hardened) {
            val pkb = keyPair.privateKey.key.toBytesPadded(PRIVATE_KEY_SIZE)
            ByteBuffer.allocate(pkb.size + 5)
                .order(ByteOrder.BIG_ENDIAN)
                .put(0)
                .put(pkb)
                .putInt(account)
                .array()
        } else {
            ByteBuffer.allocate(pub.size + 4)
                .order(ByteOrder.BIG_ENDIAN)
                .put(pub)
                .putInt(account)
                .array()
        }
        val lr = hmacSha512(chainCode.bytes, ext)
        val l = lr.copyOfRange(0, PRIVATE_KEY_SIZE)
        val r = lr.copyOfRange(PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)
        val ib = l.packIntoBigInteger()
        require(ib != BigInteger.ZERO && ib < curve.n) {
            "invalid derived key"
        }

        val fingerprint = ExtKeyFingerprint(pub.sha256hash160().copyOfRange(0, 4))
        val nextDepth = depth.next()
        val nextChainCode = ExtKeyChainCode(r)

        return if (keyPair.privateKey.key != BigInteger.ZERO) {
            val k = ib.add(keyPair.privateKey.key).mod(curve.n)
            require(k != BigInteger.ZERO) {
                "invalid derived key"
            }

            // Build child key
            ExtKey(versionBytes, nextDepth, fingerprint, account, nextChainCode, PrivateKey(k, curve).toECKeyPair())
        } else {
            val q = curve.g
                .mul(ib)
                .add(curve.decodePoint(pub))
                .normalize()
            require(!q.isInfinity) { "invalid derived key is zeros" }
            val pt = curve.createPoint(q.x, q.y).encoded(false)
            val pubk = PublicKey(pt.copyOfRange(1, pt.size).packIntoBigInteger(), curve)
            val prvk = PrivateKey(BigInteger.ZERO, curve)
            ExtKey(versionBytes, nextDepth, fingerprint, account, nextChainCode, ECKeyPair(prvk, pubk))
        }
    }

    companion object {
        fun deserialize(bip32: ByteArray, curve: Curve = DEFAULT_CURVE): ExtKey {
            val bb = ByteBuffer.wrap(bip32)
            val ver = bb.getByteArray(4)
            val depth = bb.get()
            val parent = bb.getByteArray(4)
            val sequence = bb.int
            val hardened = (sequence and BIP44_HARDENING_FLAG) != 0
            val accountNumber =
                if (!hardened) sequence xor BIP44_HARDENING_FLAG
                else sequence
            val chainCode = bb.getByteArray(CHAINCODE_SIZE)
            val hasPrivate = ver.contentEquals(xprv) || ver.contentEquals(tprv)
            val testnet = ver.contentEquals(tprv) || ver.contentEquals(tpub)

            if (depth == 2.toByte() && accountNumber != 1) {
                require(!testnet) { "no test coins in mainnet" }
            }

            val keyPair =
                if (hasPrivate) {
                    bb.get() // Ignore leading 0
                    PrivateKey.fromBytes(bb.getByteArray(PRIVATE_KEY_SIZE), curve).toECKeyPair()
                } else {
                    ECKeyPair(
                        PrivateKey(BigInteger.ZERO, curve),
                        PublicKey(decompressPublicKey(bb.getByteArray(PRIVATE_KEY_SIZE + 1), curve), curve)
                    )
                }

            return ExtKey(
                ExtKeyVersion(ver),
                AccountType.from(depth.toInt()),
                ExtKeyFingerprint(parent),
                sequence,
                ExtKeyChainCode(chainCode),
                keyPair
            )
        }
    }
}

// https://en.bitcoin.it/wiki/BIP_0032
fun DeterministicSeed.toRootKey(
    publicKeyOnly: Boolean = false,
    testnet: Boolean = false,
    curve: Curve = DEFAULT_CURVE
): ExtKey {
    val i = hmacSha512(BITCOIN_SEED, value)
    val il = i.copyOfRange(0, PRIVATE_KEY_SIZE)
    val ir = i.copyOfRange(PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)

    val ib = il.packIntoBigInteger()
    if (ib == BigInteger.ZERO || ib >= curve.n) {
        throw RuntimeException("Invalid key")
    }

    val keyPair = PrivateKey.fromBytes(il, curve).toECKeyPair().let {
        if (publicKeyOnly) ECKeyPair(PrivateKey(BigInteger.ZERO, curve), it.publicKey)
        else it
    }

    return ExtKey(
        mkVersionBytes(publicKeyOnly, testnet),
        AccountType.ROOT,
        ExtKeyFingerprint(),
        0,
        ExtKeyChainCode(ir),
        keyPair,
    )
}

private fun mkVersionBytes(publicKeyOnly: Boolean, testnet: Boolean): ExtKeyVersion {
    val ver = when {
        publicKeyOnly -> if (testnet) tpub else xpub
        else -> if (testnet) tprv else xprv
    }
    return ExtKeyVersion(ver)
}
