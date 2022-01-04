package io.provenance.hdwallet.encoding.base58

import io.provenance.hdwallet.encoding.base16.base16Decode
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestBase58 {
    @Test
    fun testRFCVectors() {
        val vectors = mapOf(
            "2NEpo7TZRRrLZSi2U" to "Hello World!".toByteArray(),
            "USm3fpXnKG5EUBx2ndxBDMPVciP5hGey2Jh4NDv6gmeo1LkMeiKrLJUUBk6Z" to "The quick brown fox jumps over the lazy dog.".toByteArray(),
            "111233QC4" to "000000287fb4cd".base16Decode(),
            "HJF6DGL1iWG" to "aoeuaoeu".toByteArray(),
            "6WWAVR6RaTut2Av6UM6awEwUE5NwgCpoRmC9WQmcjKLWSwQVE6rcRW23MBinCQ1xxPcFgZB9z2jp1igKVp1f6sdJxmf1c9GpMFxi4e1fp7zEJgJrFYD6yrVxqo2kfLAEV8xYYBJPGJTzkKMq7kfZXuTxnoNdPCjqsYDaCvsLsbwdNWgyHW6Ub9K1f5FXZTVobWAsRBNwaXmDRi78ZWz5h5fnUVRnPiq3HHvSu8DBqdxPngorx8rRkswtDsz1KbFyzDTE7W5eFYoAYbszBmkfR2CTHfoT4yZXYkU4YSLPnLGPZeEaMQonDjr3vN35aCcgeHiJq34kVbENgqet8n8cdh2phNEWyRS8ok6A62Ynb5qFnCVzuDqXYHKJCAyrqudpWS2zbRHEivNAe7B6WBuyPUg86mXZEgyGwsEiv517fWQL6hZcj4NfaqNpGsGJMgvUhu6MGgLruphbqQYEpZeLUk3zcfWqGHoVLW3iwi6i9ULDefXvVEU2SdtfkBQi7xGnZurxPxgShbofmx3QxVTLWntL7gB2LGQ2NWtEyUuxrE2h1UKeEDvPjC6dZpNdemDL8FiMQ15nSSnsEj6GEYaPScox6mjCvouw" to "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.".toByteArray()
        )

        for (vector in vectors) {
            val enc = vector.value.base58Encode()
            val dec = vector.key.base58Decode()

            val kd = vector.key.base58Decode().base58Encode()
            val vd = vector.value.base58Encode().base58Decode()

            assertEquals(vector.key, enc)
            assertArrayEquals(dec, vector.value)
            assertEquals(kd, vector.key)
            assertArrayEquals(vd, vector.value)
        }
    }
}
