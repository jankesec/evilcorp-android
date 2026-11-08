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
 * INTENTIONALLY INSECURE: simulates a custom X509TrustManager whose checkServerTrusted() is a no-op
 * combined with an HostnameVerifier that always returns true (`ALLOW_ALL_HOSTNAME_VERIFIER`).
 */
object InsecureTlsClient {
    const val EXPECTED_HOST = "treasury.evilcorp.internal"

    @JvmStatic
    fun acceptServerCertificate(subjectCn: String, selfSigned: Boolean, allowAllHostname: Boolean): Boolean {
        return selfSigned && allowAllHostname && subjectCn.isNotBlank()
    }
}

@Composable
fun TrustManagerLab() {
    var peerCn by remember { mutableStateOf("CN=mitm-proxy.attacker.local") }
    var selfSigned by remember { mutableStateOf(true) }
    var allowAllHost by remember { mutableStateOf(false) }
    var output by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "To 'fix' TLS errors in staging, a developer installed a custom X509TrustManager " +
            "with an empty checkServerTrusted() method and a HostnameVerifier that returns true for " +
            "any domain. Intercept the handshake with a self-signed MITM certificate.",
        remediation = "Never override X509TrustManager with empty checks or return true from " +
            "HostnameVerifier. Use the system default TrustManager and scope any debug CA strictly " +
            "to <debug-overrides> inside network_security_config.xml.",
    ) {
        LabField(peerCn, { peerCn = it }, "Presented certificate Subject CN")
        Mono(
            "Target Host:       ${InsecureTlsClient.EXPECTED_HOST}\n" +
                "TrustAll Manager:  checkServerTrusted() { /* empty */ }\n" +
                "HostnameVerifier:  ${if (allowAllHost) "ALLOW_ALL ({ _, _ -> true })" else "Strict (RFC 2818)"}"
        )
        PrimaryButton(
            if (allowAllHost) "HostnameVerifier: ALLOW_ALL (Insecure)"
            else "Toggle HostnameVerifier -> ALLOW_ALL"
        ) {
            allowAllHost = !allowAllHost
        }
        PrimaryButton("Intercept TLS Handshake (MITM)") {
            if (InsecureTlsClient.acceptServerCertificate(peerCn, selfSigned, allowAllHost)) {
                ok = true
                LogStore.append("[NET] MITM cert '$peerCn' accepted by TrustAll X509TrustManager")
                output = "TLS MITM succeeded against ${InsecureTlsClient.EXPECTED_HOST}!\n" +
                    "Accepted spoofed cert: $peerCn\n" +
                    "Captured Authorization header: Bearer ${Flags.TRUSTMANAGER}"
            } else {
                ok = false
                output = "SSLHandshakeException: Hostname '${InsecureTlsClient.EXPECTED_HOST}' " +
                    "does not match certificate '$peerCn'. Enable ALLOW_ALL HostnameVerifier or hook InsecureTlsClient."
            }
        }
        if (output.isNotEmpty()) Mono(output, color = if (ok) Green else Red)
    }
}
