import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.signing

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

val artifactName = if (name.startsWith("hdwallet")) name else "hdwallet-$name"

inner class PublishingInfo {
    val group = "tech.figure"
    val name = "Provenance HDWallet Implementation"
    val description = "A collection of libraries to facilitate HDWallet usage"
    val url = "https://figure.tech"
}

val info = PublishingInfo()

plugins {
    `maven-publish`
    signing
}

val projectGroup = "tech.figure.hdwallet"
val projectVersion = project.property("version")?.takeIf { it != "unspecified" } ?: "1.0-SNAPSHOT"


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = info.group
            artifactId = artifactName
            version = projectVersion.toString()

            from(components["java"])

            pom {
                name.set(info.name)
                description.set(info.description)
                url.set(info.url)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("tech@figure.com")
                        name.set("Figure Technologies")
                        email.set("tech@figure.com")
                    }
                }

                scm {
                    connection.set("git@github.com:FigureTechnologies/hdwallet.git")
                    developerConnection.set("git@github.com/FigureTechnologies/hdwallet.git")
                    url.set("https://github.com/FigureTechnologies/hdwallet")
                }
            }
        }
    }
    signing {
        setRequired {
            gradle.taskGraph.allTasks.any { it is PublishToMavenRepository }
        }
        sign(publishing.publications["maven"])
    }
}