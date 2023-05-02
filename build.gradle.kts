import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `maven-publish`
    `java-library`
    signing
    alias(libs.plugins.nexusPublish)
    kotlin("jvm") version(libs.versions.kotlin.get())
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProject("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME"))
            password.set(findProject("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set("858b6e4de4734a") // prevents querying for the staging profile id, performance optimization
        }
    }
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("idea")
        plugin("java-library")
        plugin("jacoco")
        plugin("signing")
    }
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
