plugins {
    id("with-core")
}

dependencies {
    api(projects.hdwallet)

    implementation(libs.grpc.protobuf)
    implementation(libs.provenance.proto.kotlin)
    implementation(libs.provenance.grpc.client)
}
