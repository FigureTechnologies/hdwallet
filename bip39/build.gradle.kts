dependencies {
    implementation(project(":common"))

    testImplementation("com.fasterxml.jackson.module", "jackson-module-kotlin", Versions.jackson)
    testImplementation("commons-codec", "commons-codec", "1.15")
}
