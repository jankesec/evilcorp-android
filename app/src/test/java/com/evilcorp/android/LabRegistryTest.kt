package com.evilcorp.android

import com.evilcorp.android.labs.ApkSignatureVerifier
import com.evilcorp.android.labs.FlagVault
import com.evilcorp.android.labs.Flags
import com.evilcorp.android.labs.IntegrityGate
import com.evilcorp.android.labs.JwtSessionVerifier
import com.evilcorp.android.labs.LabRegistry
import com.evilcorp.android.labs.NativeBridgeSim
import com.evilcorp.android.labs.NonceReuseCrypto
import com.evilcorp.android.labs.PinVault
import com.evilcorp.android.labs.SessionTokenParcel
import com.evilcorp.android.labs.SslPinValidator
import com.evilcorp.android.labs.StepUpAuthController
import com.evilcorp.android.labs.TaskStackInspector
import com.evilcorp.android.labs.UnsafeDeserializer
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
        assertEquals(42, LabRegistry.entries.size)
        assertEquals(ChallengeCatalog.all.map { it.id }.toSet(), LabRegistry.entries.keys)
    }

    @Test fun flagVaultCoversEveryLabWithUniqueWellFormedFlags() {
        assertEquals(LabRegistry.entries.keys, FlagVault.ids.toSet())
        val flags = FlagVault.ids.map(FlagVault::reveal)
        assertEquals(42, flags.toSet().size)
        flags.forEach { assertTrue(it, it.matches(Regex("EVILCORP\\{[A-Za-z0-9_]+}"))) }
    }

    @Test fun flagCheckIsExactAndDigestBased() {
        val f = FlagVault.reveal("sqli")
        assertTrue(LabRegistry.isCorrect("sqli", "  $f "))
        assertFalse(LabRegistry.isCorrect("sqli", f.lowercase()))
        assertFalse(LabRegistry.isCorrect("sqli", f.dropLast(1)))
        assertFalse(LabRegistry.isCorrect("prefs", f))
        assertFalse(LabRegistry.isCorrect("unknown", f))
    }

    @Test fun flagGettersDecodeFromTheVault() {
        assertEquals(FlagVault.reveal("keystore"), Flags.KEYSTORE)
        assertTrue(FlagVault.matches("keystore", Flags.KEYSTORE))
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

    @Test fun jwtAlgNoneTokenIsAcceptedAsAdmin() {
        assertFalse(JwtSessionVerifier.isAdminToken(JwtSessionVerifier.DEFAULT_GUEST_TOKEN))
        assertTrue(JwtSessionVerifier.isAdminToken(JwtSessionVerifier.forgeAlgNoneAdminToken()))
    }

    @Test fun jwtClaimTamperingWithoutAlgNoneIsRejected() {
        val (header, _, signature) = JwtSessionVerifier.DEFAULT_GUEST_TOKEN.split(".")
        val adminClaims = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"sub":"j.doe","role":"admin","dept":"retail"}""".toByteArray())
        assertFalse(JwtSessionVerifier.isAdminToken("$header.$adminClaims.$signature"))
        assertFalse(JwtSessionVerifier.isAdminToken("$header.$adminClaims."))
    }

    @Test fun stepUpSkippedForInternalBatchChannel() {
        assertFalse(StepUpAuthController.isStepUpSatisfied(250_000, "mobile_app", ""))
        assertTrue(StepUpAuthController.isStepUpSatisfied(250_000, StepUpAuthController.TRUSTED_BATCH_CHANNEL, ""))
    }

    @Test fun staticIvTwoTimePadRecoversSecretFlag() {
        // The known plaintext must cover the whole flag, otherwise the lab is unsolvable.
        assertTrue(NonceReuseCrypto.KNOWN_PLAINTEXT.length >= Flags.IVREUSE.length)
        val (c1, c2) = NonceReuseCrypto.interceptedPackets()
        assertEquals(Flags.IVREUSE, NonceReuseCrypto.recoverViaTwoTimePad(c1, NonceReuseCrypto.KNOWN_PLAINTEXT, c2))
    }

    @Test fun ivReuseKeyIsNotRecoverableFromTheApk() {
        // Two independent keys give different ciphertexts: nothing about the key is hardcoded.
        val a = NonceReuseCrypto.encrypt(NonceReuseCrypto.newKey(), NonceReuseCrypto.KNOWN_PLAINTEXT)
        val b = NonceReuseCrypto.encrypt(NonceReuseCrypto.newKey(), NonceReuseCrypto.KNOWN_PLAINTEXT)
        assertFalse(a.contentEquals(b))
    }

    @Test fun unsafeDeserializationRestoresSuperAdminFlag() {
        val b64 = UnsafeDeserializer.serializeToBase64(SessionTokenParcel("admin", isSuperAdmin = true))
        val parsed = UnsafeDeserializer.deserializeFromBase64(b64)
        assertTrue(parsed.isSuperAdmin)
    }

    @Test fun jniBufferOverflowCorruptsPrivilegeByte() {
        assertFalse(NativeBridgeSim.jniParseBadge("jdoe").corruptedPrivilege)
        assertTrue(NativeBridgeSim.jniParseBadge("A".repeat(17)).corruptedPrivilege)
    }

    @Test fun apkSignatureAndTaskAffinityChecksBehaveAsExpected() {
        assertTrue(
            ApkSignatureVerifier.isOfficialBuild(
                ApkSignatureVerifier.OFFICIAL_CERT_SHA256,
                ApkSignatureVerifier.EXPECTED_INSTALLER,
            )
        )
        assertFalse(ApkSignatureVerifier.isOfficialBuild(ApkSignatureVerifier.DEBUG_CERT_SHA256, "adb"))
        val hijacked = TaskStackInspector.simulateTaskReparenting(TaskStackInspector.VULNERABLE_AFFINITY, true)
        assertTrue(hijacked.contains(Flags.STRANDHOGG))
    }
}
