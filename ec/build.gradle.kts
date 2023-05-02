plugins {
    id("with-core")
    id("with-testing")
}

dependencies {
    implementation(projects.common)
    implementation(projects.bech32)
}
