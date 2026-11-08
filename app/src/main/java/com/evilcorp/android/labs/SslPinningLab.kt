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
 * INTENTIONALLY INSECURE: single client-side boolean pin check that is easy to hook with Frida,
 * plus an exposed pin constant in bytecode.
 */
object SslPinValidator {
    const val EXPECTED_PIN = "sha256/9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"
    const val PROXY_PIN = "sha256/0000000000000000000000000000000000000000000000000000000000000000"

    @JvmStatic
    fun verifyCertificatePin(presentedPin: String): Boolean = presentedPin.trim() == EXPECTED_PIN
}

@Composable
fun SslPinningLab() {
    var presentedPin by remember { mutableStateOf(SslPinValidator.PROXY_PIN) }
    var response by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The high-value API refuses to talk through your intercepting proxy because the " +
            "client pins the server's SPKI SHA-256 hash in SslPinValidator.verifyCertificatePin(). " +
            "Bypass the pin check with Frida or supply the expected pin from the decompiled APK.",
        remediation = "Configure certificate pinning declaratively in network_security_config.xml " +
            "with backup pins, combine with R8 obfuscation and runtime integrity checks, and never " +
            "trust client-side pinning alone to authorize privileged requests.",
    ) {
        LabField(presentedPin, { presentedPin = it }, "Presented certificate SPKI pin")
        PrimaryButton("Send pinned request") {
            if (SslPinValidator.verifyCertificatePin(presentedPin)) {
                ok = true
                response = "TLS handshake verified.\nHTTP/1.1 200 OK\n{\"wire_secret\": \"${Flags.SSLPINNING}\"}"
            } else {
                ok = false
                response = "javax.net.ssl.SSLPeerUnverifiedException: Certificate pinning failure!\n" +
                    "  Peer pin: $presentedPin"
            }
        }
        if (response.isNotEmpty()) Mono(response, color = if (ok) Green else Red)
    }
}
