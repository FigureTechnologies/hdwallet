package tech.figure.hdwallet.bech32

import kotlin.jvm.JvmInline

/**
 * A typed Bech32 address.
 */
@JvmInline
value class Address(val value: String) {

    companion object {
        fun fromString(value: String): Address = Address(value)
    }

    override fun toString() = value
}
