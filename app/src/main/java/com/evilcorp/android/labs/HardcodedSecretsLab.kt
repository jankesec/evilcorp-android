package com.evilcorp.android.labs

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

/**
 * INTENTIONALLY INSECURE: "obfuscated" API key shipped inside the APK.
 * Base64 is an encoding, not encryption.
 */
object HardcodedSecrets {
    @JvmField
    val EC_API_KEY_B64: String = "ZWNfbGl2ZV83ZjNhOWMyZTFiNGQ4YTZmMGU1Yw=="

    fun apiKey(): String = String(Base64.decode(EC_API_KEY_B64, Base64.DEFAULT))
}

@Composable
fun HardcodedSecretsLab() {
    var key by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The partner API is \"protected\" by a static API key that the developers compiled " +
            "into the app. Recover the key from the APK and call the privileged endpoint.",
        remediation = "Treat everything in the APK as public. Use short-lived, per-user tokens from a " +
            "backend; keep partner keys server-side and proxy the calls. Add secret scanning " +
            "(gitleaks, trufflehog) to CI.",
    ) {
        LabField(key, { key = it }, "X-EvilCorp-Key")
        PrimaryButton("GET /v1/admin/export") {
            // Simulated backend: the "server" trusts any client that knows the static key.
            response = if (key.trim() == HardcodedSecrets.apiKey()) {
                "HTTP/1.1 200 OK\n{\"export\": \"${Flags.SECRETS}\"}"
            } else {
                "HTTP/1.1 401 Unauthorized\n{\"error\": \"invalid api key\"}"
            }
        }
        if (response.isNotEmpty()) Mono(response)
    }
}
