rootProject.name = "hdwallet-root"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("common")
include("base58")
include("bech32")
include("bip32")
include("bip39")
include("bip44")
include("ec")
include("signer")
include("signer-provenance")
include("hdwallet")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    includeBuild(".build-logic")
}