import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("with-core")
    id("with-testing")
}

dependencies {
    implementation(projects.common)
}