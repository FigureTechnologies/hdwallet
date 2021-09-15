package io.provenance.crypto.common.hashing

/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Generates SHA-256 digest for the given `input`.
 *
 * @return The hash value for the given input
 * @throws RuntimeException If we couldn't find a provider
 */
fun ByteArray.sha256(): ByteArray {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.digest(this)
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Couldn't find a SHA-256 provider", e)
    }
}

/**
 * Calculates the SHA-256 hash of the given byte range,
 * and then hashes the resulting hash again.
 *
 * @param offset the offset within the array of the bytes to hash
 * @param len the number of bytes to hash
 * @return the double-hash (in big-endian order)
 */
fun ByteArray.hashTwice(offset: Int, len: Int): ByteArray =
    MessageDigest.getInstance("SHA-256").let {
        it.update(this, offset, len)
        it.digest(it.digest())
    }

/**
 * Generates HMAC-SHA256 digest for the given `input`.
 *
 * @return The hash value for the given input
 * @throws RuntimeException If we couldn't find a provider
 */
fun ByteArray.hmacSha512(key: ByteArray): ByteArray {
    val hMac = HMac(SHA512Digest())
    hMac.init(KeyParameter(key))
    hMac.update(this, 0, this.size)
    val out = ByteArray(64)
    hMac.doFinal(out, 0)
    return out
}

/**
 * Generates SHA256-RIPEMD160 digest for the given `input`.
 *
 * @return The hash value for the given input
 * @throws RuntimeException If we couldn't find a provider
 */
fun ByteArray.sha256hash160(): ByteArray {
    val digest = RIPEMD160Digest()
    val sha256 = sha256()
    digest.update(sha256, 0, sha256.size)
    val out = ByteArray(20)
    digest.doFinal(out, 0)
    return out
}
