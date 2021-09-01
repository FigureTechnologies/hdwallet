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
    }

    group = "io.provenance.hdwallet"
    version = "1.0-SNAPSHOT"

    project.ext.properties["kotlin_version"] = Versions.kotlin

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.bouncycastle", "bcprov-jdk15on", Versions.bouncyCastle)

        implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-stdlib", Versions.kotlin)
        implementation("org.jetbrains.kotlin", "kotlin-reflect", Versions.kotlin)

        testImplementation("junit", "junit", "4.13.1")
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)

        reports {

            xml.isEnabled = false
            csv.isEnabled = false
            html.isEnabled = true
        }
    }

    val artifactName = if (name.startsWith("hdwallet-")) name else "hdwallet-$name"

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = artifactName
                version = project.version.toString()

                from(components["java"])
            }
        }
    }
}
