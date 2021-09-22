dependencies {
    listOf(":base58", ":bech32", ":bip32", ":bip39", ":bip44", ":common", ":ec", ":signer")
        .map { implementation(project(it)) }
}
