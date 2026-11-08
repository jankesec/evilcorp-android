package com.evilcorp.android.labs

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.evilcorp.android.ui.Mono
import com.evilcorp.android.ui.SectionTitle
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/** INTENTIONALLY INSECURE: static key + ECB mode. */
object BrokenCrypto {
    private const val KEY = "EvilCorpKey12345" // 16 bytes -> AES-128

    @Suppress("GetInstance") // ECB is the point of the lab
    private fun cipher(mode: Int) = Cipher.getInstance("AES/ECB/PKCS5Padding").apply {
        init(mode, SecretKeySpec(KEY.toByteArray(), "AES"))
    }

    fun encrypt(plain: String): ByteArray = cipher(Cipher.ENCRYPT_MODE).doFinal(plain.toByteArray())

    fun decrypt(blob: ByteArray): String = String(cipher(Cipher.DECRYPT_MODE).doFinal(blob))
}

private fun ByteArray.hex() = joinToString("") { "%02x".format(it) }

@Composable
fun BrokenCryptoLab() {
    val vaultBlob = remember { Base64.encodeToString(BrokenCrypto.encrypt(Flags.CRYPTO), Base64.NO_WRAP) }
    val pattern = remember {
        BrokenCrypto.encrypt("TRANSFER:1000000TRANSFER:1000000").hex().chunked(32).joinToString("\n")
    }

    LabFrame(
        scenario = "The vault protects its contents with AES. Unfortunately it uses ECB mode and a key " +
            "that ships inside the app. Decrypt the vault blob.",
        remediation = "Use an authenticated mode (AES-GCM) with a random IV per message, and keep keys in " +
            "Android Keystore (non-exportable). Prefer Jetpack Security / Tink over hand-rolled crypto.",
    ) {
        SectionTitle("Vault blob (Base64)")
        Mono(vaultBlob)
        SectionTitle("ECB pattern leak: two identical plaintext blocks")
        Mono(pattern)
    }
}
