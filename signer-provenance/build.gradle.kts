dependencies {
    listOf(":hdwallet")
        .map { api(project(it)) }

    implementation("io.grpc", "grpc-protobuf", Versions.grpc)
    implementation("io.provenance", "proto-kotlin", Versions.provenance)
    implementation("io.provenance.client", "pb-grpc-client-kotlin", Versions.provenanceClient)
}
