dependencies {
    implementation(project(":ec"))

    listOf(project(":base58"), project(":bip32"), project(":bip39"), project(":common"), project(":hdwallet"))
        .map(::testImplementation)
}
