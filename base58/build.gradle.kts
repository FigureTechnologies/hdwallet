import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("with-core")
    id("with-testing")
    id("with-publishing")
}

dependencies {
    implementation(projects.common)
}