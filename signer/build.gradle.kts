dependencies {
    implementation(project(":ec"))

    listOf(":base58", ":bip32", ":bip39", ":common", ":hdwallet")
        .map { testImplementation(project(it)) }
}
