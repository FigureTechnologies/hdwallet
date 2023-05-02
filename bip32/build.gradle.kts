plugins {
    id("with-core")
    id("with-testing")
    id("with-publishing")
}

dependencies {
    implementation(projects.base58)
    implementation(projects.bip39)
    implementation(projects.bip44)
    implementation(projects.ec)
    implementation(projects.common)
}
