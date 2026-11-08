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
 * INTENTIONALLY INSECURE: encrypts two messages in stream/CTR mode under the same key and a
 * hardcoded all-zero 12-byte IV/nonce (`000000000000`). XORing C1 ^ C2 ^ P1 recovers P2 without
 * knowing the secret key.
 */
object NonceReuseCrypto {
    const val STATIC_IV_HEX = "000000000000000000000000"
    const val KNOWN_PLAINTEXT = "GUEST_SESSION_HEADER_2026_pad_0000000"
    private val KEYSTREAM = ByteArray(64) { i -> ((i * 37 + 0x5D) and 0xFF).toByte() }

    private fun xorWithStream(data: ByteArray): ByteArray =
        ByteArray(data.size) { i -> (data[i].toInt() xor KEYSTREAM[i % KEYSTREAM.size].toInt()).toByte() }

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    fun fromHex(hex: String): ByteArray {
        val clean = hex.trim().replace(" ", "")
        require(clean.length % 2 == 0) { "Odd hex length" }
        return ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    val CIPHERTEXT_1_HEX: String = toHex(xorWithStream(KNOWN_PLAINTEXT.toByteArray(Charsets.UTF_8)))
    val CIPHERTEXT_2_HEX: String = toHex(xorWithStream(Flags.IVREUSE.toByteArray(Charsets.UTF_8)))

    @JvmStatic
    fun recoverViaTwoTimePad(c1Hex: String, p1Ascii: String, c2Hex: String): String =
        runCatching {
            val c1 = fromHex(c1Hex)
            val p1 = p1Ascii.toByteArray(Charsets.UTF_8)
            val c2 = fromHex(c2Hex)
            val len = minOf(c1.size, p1.size, c2.size)
            val p2 = ByteArray(len) { i ->
                (c1[i].toInt() xor p1[i].toInt() xor c2[i].toInt()).toByte()
            }
            String(p2, Charsets.UTF_8)
        }.getOrElse { "Error: ${it.message}" }
}

@Composable
fun IvReuseLab() {
    var candidateC2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    LabFrame(
        scenario = "Both the public greeting packet (C1) and the admin vault flag (C2) were encrypted " +
            "with AES-CTR using a hardcoded zero IV (${NonceReuseCrypto.STATIC_IV_HEX}). Reusing a " +
            "key + IV pair creates a two-time pad: P2 = C1 XOR P1 XOR C2.",
        remediation = "Never hardcode or reuse initialization vectors (IVs) or nonces. Let " +
            "AndroidKeyStore or Cipher.init(Cipher.ENCRYPT_MODE, key) generate a fresh random IV " +
            "via SecureRandom for every encryption operation.",
    ) {
        Mono(
            "Static IV: ${NonceReuseCrypto.STATIC_IV_HEX}\n" +
                "P1 (ascii): ${NonceReuseCrypto.KNOWN_PLAINTEXT}\n" +
                "C1 (hex):   ${NonceReuseCrypto.CIPHERTEXT_1_HEX}\n" +
                "C2 (hex):   ${NonceReuseCrypto.CIPHERTEXT_2_HEX}"
        )
        LabField(candidateC2, { candidateC2 = it }, "Paste C2 (hex) to XOR against (C1 ^ P1)")
        PrimaryButton("Recover Plaintext (C1 ⊕ P1 ⊕ C2)") {
            val recovered = NonceReuseCrypto.recoverViaTwoTimePad(
                NonceReuseCrypto.CIPHERTEXT_1_HEX,
                NonceReuseCrypto.KNOWN_PLAINTEXT,
                candidateC2,
            )
            ok = recovered == Flags.IVREUSE
            result = "Recovered P2: $recovered"
        }
        if (result.isNotEmpty()) Mono(result, color = if (ok) Green else Red)
    }
}
