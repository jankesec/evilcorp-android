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
 * INTENTIONALLY INSECURE: event-bound biometric check (a plain boolean return value not bound to a
 * Keystore CryptoObject), plus a leftover QA system property backdoor.
 */
object BiometricGate {
    const val QA_PROP = "debug.evilcorp.bio_override"

    private fun readProp(name: String): String =
        runCatching {
            ProcessBuilder("getprop", name).start().inputStream.bufferedReader().readText().trim()
        }.getOrDefault("")

    @JvmStatic
    fun isBiometricVerified(): Boolean = readProp(QA_PROP) == "true"
}

@Composable
fun BiometricLab() {
    var result by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "The corporate vault uses an event-bound biometric check: instead of decrypting " +
            "data with a Keystore CryptoObject unlocked by BiometricPrompt, it simply checks whether " +
            "BiometricGate.isBiometricVerified() returns true.",
        remediation = "Bind biometric authentication to cryptography: generate a key in AndroidKeyStore " +
            "with setUserAuthenticationRequired(true), pass a Cipher wrapped in BiometricPrompt.CryptoObject " +
            "to authenticate(), and use the unlocked Cipher inside onAuthenticationSucceeded.",
    ) {
        PrimaryButton("Authenticate with Biometrics") {
            if (BiometricGate.isBiometricVerified()) {
                unlocked = true
                result = "Biometric callback succeeded!\nVault secret: ${Flags.BIOMETRIC}"
            } else {
                unlocked = false
                result = "BIOMETRIC_ERROR_NO_HARDWARE / Cancelled.\n" +
                    "BiometricGate.isBiometricVerified() returned false."
            }
        }
        if (result.isNotEmpty()) Mono(result, color = if (unlocked) Green else Red)
    }
}
