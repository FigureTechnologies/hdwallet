import Repos.sonatypeOss

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin", "kotlin-gradle-plugin", Versions.kotlin)
    }
}

plugins {
    `maven-publish`
    `java-library`
    idea
    jacoco
    signing
    id("io.github.gradle-nexus.publish-plugin") version Versions.nexusPublishPlugin
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProject("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME"))
            password.set(findProject("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set("3180ca260b82a7") // prevents querying for the staging profile id, performance optimization
        }
    }
}

val projectGroup = "io.provenance.hdwallet"
val projectVersion = project.property("version")?.takeIf { it != "unspecified" } ?: "1.0-SNAPSHOT"

group = projectGroup
version = projectVersion


subprojects {
    group = projectGroup
    version = projectVersion

    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("idea")
        plugin("java-library")
        plugin("jacoco")
        plugin("signing")
    }

    project.ext.properties["kotlin_version"] = Versions.kotlin

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        listOf(
            Deps.bouncycastle,
            Deps.kotlinStdLibJdk8, Deps.kotlinStdLib, Deps.kotlinReflect
        ).map(::implementation)

        listOf(
            Deps.junitJupiterApi,
            Deps.junitJupiterEngine,
            Deps.coroutines
        ).map(::testImplementation)
    }

    tasks {
        test {
            finalizedBy(jacocoTestReport)
            useJUnitPlatform()
        }

        jacocoTestReport {
            dependsOn(test)

            reports {
                xml.isEnabled = false
                csv.isEnabled = false
                html.isEnabled = true
            }
        }
    }

    val artifactName = if (name.startsWith("hdwallet")) name else "hdwallet-$name"
    val projectVersion = version.toString()

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = artifactName
                version = project.version.toString()

                from(components["java"])

                pom {
                    name.set("Provenance HDWallet Implementation")
                    description.set("A collection of libraries to facilitate HDWallet usage")
                    url.set("https://provenance.io")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("scirner22")
                            name.set("Stephen Cirner")
                            email.set("scirner@figure.com")
                        }
                    }

                    scm {
                        connection.set("git@github.com:provenance-io/hdwallet.git")
                        developerConnection.set("git@github.com/provenance-io/hdwallet.git")
                        url.set("https://github.com/provenance-io/hdwallet")
                    }
                }
            }
        }
        signing {
            sign(publishing.publications["maven"])
        }
    }
}
