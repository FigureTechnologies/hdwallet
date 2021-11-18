import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

object Deps {
    private fun mvn(group: String, name: String, version: String): ModuleDependency =
        DefaultExternalModuleDependency(group, name, version)

    // General deps.
    val bouncycastle = mvn("org.bouncycastle", "bcprov-jdk15on", Versions.bouncyCastle)
    val commonsCodec = mvn("commons-codec", "commons-codec", Versions.commonsCodec)
    val jacksonKotlin = mvn("com.fasterxml.jackson.module", "jackson-module-kotlin", Versions.jackson)

    // Kotlin deps.
    val kotlinStdLibJdk8 = mvn("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Versions.kotlin)
    val kotlinStdLib = mvn("org.jetbrains.kotlin", "kotlin-stdlib", Versions.kotlin)
    val kotlinReflect = mvn("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)

    // Test deps.
    val junit = mvn("junit", "junit", Versions.junit)
    val coroutines = mvn("org.jetbrains.kotlinx", "kotlinx-coroutines-core", Versions.coroutines)
}
