plugins {
    id("with-core")
    id("with-testing")
    id("with-publishing")
}

dependencies {
    implementation(projects.ec)

    testImplementation(projects.base58)
    testImplementation(projects.bip32)
    testImplementation(projects.bip39)
    testImplementation(projects.common)
    testImplementation(projects.hdwallet)
}
