package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

/**
 * INTENTIONALLY INSECURE: client-side APK signing-certificate & installer package verification
 * performed in Kotlin bytecode (`ApkSignatureVerifier.isOfficialBuild`).
 */
object ApkSignatureVerifier {
    const val OFFICIAL_CERT_SHA256 = "E4:9A:2F:7C:10:B8:41:D3:99:06:CE:5E:8A:12:4F:77:AA:01:5C:33:8D:29:F0:6B:11:C4:7E:92:05:3B:D1:88"
    const val DEBUG_CERT_SHA256 = "11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF"
    const val EXPECTED_INSTALLER = "com.android.vending"

    @JvmStatic
    fun isOfficialBuild(certSha256: String, installerPkg: String): Boolean =
        certSha256.trim().equals(OFFICIAL_CERT_SHA256, ignoreCase = true) &&
            installerPkg.trim() == EXPECTED_INSTALLER
}

@Composable
fun RepackagingLab() {
    var certDigest by remember { mutableStateOf(ApkSignatureVerifier.DEBUG_CERT_SHA256) }
    var installer by remember { mutableStateOf("adb (null)") }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "To block repackaged APKs signed with a debug/custom key or sideloaded via ADB, " +
            "ApkSignatureVerifier.isOfficialBuild() compares PackageInfo.signingInfo and " +
            "InstallSourceInfo against constants in bytecode. Bypass the check via Frida or jadx.",
        remediation = "Do not rely solely on client-side PackageManager signature comparisons, which " +
            "an attacker can patch out before re-signing. Validate Play Integrity API appLicensingVerdict " +
            "and appRecognitionVerdict tokens on your backend server.",
    ) {
        LabField(certDigest, { certDigest = it }, "Presented APK Signing Cert SHA-256")
        LabField(installer, { installer = it }, "Reported installer package")
        PrimaryButton("Verify APK Signature & Installer") {
            if (ApkSignatureVerifier.isOfficialBuild(certDigest, installer)) {
                ok = true
                LogStore.append("[RESILIENCE] APK signature & installer check passed")
                output = "Release signature & Play Store origin verified!\nFlag: ${Flags.REPACKAGING}"
            } else {
                ok = false
                output = "Tamper alert: untrusted signing certificate or sideloaded installer ('$installer').\n" +
                    "Inspect ApkSignatureVerifier in jadx or hook isOfficialBuild() in Frida."
            }
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
