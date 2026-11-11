package com.evilcorp.android.labs

import android.annotation.SuppressLint
import android.os.Build
import android.provider.Settings
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
import java.io.File

/**
 * INTENTIONALLY INSECURE: harvests persistent device identifiers (`ANDROID_ID`, hardware fingerprint,
 * non-resettable tracking correlation token) before user consent and persists them in plaintext.
 */
object TelemetryTracker {
    const val BEACON_FILE = "telemetry_beacon.json"

    fun writeFingerprintingBeacon(dir: File, androidId: String): File {
        val f = File(dir, BEACON_FILE)
        val json = """
            {
              "consent_granted": false,
              "android_id": "$androidId",
              "hardware_serial_fallback": "${Build.HARDWARE}-${Build.BOARD}",
              "cross_app_correlation_id": "${Flags.TRACKING}"
            }
        """.trimIndent()
        f.writeText(json)
        return f
    }
}

@Composable
fun PrivacyTrackingLab() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    var inspected by remember { mutableStateOf("") }

    LabFrame(
        scenario = "Even when the user declines analytics tracking, the embedded ad SDK harvests " +
            "Settings.Secure.ANDROID_ID and hardware attributes on startup and writes a correlation " +
            "beacon to files/${TelemetryTracker.BEACON_FILE} (MASVS-PRIVACY-2).",
        remediation = "Respect user consent before initializing analytics SDKs, never combine " +
            "non-resettable hardware identifiers (ANDROID_ID, IMEI, MAC, serial) for cross-app " +
            "tracking, and use user-resettable, app-scoped random UUIDs when needed.",
    ) {
        PrimaryButton("Decline Tracking & Trigger SDK Init") {
            @SuppressLint("HardwareIds")
            val androidId = runCatching {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }.getOrNull() ?: "9774d56d682e549c"
            val f = TelemetryTracker.writeFingerprintingBeacon(context.filesDir, androidId)
            LogStore.append("[PRIVACY] Covert fingerprint beacon written to ${f.name} (consent=false)")
            status = "User selected: OPT-OUT\n" +
                "Analytics consent stored as: declined.\n" +
                "(The SDK's own spool is not shown in the app UI.)"
        }
        PrimaryButton("Show consent receipt") {
            // The app only shows what it *intends* to collect; the beacon on disk tells another story.
            val f = File(context.filesDir, TelemetryTracker.BEACON_FILE)
            inspected = if (f.exists()) {
                "Consent receipt\n  analytics: declined\n  identifiers collected: none (claimed)"
            } else {
                "No consent decision recorded yet."
            }
        }
        if (status.isNotEmpty()) Mono(status)
        if (inspected.isNotEmpty()) Mono(inspected, color = Green)
    }
}
