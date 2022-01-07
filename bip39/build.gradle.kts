dependencies {
    implementation(project(":common"))
    testImplementation("commons-codec", "commons-codec", Versions.commonsCodec)
    testImplementation("com.fasterxml.jackson.module", "jackson-module-kotlin", Versions.jackson)
}
