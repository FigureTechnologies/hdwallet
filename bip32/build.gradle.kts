dependencies {
    listOf(":base58", ":bip39", ":bip44", ":ec", ":common")
        .map { implementation(project(it)) }
}
