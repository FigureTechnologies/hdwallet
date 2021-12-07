package io.provenance.hdwallet.hrp

enum class Hrp(val mainnet: String, val testnet: String) {
    CosmosHub("cosmos", "cosmos"),
    CryptoOrg("cro", "tcro"),
    ProvenanceBlockchain("pb", "tp"),
    ;
}