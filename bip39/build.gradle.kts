plugins {
    id("with-core")
    id("with-testing")
    id("with-publishing")
}

dependencies {
    implementation(projects.common)
    implementation(libs.bouncyCastle)
}
