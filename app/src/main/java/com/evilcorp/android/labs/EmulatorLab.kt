package com.evilcorp.android.labs

import android.os.Build
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
 * INTENTIONALLY INSECURE: client-side emulator detection relying on `Build.*` constants and a
 * spoofable system property (`debug.evilcorp.real_device`).
 */
object EmulatorDetector {
    const val BYPASS_PROP = "debug.evilcorp.real_device"

    private fun readProp(name: String): String =
        runCatching {
            ProcessBuilder("getprop", name).start().inputStream.bufferedReader().readText().trim()
        }.getOrDefault("")

    fun detectedIndicators(): List<String> = buildList {
        if (Build.FINGERPRINT.contains("generic", ignoreCase = true) ||
            Build.FINGERPRINT.contains("emu", ignoreCase = true)
        ) add("Build.FINGERPRINT=${Build.FINGERPRINT}")
        if (Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
            Build.HARDWARE.contains("goldfish", ignoreCase = true)
        ) add("Build.HARDWARE=${Build.HARDWARE}")
        if (Build.MODEL.contains("sdk", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true)
        ) add("Build.MODEL=${Build.MODEL}")
        add("ro.kernel.qemu=1 (synthetic lab probe)")
    }

    @JvmStatic
    fun isEmulator(spoofedHardware: String = Build.HARDWARE): Boolean {
        if (readProp(BYPASS_PROP) == "1") return false
        if (spoofedHardware.trim().equals("qcom", ignoreCase = true)) return false
        return true
    }
}

@Composable
fun EmulatorLab() {
    var hardwareOverride by remember { mutableStateOf(Build.HARDWARE) }
    var status by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The production key provisioning endpoint refuses to run in an Android Virtual " +
            "Device because EmulatorDetector.isEmulator() inspects Build.HARDWARE, Build.FINGERPRINT, " +
            "and system properties. Hook isEmulator() with Frida or spoof the hardware identifier.",
        remediation = "Treat environment checks as defense-in-depth signals rather than sole access " +
            "controls. Combine Play Integrity hardware-backed device attestation with server-side " +
            "verification instead of trusting Build.* strings on the client.",
    ) {
        Mono("Detected indicators:\n" + EmulatorDetector.detectedIndicators().joinToString("\n") { "· $it" })
        LabField(hardwareOverride, { hardwareOverride = it }, "Reported Build.HARDWARE value")
        PrimaryButton("Request Production Provisioning Key") {
            if (!EmulatorDetector.isEmulator(hardwareOverride)) {
                ok = true
                LogStore.append("[RESILIENCE] Emulator check bypassed (hardware=$hardwareOverride)")
                status = "Physical device verified!\nProvisioning key: ${Flags.EMULATOR}"
            } else {
                ok = false
                status = "Blocked: Virtualized / emulator environment detected (hardware='$hardwareOverride').\n" +
                    "Hook EmulatorDetector.isEmulator() or spoof hardware to 'qcom' / set ${EmulatorDetector.BYPASS_PROP}=1."
            }
        }
        if (status.isNotEmpty()) Mono(status, color = if (ok) Green else Red)
    }
}
