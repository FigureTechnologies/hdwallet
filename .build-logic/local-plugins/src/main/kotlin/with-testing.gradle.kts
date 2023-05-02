val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

plugins {
    `java-library`
}

dependencies {
    testImplementation(libs.bundles.junit)

    testImplementation(libs.commons.codec)
    testImplementation(libs.jackson.kotlin)
}