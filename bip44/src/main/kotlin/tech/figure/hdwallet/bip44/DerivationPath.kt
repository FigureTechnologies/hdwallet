package tech.figure.hdwallet.bip44

/**
 * Defines a typed representation of a BIP44-style derivation path.
 *
 * Note: for a BIP-44 derivation path to be valid all 5 components (coin, purpose, account, change, index)
 * MUST be present.
 *
 * See https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki
 */
data class DerivationPath(
    val purpose: PathElement.Purpose,
    val coinType: PathElement.CoinType,
    val account: PathElement.Account,
    val change: PathElement.Change,
    val index: PathElement.Index,
) {
    companion object {
        /**
         * Parse a string into a [DerivationPath] instance.
         */
        fun from(path: String): DerivationPath = PathElements.from(path).toDerivationPath()
    }

    data class Builder(private val existingPath: DerivationPath) {
        private var account: PathElement.Account? = null
        private var index: PathElement.Index? = null

        /**
         * Set the index component of the derivation path to be built.
         *
         * @param account The account value to use.
         * @param harden Specify if hardening should be applied to the path element.
         */
        fun account(account: Int, harden: Boolean): Builder =
            apply {
                require(account >= 0) { "account cannot be negative" }
                this.account = PathElement.Account(account, harden)
            }

        /**
         * Set the account component of the derivation path to be built.
         *
         * @param index The index value to use.
         * @param harden Specify if hardening should be applied to the path element.
         */
        fun index(index: Int, harden: Boolean): Builder =
            apply {
                require(index >= 0) { "index cannot be negative" }
                this.index = PathElement.Index(index, harden)
            }

        /**
         * Construct a new [DerivationPath] instance from this builder.
         */
        fun build(): DerivationPath = DerivationPath(
            purpose = existingPath.purpose,
            coinType = existingPath.coinType,
            account = this.account ?: existingPath.account,
            change = existingPath.change,
            index = this.index ?: existingPath.index
        )
    }

    /**
     * Construct a new [Builder] instance from this [DerivationPath].
     */
    fun toBuilder(): Builder = Builder(this)

    /**
     * Returns a list of the elements comprising the derivation path.
     *
     * The elements returned correspond to the standard defined in
     * https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki, specifically
     *
     * (1) purpose
     * (2) coin type
     * (3) account
     * (4) change
     * (5) address index
     *
     * in the same order.
     */
    fun elements(): List<PathElement> = listOf(purpose, coinType, account, change, index)

    override fun toString(): String = elements().toPathString()
}

/**
 * Convert an iterable of derivation path elements into a typed derivation path.
 */
fun Iterable<PathElement>.toDerivationPath(): DerivationPath {
    val elements = this.take(5)
    require(elements.size == 5) { "Missing path elements" }
    check(elements[0] is PathElement.Purpose) { "Path element 0 is not purpose" }
    check(elements[1] is PathElement.CoinType) { "Path element 1 is not coin type" }
    check(elements[2] is PathElement.Account) { "Path element 2 is not account" }
    check(elements[3] is PathElement.Change) { "Path element 3 is not change" }
    check(elements[4] is PathElement.Index) { "Path element 4 is not index" }
    return DerivationPath(
        purpose = elements[0] as PathElement.Purpose,
        coinType = elements[1] as PathElement.CoinType,
        account = elements[2] as PathElement.Account,
        change = elements[3] as PathElement.Change,
        index = elements[4] as PathElement.Index,
    )
}
