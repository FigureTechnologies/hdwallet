package io.provenance.hdwallet.wallet

import io.provenance.hdwallet.bip32.toRootKey
import io.provenance.hdwallet.bip39.DeterministicSeed
import io.provenance.hdwallet.bip39.MnemonicWords
import io.provenance.hdwallet.common.hashing.sha256
import io.provenance.hdwallet.encoding.base58.base58EncodeChecked
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.stream.Collectors
import kotlin.streams.toList

class TestWallet {
    private data class WalletData(val path: String, val address: String, val publicKey: String, val privateKey: String, val signature: String)
    private data class Tv(val seed: DeterministicSeed, val data: List<WalletData>)

    private fun runBIP32SyncTestVectors(seed: DeterministicSeed, vectors: List<WalletData>) {
        vectors.map { runBip32Test(seed, it) }
    }

    private fun runFromSeedSyncTestVectors(seed: DeterministicSeed, vectors: List<WalletData>) {
        vectors.map { runFromSeedTest(seed, it) }
    }

    private fun runBip32Test(seed: DeterministicSeed, walletData: WalletData) {
        val encodedKey = seed.toRootKey().serialize().base58EncodeChecked()
        val wallet: Wallet = Wallet.fromBip32("cosmos", encodedKey)

        val childKey = wallet[walletData.path]

        val sig = childKey.sign( "test".toByteArray().sha256())
        Assert.assertEquals(childKey.address, walletData.address)
    }

    private fun runFromSeedTest(seed: DeterministicSeed, walletData: WalletData) {
        val wallet = Wallet.fromSeed("cosmos", seed)
        val childKey = wallet[walletData.path]
        val sig = childKey.sign("test".toByteArray().sha256())

        Assert.assertEquals(childKey.address, walletData.address)
    }

    private fun runBIP32AsyncTestVectors(vectors: List<Tv>) {
        runBlocking {
            val tasks = mutableListOf<Deferred<Unit>>()
            for (vector in vectors) {
                tasks.addAll(
                    vector.data.parallelStream().map {
                        async { runBip32Test(vector.seed, it) }
                    }.collect(Collectors.toList())
                )
            }

            // await for all async tests to finish
            tasks.awaitAll()
        }
    }

    private fun runFromSeedAsyncTestVectors(vectors: List<Tv>) {
        runBlocking {
            val tasks = mutableListOf<Deferred<Unit>>()
            for (vector in vectors) {
                tasks.addAll(
                    vector.data.parallelStream().map {
                        async { runFromSeedTest(vector.seed, it) }
                    }.collect(Collectors.toList())
                )
            }

            // await for all async tests to finish
            tasks.awaitAll()
        }
    }

    @Test
    fun testWalletSyncFromSeed() {
        val vectors = getVectors()
        vectors.map {
            runFromSeedSyncTestVectors(it.seed, it.data)
        }
    }

    @Test
    fun testWalletAsyncFromSeed() = runFromSeedAsyncTestVectors(
        getVectors()
    )

    @Test
    fun testWalletSyncFromBip32() {
        val vectors = getVectors()
        vectors.map {
            runBIP32SyncTestVectors(it.seed, it.data)
        }
    }

    @Test
    fun testWalletAsyncFromBip32() = runBIP32AsyncTestVectors(
        getVectors()
    )

    private fun getVectors(): List<Tv> {
        // Test data source of truth from here: https://iancoleman.io/bip39/ pass phrase is 'trezor', coin is 'ATOM', and derivation path is BIP44
        return listOf(
            Tv(
                MnemonicWords.of("gun green cherry guitar barely mango chaos rice absent regular wide since").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0", "cosmos1wqxdz929kh7utgpt6p5cu7xycte340cxrl8t6h", "cosmospub1addwnpepqd640d584qqz2j0hj20cr30axsa3rcednf02uvwdv8j32wzjp769smz9zk2", "I4bza9OOwfN2eaFsT63KelNVebhZcADZPoolefV1pwk=", ""),
                    WalletData("m/44'/118'/0'/0/1","cosmos1skeamj53fd5772fsjt02xy7gvlwjt49cpfx4uw","cosmospub1addwnpepqfuqv5ff7s7cu6ue2jc934jsufxmq6fxa4ahthzd5jgrwzth02fuq6q9x4h","jTM2MWCaN+yywwa7Kku3uOaPKgxT3i7yDoGgWeE8wyw=", ""),
                    WalletData("m/44'/118'/0'/0/2","cosmos17r69g2va92pd2ye4mvq40zxtv0zku6me6hea52","cosmospub1addwnpepqgcv3ckf05d5n9rg95nwerf5gccwnaflchte26qlx2zk5e49dmj2x3l4y50","Fr5LjB1+9jJv6/yE4ThfogugTIKJt8CKn+QLanbyX70=", "")
                )
            ),
            Tv(
                MnemonicWords.of("approve fossil renew stamp outer achieve mushroom type uncover radio abuse off liberty catalog repeat duck early impulse answer bounce correct chief cook general").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1qg2928pkya32te03g2nmjnf086ujpgxyj0k62w","cosmospub1addwnpepq2zkhqnvltqjej23zt33xtfvzyc3s7s6tw68enr7xqtprkgf3xavzrtcel4","Eoqa8B+CSxhtozjIheMe+Q7T2ZurcveJL/CxJjTz5I4=", ""),
                    WalletData("m/44'/118'/0'/0/1","cosmos1vn2dxpmytcv06zz5ztd3cxdeqddvct7j8enxcm","cosmospub1addwnpepqd8hjj4cw97xdfsd280xvgl2g433fjlq5y5vqjda7qq94s06p893s7pemhx","BjCF5UhDAoe8jT9XIWQ5pkY3Thhx2Tx8JA6aVNEsNXU=", ""),
                    WalletData("m/44'/118'/0'/0/2","cosmos15vmr7d28xnermswtpurtglkmtyy56yycgfpnrq","cosmospub1addwnpepqw52qarehscctvwyadm09puayyxr82uum7qrmks2ks2wztw9zcy36g3fe8j","7J28wicN9ly4sZ0cN7Gl9P9ULNt7lJVHgurqahRhcR8=", "")
                )
            ),
            Tv(
                MnemonicWords.of("october shed view vivid horn bracket confirm sting lava fly insane analyst month finger speak riot tone usual").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1ftlulhgat77cyz3waxdx282v2ne7fgja95qv5q","cosmospub1addwnpepqd8netmxxay2arfkvlukvehdgzs68gxaysxad603nzuae3uq52jckfjkutw","CSOPY3BBQEEGn2LBblYzt0Z3iyp/kW+W35oRBruVE2g=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("away disease shaft patrol sorry clip catch inflict traffic repeat bunker filter you member gossip").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos17wp5tzps5e9lqj4dcwhjqh9yyhn7gke2h70mu7","cosmospub1addwnpepq2wava9hvtszngv5s202c3ghestmxs3vx4m7wkjprj6tu5etjg5k534w889","fsxV72uC5vtriEjy7wFtHtmSlmypnAN/OVT+4jTA7nA=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("dream plate axis utility flavor swim odor napkin glide wheat blouse young").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1dg0r5xedrt7hu9xpng80778m3xqfhgfuywn5rw","cosmospub1addwnpepqf0dgpegyw52275q4w3plp3d3n3eq2zny767k27zfxzxn3hf9ycvzzj8k3h","R1BaxCJafULqzaFfkLo9unbbLsRpHE2ZekZ3a3H7omA=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("bronze rebuild analyst elbow connect fold develop secret fringe double divide merry").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1cpeszg0arka0zjh0gejnz4ul2t4px568g62vzx","cosmospub1addwnpepqg8ee45hpzsnqyzdeqkprj4dtggtatzrm7lccapp89pnhhrtp8xh2h0g3du","E0nt2NM8V2HK/6BBFPyDRZu3fnOqpP/OUVLZLye824w=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("jewel attack purpose goat identify regular isolate left funny morning emotion reunion").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1smhcgesdrztd9y3t9mqakgflt7ss7uz7aqn0cv","cosmospub1addwnpepq2adjeyndmqa7ws6e9gavuy0gfkw2hyry6fduwhryt85jf25j0hvuwyjy9v","Xg7Fsnp6Oiwua9hJPRUb3XccO4n71HZHZhi0uEtQadQ=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("blast amount purchase ready what reform crane decline idea ring supreme that inspire sketch toy").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos1jztta7l6l9m00kaf7jvtdw29cqr4d3vnm6ch70","cosmospub1addwnpepq2tqptun2tummcvktjut33ej4ktcpnmgnlgxnsv4j349rv30pq33sk893rn","FIgBLGeZTZ3+4T8IBqH3/c8++5gkMzevFSzck09Fc8A=", ""),
                )
            ),
            Tv(
                MnemonicWords.of("reason violin squirrel century park catch aim arm buddy all borrow bleak torch clinic cattle").toSeed("trezor".toCharArray()),
                listOf(
                    WalletData("m/44'/118'/0'/0/0","cosmos174zlrcp6c778zueta84y7cdnk9utkr9ulszd9l","cosmospub1addwnpepqf3ve7nr2wml7kljcgfwfaf5gnk9g6z564fmplpweu5r9c3msrgl5rvmpkg","5MKhiU1GW37L3x94vhcbRULUym+1fh0Yv1luL+UBo7Y=", ""),
                )
            )
        )
    }
}