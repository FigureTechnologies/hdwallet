# HDWallet

HDWallet is a collection of helper utilities to ease use of hierarchical deterministic wallets and key management in kotlin and java.

# Getting Started

## Maven

Current published version:

![Maven Central](https://img.shields.io/maven-central/v/tech.figure.hdwallet/hdwallet.svg?label=Maven%20Central)

```xml
<dependency>
  <groupId>tech.figure.hdwallet</groupId>
  <artifactId>hdwallet</artifactId>
  <version>$VERSION</version>
</dependency>
```

## Gradle

```kotlin
implementation("tech.figure.hdwallet", "hdwallet", "$VERSION")
```

# Quickstart

## Wallet operation examples

### Initializing a wallet from a mnemonic set and signing a `ByteArray` payload

#### Shortcut version

```kotlin
// Convert a seed into a wallet:
val wallet = Wallet.fromSeed(hrp, seed)

// Convert a mnemonic into a wallet:
val wallet = Wallet.fromMnemonic(hrp, "passphrase".toCharArray(), wordlist)

// Convert a base58-encoded bip32 key into an account and derive a child key / account:
val testnetPath = "m/44'/1'/0'/0/0"
val account = Account.fromBip32(hrp, base58EncodedBip32Key)
val childAccount = account[testnetPath]

// Generate a bech32 address for the account:
val address = account.address

// Derive a child key from the root wallet:
val testnetPath = "m/44'/1'/0'/0/0"
val childKey = wallet[testnetPath]

// Sign a payload:
val payload = "test-payload".toByteArray()
val payloadHash = payload.sha256()
val signature = BCECSigner().sign(childKey.keyPair.privateKey, payloadHash)

// Verify the signature:
val ok = BCECSigner().verify(childKey.keyPair.publicKey, payloadHash, signature)
```

### Full key derivation
_(the shortcuts outlined above perform the following for you)_

```kotlin
// Generate the seed from the mnemonic + passphrase:
val seed = MnemonicWords.of("hip valley wave rider ... ...").toSeed("trezor".toCharArray())

// Derive the root extended key:
val rootKey = seed.toRootKey(/* curve = secp256k1 */) // optional curve parameter, default: secp256k1

// Derive the child key based on path:
val testnetPath = "m/44'/1'/0'/0/0"
val childKey = rootKey.childKey(testnetPath)

// Sign a payload:
val payloadHash = "test".toByteArray().sha256()
val signature = BCECSigner().sign(childKey.keyPair.privateKey, payloadHash)
```

### Initializing a key from a bip32 encoded private key

```kotlin
// Decode the base58-encoded bip32 key into an extKey.
val extKey: ExtKey = ExtKey.deserialize("<encoded-key>".base58DecodeChecked())
```

## Key conversion examples

### Converting a base64 encoded key into a `java.security.PublicKey` using the default (secp256k1) curve

```kotlin
import tech.figure.hdwallet.ec.PublicKey
import tech.figure.hdwallet.ec.extensions.toJavaECPublicKey
import java.security.PublicKey as JavaPublicKey

val encoded: String = "AmqcgLOp640tgccRYL/+PtKftP0NwHcDNzUiHNsJV+gb"
val pubKey: JavaPublicKey = PublicKey.fromString(encoded).toJavaECPublicKey()
```

### Convert between private key types

```kotlin
import tech.figure.hdwallet.bip32.ExtKey
import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.ECKeyPair
import tech.figure.hdwallet.encoding.base58.base58DecodeChecked
import java.security.PrivateKey as JavaPrivateKey

val extKey: ExtKey = ExtKey.deserialize("<encoded-key>".base58DecodeChecked())
val keyPair: ECKeyPair = extKey.keyPair

// Convert: hdwallet -> Java -> BC -> BigInteger -> ByteArray -> hdwallet
val privateKey: PrivateKey = keyPair.privateKey
val javaPrivateKey: JavaPrivateKey = privateKey.toJavaECPrivateKey()
val bcPrivateKey: BCECPrivateKey? = javaPrivateKey.toBCECPrivateKey()  // BouncyCastle
val (bcPrivateBigInt, curve): Pair<BigInteger, Curve> = bcPublicKey!!.toBigIntegerPair()
val bytesPrivateKey: ByteArray = bcPrivateBigInt.toByteArray()
val privateKeyCopy: PrivateKey = PrivateKey.fromBytes(bytesPrivateKey, curve)

// assert(privateKey == privateKeyCopy)
```

### Convert between public key types

```kotlin
import tech.figure.hdwallet.bip32.ExtKey
import tech.figure.hdwallet.ec.Curve
import tech.figure.hdwallet.ec.ECKeyPair
import tech.figure.hdwallet.ec.PublicKey
import tech.figure.hdwallet.encoding.base58.base58DecodeChecked
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey

val extKey: ExtKey = ExtKey.deserialize("<encoded-key>".base58DecodeChecked())
val keyPair: ECKeyPair = extKey.keyPair

// Convert: hdwallet -> Java -> BC -> BigInteger -> ByteArray -> hdwallet
val publicKey: PublicKey = keyPair.publicKey
val javaPublicKey: JavaPublicKey = publicKey.toJavaECPublicKey()
val bcPublicKey: BCECPublicKey? = javaPublicKey.toBCECPublicKey()  // BouncyCastle
val (bcPublicBigInt, curve): Pair<BigInteger, Curve> = bcPublicKey!!.toBigIntegerPair()
val bytesPublicKey: ByteArray = bcPublicBigInt.toByteArray()
val publicKeyCopy: PublicKey = PublicKey.fromBytes(bytesPublicKey, curve)

// assert(publicKey == publicKeyCopy)
```

### Convert between key pair types

```kotlin
import tech.figure.hdwallet.ec.PublicKey
import java.security.PrivateKey as JavaPrivateKey
import java.security.PublicKey as JavaPublicKey
import java.security.KeyPair

val extKey: ExtKey = ExtKey.deserialize("<encoded-key>".base58DecodeChecked())
val keyPair: ECKeyPair = extKey.keyPair
val javaKeyPair: KeyPair = keyPair.toJavaECKeyPair()
```
