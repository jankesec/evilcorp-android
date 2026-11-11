package com.evilcorp.android.labs

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.data.LogStore
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red
import java.security.MessageDigest

/**
 * INTENTIONALLY INSECURE: the signing-certificate and installer checks run only on the client
 * (`ApkSignatureVerifier.isOfficialBuild`), so they can be hooked or patched out before re-signing.
 * The inputs are real: the certificate that signed the installed APK and the package that installed it.
 */
object ApkSignatureVerifier {
    /** SHA-256 of the (fictional) release signing certificate. The release key is not in this repo. */
    const val OFFICIAL_CERT_SHA256 = "E4:9A:2F:7C:10:B8:41:D3:99:06:CE:5E:8A:12:4F:77:AA:01:5C:33:8D:29:F0:6B:11:C4:7E:92:05:3B:D1:88"
    /** Example digest used by the unit tests to model a debug/re-signed build. */
    const val DEBUG_CERT_SHA256 = "11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00:AA:BB:CC:DD:EE:FF"
    const val EXPECTED_INSTALLER = "com.android.vending"

    @JvmStatic
    fun isOfficialBuild(certSha256: String, installerPkg: String?): Boolean =
        certSha256.trim().equals(OFFICIAL_CERT_SHA256, ignoreCase = true) &&
            installerPkg?.trim() == EXPECTED_INSTALLER

    /** SHA-256 of the certificate that signed the installed APK, formatted AA:BB:... */
    fun installedCertSha256(context: Context): String = runCatching {
        val pm = context.packageManager
        val signers: Array<out Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures.orEmpty()
        }
        val cert = signers.firstOrNull() ?: return@runCatching "unknown"
        MessageDigest.getInstance("SHA-256").digest(cert.toByteArray())
            .joinToString(":") { "%02X".format(it) }
    }.getOrDefault("unknown")

    /** Package that installed the app (null for `adb install` and most sideloads). */
    fun installerPackage(context: Context): String? = runCatching {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            pm.getInstallerPackageName(context.packageName)
        }
    }.getOrNull()
}

@Composable
fun RepackagingLab() {
    val context = LocalContext.current
    val certDigest = remember { ApkSignatureVerifier.installedCertSha256(context) }
    val installer = remember { ApkSignatureVerifier.installerPackage(context) }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "To block repackaged or sideloaded copies, the app releases its premium content only when " +
            "it was signed with the official release key and installed by Google Play. Both checks run " +
            "on the device. This build is debug-signed and was installed over adb, so it fails. " +
            "Get the content anyway.",
        remediation = "Client-side PackageManager comparisons can be patched out before re-signing. Send " +
            "Play Integrity (appRecognitionVerdict) or hardware-backed key attestation evidence to your " +
            "backend and enforce the verdict there.",
    ) {
        Mono(
            "Installed signing cert SHA-256:\n$certDigest\n\n" +
                "Installer package: ${installer ?: "null (adb / sideload)"}"
        )
        PrimaryButton("Verify release signature & installer") {
            if (ApkSignatureVerifier.isOfficialBuild(certDigest, installer)) {
                ok = true
                LogStore.append("[RESILIENCE] APK signature & installer check passed")
                output = "Release signature & Play Store origin verified!\nFlag: ${Flags.REPACKAGING}"
            } else {
                ok = false
                output = "Tamper alert: untrusted signing certificate or installer. Premium content locked."
            }
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
