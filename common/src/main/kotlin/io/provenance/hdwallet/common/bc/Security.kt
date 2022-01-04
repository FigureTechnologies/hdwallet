package io.provenance.hdwallet.common.bc

import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider

/**
 * Installs the BouncyCastyle [java.security.Provider] if needed.
 */
fun registerBouncyCastle() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(BouncyCastleProvider())
    }
}