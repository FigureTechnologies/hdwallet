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

    group = "io.provenance.hdwallet"

    val projectVersion = project.properties["version"]
        ?.toString()
        .let { if (it == null || it == "unspecified") "1.0-SNAPSHOT" else it }

    version = projectVersion

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
            Deps.junit
        ).map(::testImplementation)
    }

    tasks {
        test {
            finalizedBy(jacocoTestReport)
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

    publishing {
        repositories {
            sonatypeOss(projectVersion)
        }

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
                            name.set("mtps")
                            email.set("mtps@users.noreply.github.com")
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
    }

    tasks.create("version") {
        println(projectVersion)
    }
}
