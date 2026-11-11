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
 * INTENTIONALLY INSECURE: client-side emulator detection that relies on spoofable `Build.*`
 * constants, plus a QA override property that was never removed from the release.
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
        // Synthetic probe so the lab behaves the same on emulators and physical test devices.
        add("ro.kernel.qemu=1 (synthetic lab probe)")
    }

    @JvmStatic
    fun isEmulator(): Boolean {
        if (readProp(BYPASS_PROP) == "1") return false
        return detectedIndicators().isNotEmpty()
    }
}

@Composable
fun EmulatorLab() {
    var status by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The production key provisioning endpoint refuses to run on an Android Virtual Device. " +
            "EmulatorDetector inspects Build.* values and system properties before releasing the key. " +
            "Convince the app it is running on a physical device.",
        remediation = "Treat environment checks as defense-in-depth signals rather than sole access " +
            "controls. Combine Play Integrity device verdicts with server-side verification instead of " +
            "trusting Build.* strings on the client, and strip QA overrides from release builds.",
    ) {
        Mono("Detected indicators:\n" + EmulatorDetector.detectedIndicators().joinToString("\n") { "· $it" })
        PrimaryButton("Request Production Provisioning Key") {
            if (!EmulatorDetector.isEmulator()) {
                ok = true
                LogStore.append("[RESILIENCE] Emulator check bypassed")
                status = "Physical device verified!\nProvisioning key: ${Flags.EMULATOR}"
            } else {
                ok = false
                status = "Blocked: virtualized / emulator environment detected."
            }
        }
        if (status.isNotEmpty()) Mono(status, color = if (ok) Green else Red)
    }
}
