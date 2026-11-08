package com.evilcorp.android

import com.evilcorp.android.labs.Flags
import com.evilcorp.android.labs.IntegrityGate
import com.evilcorp.android.labs.LabRegistry
import com.evilcorp.android.labs.PinVault
import com.evilcorp.android.labs.SslPinValidator
import com.evilcorp.android.labs.WeakKeystore
import com.evilcorp.android.labs.WeakRandom
import com.evilcorp.android.model.ChallengeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class LabRegistryTest {
    @Test fun everyCatalogEntryHasALabAndViceVersa() {
        assertEquals(30, LabRegistry.entries.size)
        assertEquals(ChallengeCatalog.all.map { it.id }.toSet(), LabRegistry.entries.keys)
    }

    @Test fun flagsAreUniqueAndWellFormed() {
        val flags = LabRegistry.entries.values.map { it.flag }
        assertEquals(30, flags.toSet().size)
        flags.forEach { assertTrue(it, it.matches(Regex("EVILCORP\\{[A-Za-z0-9_]+}"))) }
    }

    @Test fun flagCheckIsExact() {
        val f = LabRegistry.entries.getValue("sqli").flag
        assertTrue(LabRegistry.isCorrect("sqli", "  $f "))
        assertFalse(LabRegistry.isCorrect("sqli", f.lowercase()))
        assertFalse(LabRegistry.isCorrect("prefs", f))
    }

    @Test fun pinLabIsSolvableByBruteForce() {
        val pin = (0..9999).map { "%04d".format(it) }.first(PinVault::check)
        assertEquals("7391", pin)
    }

    @Test fun weakRandomIsDeterministic() {
        assertEquals(WeakRandom.resetToken(1_700_000_000L), WeakRandom.resetToken(1_700_000_000L))
    }

    @Test fun tamperingLicenseCheckMatchesXorKey() {
        assertEquals("EC-ENT-2026-PRO", IntegrityGate.expectedLicenseKey())
        assertTrue(IntegrityGate.verifyLicense("EC-ENT-2026-PRO"))
        assertFalse(IntegrityGate.verifyLicense("INVALID"))
    }

    @Test fun sslPinValidatorChecksExpectedPin() {
        assertTrue(SslPinValidator.verifyCertificatePin(SslPinValidator.EXPECTED_PIN))
        assertFalse(SslPinValidator.verifyCertificatePin(SslPinValidator.PROXY_PIN))
    }

    @Test fun pkcs12KeystoreUnlocksWithDefaultPassword() {
        val tmp = Files.createTempDirectory("ec_ks_test").toFile()
        try {
            WeakKeystore.provision(tmp)
            val unlocked = WeakKeystore.inspectWithPassword(tmp, "changeit")
            assertTrue(unlocked.contains(Flags.KEYSTORE))
        } finally {
            tmp.deleteRecursively()
        }
    }
}
