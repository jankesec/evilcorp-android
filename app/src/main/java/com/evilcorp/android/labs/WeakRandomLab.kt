package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton

/** INTENTIONALLY INSECURE: predictable reset token. */
object WeakRandom {
    fun resetToken(seed: Long): String = java.lang.Long.toString(java.util.Random(seed).nextLong(), 36)
}

@Composable
fun WeakRandomLab() {
    var seed by remember { mutableLongStateOf(0L) }
    var guess by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The password-reset flow emails the admin a token generated with java.util.Random, " +
            "seeded with the request time. The API helpfully echoes that time back. Predict the token.",
        remediation = "Generate security tokens with java.security.SecureRandom (at least 128 bits), " +
            "store only a hash server-side, make them single-use and short-lived.",
    ) {
        PrimaryButton("Request reset for admin") {
            seed = System.currentTimeMillis() / 1000
            response = ""
        }
        if (seed != 0L) {
            Mono("POST /reset {\"user\":\"admin\"}\n202 Accepted\n{\"requested_at\": $seed, \"delivery\": \"email\"}")
            LabField(guess, { guess = it }, "Reset token")
            PrimaryButton("Submit token") {
                response = if (guess.trim() == WeakRandom.resetToken(seed)) {
                    "Password reset authorised. Admin recovery note: ${Flags.RANDOM}"
                } else {
                    "Invalid token."
                }
            }
            if (response.isNotEmpty()) Mono(response)
        }
    }
}
