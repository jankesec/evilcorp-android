package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.ui.Green
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import com.evilcorp.android.ui.Red

/**
 * INTENTIONALLY INSECURE: client-side license & tamper verification algorithm embedded in bytecode.
 * Can be solved either by reversing expectedLicenseKey() in jadx or by hooking verifyLicense() in Frida.
 */
object IntegrityGate {
    // XOR-masked bytes of "EC-ENT-2026-PRO" with key 0x5A
    private val MASKED = intArrayOf(
        0x1F, 0x19, 0x77, 0x1F, 0x14, 0x0E, 0x77, 0x68, 0x6A, 0x68, 0x6C, 0x77, 0x0A, 0x08, 0x15
    )

    fun expectedLicenseKey(): String =
        MASKED.map { (it xor 0x5A).toChar() }.joinToString("")

    @JvmStatic
    fun verifyLicense(input: String): Boolean = input.trim() == expectedLicenseKey()
}

@Composable
fun TamperingLab() {
    var license by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "Enterprise features are locked behind IntegrityGate.verifyLicense(). Reverse " +
            "the XOR license check in jadx or patch/hook verifyLicense() to return true.",
        remediation = "Perform license and entitlement checks on the server using signed tokens " +
            "verified against a backend, and combine with Play Integrity API to detect modified APKs.",
    ) {
        LabField(license, { license = it }, "Enterprise license key")
        PrimaryButton("Activate Enterprise Edition") {
            if (IntegrityGate.verifyLicense(license)) {
                ok = true
                status = "Enterprise edition activated!\nUnlock flag: ${Flags.TAMPERING}"
            } else {
                ok = false
                status = "License check failed (Community Edition)."
            }
        }
        if (status.isNotEmpty()) Mono(status, color = if (ok) Green else Red)
    }
}
