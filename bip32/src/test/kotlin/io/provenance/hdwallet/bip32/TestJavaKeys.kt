package io.provenance.hdwallet.bip32

import io.provenance.hdwallet.ec.Curve
import io.provenance.hdwallet.ec.bc.toCurvePoint
import io.provenance.hdwallet.ec.deriveCurve
import io.provenance.hdwallet.ec.toBigInteger
import io.provenance.hdwallet.ec.toECPrivateKey
import io.provenance.hdwallet.ec.toECPublicKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECKeySpec
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Provider
import java.security.Security
import java.security.spec.ECGenParameterSpec

class TestJavaKeys {

    @Before
    fun init() {
        Security.addProvider(BouncyCastleProvider())
    }

    val curveName = "secp256k1"

    @Test
    fun testCurveConverter() {
        val jcurve = ECNamedCurveTable.getParameterSpec(curveName)
        val curve = Curve.lookup(curveName)

        // Control values.
        Assert.assertEquals("n", jcurve.n, curve.n)
        Assert.assertEquals("gx", jcurve.g.xCoord.toBigInteger(), curve.g.x)
        Assert.assertEquals("gy", jcurve.g.yCoord.toBigInteger(), curve.g.y)
        Assert.assertEquals("h", jcurve.h, curve.ecDomainParameters.h)
        Assert.assertEquals("a", jcurve.curve.a.toBigInteger(), curve.ecDomainParameters.curve.a.toBigInteger())
        Assert.assertEquals("fs", jcurve.curve.fieldSize, curve.ecDomainParameters.curve.fieldSize)
        Assert.assertEquals("seed", jcurve.seed, curve.ecDomainParameters.seed)
    }

    @Test
    fun testKeyConvertJavaToEC() {
        // Java -> BC
        val kf = KeyFactory.getInstance("EC")
        val genSpec = ECGenParameterSpec(curveName)
        val gen = KeyPairGenerator.getInstance("ECDSA").also {
            it.initialize(genSpec)
        }

        val keyPair = gen.generateKeyPair()
        val jprvkey = keyPair.private as BCECPrivateKey
        val hprvkey = keyPair.private.toECPrivateKey()
        
        val jpubkey = keyPair.public as BCECPublicKey
        val hpubkey = keyPair.public.toECPublicKey()
        Assert.assertEquals(jprvkey.d, hprvkey.key)
        Assert.assertEquals(jpubkey.q.getEncoded(true).toBigInteger(), hpubkey.key)

        println(jprvkey.toECPrivateKey().deriveCurve())
        println(hprvkey.deriveCurve())
        println(jpubkey.toECPublicKey().deriveCurve())
        println(hpubkey.deriveCurve())
    }

    @Test
    fun testKeyConvertECToJava() {

    }
}
