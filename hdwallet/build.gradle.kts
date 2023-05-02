plugins {
    id("with-core")
    id("with-testing")
    id("with-publishing")
}

repositories {
    // Required for Elmyr fuzz testing.
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api(projects.base58)
    api(projects.bech32)
    api(projects.bip32)
    api(projects.bip39)
    api(projects.bip44)
    api(projects.common)
    api(projects.ec)
    api(projects.signer)

    api(libs.kotlin.coroutines)

    testImplementation("com.github.xgouchet.Elmyr", "core", libs.versions.elmyr.get())
    testImplementation("com.github.xgouchet.Elmyr", "junit5", libs.versions.elmyr.get())
    testImplementation("com.github.xgouchet.Elmyr", "inject", libs.versions.elmyr.get())
    testImplementation("com.github.xgouchet.Elmyr", "jvm", libs.versions.elmyr.get())
}
