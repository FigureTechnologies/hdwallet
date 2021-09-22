dependencies {
    implementation(project(":common"))

    listOf(Deps.jacksonKotlin, Deps.commonsCodec)
        .map(::testImplementation)
}
