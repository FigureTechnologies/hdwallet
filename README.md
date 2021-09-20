# HDWallet

HDWallet is a collection of helper utilities to ease use of hierarchical deterministic wallets and key management in kotlin and java.

# Getting Started

## Maven

Current published version:

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.provenance.hdwallet/hdwallet/badge.svg)

```xml
<dependency>
  <groupId>io.provenance.hdwallet</groupId>
  <artifactId>hdwallet</artifactId>
  <version>${version}</version>
</dependency>
```

## Gradle

```
implementation("io.provenance.hdwallet", "hdwallet", "$version")
```

# Quickstart

## Initializing a wallet from a mnemonic set and signing a []byte payload.

### Shortcut versions

```kotlin
// Convert a seed into a wallet.
val wallet = Wallet.fromSeed(hrp, seed)

// Convert a mnemonic into a wallet.
val wallet = Wallet.fromMnemonic(hrp, wordlist)

// Convert a base58-encoded bip32 key into a wallet.
val wallet = Wallet.fromBip32(hrp, base58EncodedBip32Key)

// Derive a child key from the root wallet.
val testnetPath = "m/44'/1'/0'/0/0"
val childKey = wallet[testnetPath]

// Sign a payload
val signature = BCECSigner().sign(childKey.keyPair.privateKey, "test".toByteArray().sha256())
```

### Full key derivation (the shortcuts outlined above perform the following for you).

```kotlin
// Generate the seed from the mnemonic + passphrase.
val seed = MnemonicWords.of("hip valley wave rider ... ...").toSeed("trezor".toCharArray())

// Derive the root extkey.
val rootKey = seed.toRootKey(/* curve = secp256k1 */) // optional curve parameter, default: secp256k1

// Derive the child key based on path.
val testnetPath = "m/44'/1'/0'/0/0"
val childKey = seed.childKey(testnetPath)

// Create a new BouncyCastle signer
val signer = BCECSigner()

// Generate an ecdsa signature.
val payloadHash = "test".toByteArray().sha256()
val sig = signer.sign(childKey.keyPair.privateKey, payloadHash)
```

## Initializing a key from a bip32 encoded private key

```kotlin
// Decode the base58-encoded bip32 key into an extKey.
val encodedKey = ...
val extKey = ExtKey.deserialize(encodedKey.base58DecodeChecked())
```

## Initializing a key from a java private key

TODO

