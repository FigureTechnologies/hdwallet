import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.repositories

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin", "kotlin-stdlib")
    implementation("org.jetbrains.kotlin", "kotlin-reflect")

    implementation(libs.bouncyCastle)
}
