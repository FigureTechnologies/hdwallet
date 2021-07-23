package io.provenance.bip39

import javax.crypto.SecretKey
import javax.security.auth.Destroyable

class DeterministicSeed(val value: ByteArray) : Destroyable {
    override fun destroy() {
        value.fill(0x00)
    }

    companion object {
        fun fromKey(key: SecretKey): DeterministicSeed = DeterministicSeed(key.encoded)
        fun fromBytes(bytes: ByteArray): DeterministicSeed = DeterministicSeed(bytes)
    }
}
