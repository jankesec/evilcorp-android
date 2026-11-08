package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import java.security.MessageDigest

/** INTENTIONALLY INSECURE: unsalted MD5 of a 4-digit PIN, checked on the client, no lockout. */
object PinVault {
    const val PIN_MD5 = "88cf91a1aef212f3c2cd12406983427d"

    fun md5(s: String): String =
        MessageDigest.getInstance("MD5").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    fun check(pin: String): Boolean = md5(pin) == PIN_MD5
}

@Composable
fun PinVaultLab() {
    var pin by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf("") }

    LabFrame(
        scenario = "The vault is locked with a 4-digit PIN. The app compares an MD5 hash locally and " +
            "never locks you out. Open the vault.",
        remediation = "Verify PINs server-side or bind them to a Keystore key with user authentication; " +
            "use a slow, salted KDF (Argon2/scrypt/PBKDF2), enforce attempt limits with backoff, and " +
            "never ship the verifier in the client.",
    ) {
        LabField(pin, { if (it.length <= 4 && it.all(Char::isDigit)) pin = it }, "PIN", secret = true)
        PrimaryButton("Unlock") {
            attempts++
            result = if (PinVault.check(pin)) "Vault open: ${Flags.PIN}" else "Wrong PIN. Attempts: $attempts (no lockout)"
        }
        if (result.isNotEmpty()) Mono(result)
    }
}
