package com.evilcorp.android.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.PrimaryButton
import java.io.File
import java.security.KeyStore
import javax.crypto.spec.SecretKeySpec

/**
 * INTENTIONALLY INSECURE: uses a file-backed PKCS12 keystore with the default password "changeit"
 * instead of hardware-backed AndroidKeyStore with non-exportable keys.
 */
object WeakKeystore {
    const val FILE_NAME = "vault.p12"
    const val KEYSTORE_PASS = "changeit"
    const val ENTRY_ALIAS = "evilcorp_master_key"

    fun provision(dir: File): File {
        val file = File(dir, FILE_NAME)
        val ks = KeyStore.getInstance("PKCS12")
        ks.load(null, KEYSTORE_PASS.toCharArray())
        val secretKey = SecretKeySpec(Flags.KEYSTORE.toByteArray(Charsets.UTF_8), "AES")
        val prot = KeyStore.PasswordProtection(KEYSTORE_PASS.toCharArray())
        ks.setEntry(ENTRY_ALIAS, KeyStore.SecretKeyEntry(secretKey), prot)
        file.outputStream().use { ks.store(it, KEYSTORE_PASS.toCharArray()) }
        return file
    }

    fun inspectWithPassword(dir: File, password: String): String {
        val file = File(dir, FILE_NAME)
        if (!file.exists()) return "Keystore not provisioned yet."
        return runCatching {
            val ks = KeyStore.getInstance("PKCS12")
            file.inputStream().use { ks.load(it, password.toCharArray()) }
            val key = ks.getKey(ENTRY_ALIAS, password.toCharArray())
            "Entry '$ENTRY_ALIAS' (${key.algorithm}): ${String(key.encoded, Charsets.UTF_8)}"
        }.getOrElse { "Failed to unlock PKCS#12: ${it.message}" }
    }
}

@Composable
fun KeystoreLab() {
    val context = LocalContext.current
    var status by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var inspectResult by remember { mutableStateOf("") }

    LabFrame(
        scenario = "Instead of generating non-exportable keys inside the hardware-backed " +
            "AndroidKeyStore, the developer saved the master secret in a portable PKCS#12 file " +
            "(vault.p12) protected by a well-known default password.",
        remediation = "Use KeyStore.getInstance(\"AndroidKeyStore\") with KeyGenParameterSpec, " +
            "keep keys non-exportable inside TEE/StrongBox, and require user authentication " +
            "(setUserAuthenticationRequired(true)) for sensitive operations.",
    ) {
        PrimaryButton("Provision vault.p12") {
            val f = WeakKeystore.provision(context.filesDir)
            status = "Saved PKCS#12 store to:\n${f.absolutePath}\nAlias: ${WeakKeystore.ENTRY_ALIAS}"
        }
        if (status.isNotEmpty()) Mono(status)
        LabField(password, { password = it }, "PKCS#12 password", secret = true)
        PrimaryButton("Test unlock vault.p12") {
            inspectResult = WeakKeystore.inspectWithPassword(context.filesDir, password.trim())
        }
        if (inspectResult.isNotEmpty()) Mono(inspectResult)
    }
}
