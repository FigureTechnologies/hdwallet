package io.provenance.bip32

enum class AccountType {
    ROOT,
    PURPOSE,
    COIN_TYPE,
    GENERAL,
    SCOPE,
    ADDRESS;

    companion object {
        fun fromOrNull(ordinal: Int): AccountType? =
            values().firstOrNull { it.ordinal == ordinal }

        fun from(ordinal: Int): AccountType =
            requireNotNull(fromOrNull(ordinal)) { "ordinal $ordinal out of range" }
    }

    fun next(): AccountType =
        from(ordinal + 1)
}
